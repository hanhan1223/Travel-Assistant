package org.example.travel.agent;

import cn.hutool.json.JSONUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 题目生成 Agent
 * 继承 ToolCallAgent，使用知识库检索和联网搜索生成高质量题目
 *
 * 注意：此 Agent 使用同步锁保证线程安全，多用户并发调用会排队执行
 */
@Slf4j
@Component
public class QuizAgent extends ToolCallAgent {

    // 同步锁，保证多用户并发安全
    private final Object lock = new Object();

    /**
     * 题目 DTO（用于结构化输出）
     */
    @Data
    public static class QuestionDTO {
        private String questionText;
        private List<String> options;
        private String correctAnswer;
        private String explanation;
        private Integer points;
    }

    /**
     * 题目列表包装类
     */
    @Data
    public static class QuestionListDTO {
        private List<QuestionDTO> questions;
    }

    private static final String QUIZ_SYSTEM_PROMPT = """
            你是一位专业的非遗知识题目出题专家。你的任务是根据非遗项目生成高质量的知识题目。
            
            出题要求：
            1. 题目必须基于真实的非遗知识，不能编造
            2. 题目要有趣味性和知识性
            3. 选项要合理，干扰项要有迷惑性
            4. 答案解析要简洁明了
            5. 每道题必须包含完整的：题目文本、4个选项、正确答案、答案解析、分值
            
            工作流程：
            1. 使用 searchKnowledge 工具从知识库检索非遗项目的详细信息
            2. 如果知识库信息不足，使用 searchWeb 工具联网搜索补充
            3. 基于检索到的真实信息生成完整的题目
            4. 生成题目后，立即调用 doTerminate 工具结束，将题目作为 finalAnswer
            
            【重要】
            - 不要重复搜索！获取信息后立即生成题目并调用 doTerminate 结束
            - 必须确保每道题的所有字段都完整，缺少任何字段的题目都不合格
            """;

    private static final String QUIZ_NEXT_STEP_PROMPT = """
            根据上一步的结果，决定下一步行动：
            1. 如果还没有检索知识库，调用 searchKnowledge 工具
            2. 如果知识库信息不足，调用 searchWeb 工具联网搜索补充
            3. 如果已经获取了足够信息，立即生成完整题目并调用 doTerminate 结束
            4. 【禁止】不要重复搜索，不要超过 5 步
            5. 【必须】最终输出必须是标准的 JSON 数组格式
            6. 【必须】每道题必须包含完整的所有字段：questionText、options、correctAnswer、explanation、points
            """;

    public QuizAgent(ToolCallback[] availableTools,
                     ChatModel chatModel,
                     @Qualifier("applicationTaskExecutor") java.util.concurrent.Executor toolExecutor) {
        super(availableTools, toolExecutor);
        setName("题目生成专家");
        setSystemPrompt(QUIZ_SYSTEM_PROMPT);
        setNextStepPrompt(QUIZ_NEXT_STEP_PROMPT);
        setChatClient(ChatClient.builder(chatModel).build());
        setMaxSteps(5); // 减少步数，避免过度搜索
    }

    /**
     * 生成题目（使用结构化输出）
     * 线程安全：使用同步锁保证多用户并发调用时串行执行
     *
     * @param projectName 非遗项目名称
     * @param difficulty 难度（1-简单 2-中等 3-困难）
     * @param count 题目数量
     * @return 题目列表（JSON格式）
     */
    public String generateQuestions(String projectName, int difficulty, int count) {
        // 使用同步锁，保证多用户并发安全
        synchronized (lock) {
            log.info("开始生成题目: project={}, difficulty={}, count={}", projectName, difficulty, count);
            
            int maxRetries = 3; // 最多重试3次
            Exception lastException = null;
            
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    log.info("第 {} 次尝试生成题目", attempt);
                    String result = attemptGenerateQuestions(projectName, difficulty, count);
                    log.info("题目生成成功（第 {} 次尝试）", attempt);
                    return result;
                } catch (Exception e) {
                    lastException = e;
                    log.warn("第 {} 次生成题目失败: {}", attempt, e.getMessage());
                    
                    if (attempt < maxRetries) {
                        log.info("等待 2 秒后重试...");
                        try {
                            Thread.sleep(2000); // 等待2秒后重试
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
            
            // 所有重试都失败
            log.error("题目生成失败，已重试 {} 次", maxRetries);
            throw new RuntimeException("题目生成失败（已重试" + maxRetries + "次）: " + lastException.getMessage());
        }
    }
    
    /**
     * 尝试生成题目（单次）
     * 使用 ChatClient 的结构化输出功能
     */
    private String attemptGenerateQuestions(String projectName, int difficulty, int count) {
        String difficultyText = getDifficultyText(difficulty);

        // 第一步：使用 Agent 收集信息
        String knowledgeInfo = collectKnowledgeInfo(projectName);
        
        // 第二步：使用 ChatClient 结构化输出生成题目
        String prompt = String.format("""
                基于以下关于「%s」的知识信息，生成 %d 道非遗知识题目，难度为「%s」。
                
                知识信息：
                %s
                
                题目要求：
                - 题目类型：单选题
                - 每道题必须包含：题目文本、4个选项（A/B/C/D）、正确答案（只写字母）、答案解析、分值
                - 题目要基于上述真实的非遗知识，不能编造
                - 答案解析要简洁（不超过50字）
                - 每道题分值为 10 分
                - 选项格式：["A. 选项1", "B. 选项2", "C. 选项3", "D. 选项4"]
                - 正确答案格式：只写字母，如 "A" 或 "B"
                
                请严格按照 JSON 格式输出题目列表。
                """, projectName, count, difficultyText, knowledgeInfo);
        
        try {
            // 使用 ChatClient 的结构化输出
            QuestionListDTO result = getChatClient()
                    .prompt()
                    .user(prompt)
                    .call()
                    .entity(QuestionListDTO.class);
            
            if (result == null || result.getQuestions() == null || result.getQuestions().isEmpty()) {
                throw new RuntimeException("AI 返回的题目列表为空");
            }
            
            log.info("题目生成成功，共 {} 道题", result.getQuestions().size());
            return JSONUtil.toJsonStr(result.getQuestions());
            
        } catch (Exception e) {
            log.error("结构化输出失败: {}", e.getMessage());
            throw new RuntimeException("题目生成失败: " + e.getMessage());
        }
    }
    
    /**
     * 收集知识信息（使用 Agent 和工具）
     */
    private String collectKnowledgeInfo(String projectName) {
        log.info("开始收集「{}」的知识信息", projectName);
        
        // 重置 Agent 状态
        setState(org.example.travel.agent.model.AgentState.IDLE);
        setCurrentStep(0);
        getMessageList().clear();
        
        String collectPrompt = String.format("""
                请收集关于「%s」的非遗知识信息。
                
                步骤：
                1. 使用 searchKnowledge 工具从知识库检索「%s」的详细信息
                2. 如果知识库信息不足，使用 searchWeb 工具联网搜索补充（搜索关键词：%s 非遗 历史 工艺 特点）
                3. 整理所有收集到的信息，调用 doTerminate 工具结束，将整理后的信息作为 finalAnswer
                
                要求：
                - 信息要全面，包括历史、工艺、特点、传承等方面
                - 信息要准确，基于检索到的真实内容
                - 整理成易于理解的文本格式
                """, projectName, projectName, projectName);
        
        // 执行 Agent 收集信息
        String result = run(collectPrompt);
        String knowledgeInfo = extractFinalAnswer(result);
        
        if (knowledgeInfo == null || knowledgeInfo.trim().isEmpty()) {
            log.warn("未能收集到知识信息，使用默认提示");
            return "关于" + projectName + "的基本信息";
        }
        
        log.info("知识信息收集完成，长度: {}", knowledgeInfo.length());
        return knowledgeInfo;
    }

    /**
     * 批量生成题目（多个非遗项目）
     * 线程安全：使用同步锁保证多用户并发调用时串行执行
     */
    public String generateMixedQuestions(List<String> projectNames, int difficulty, int countPerProject) {
        // 使用同步锁，保证多用户并发安全
        synchronized (lock) {
            log.info("批量生成题目: projects={}, difficulty={}, countPerProject={}",
                    projectNames, difficulty, countPerProject);

            String difficultyText = getDifficultyText(difficulty);
            String projectList = String.join("、", projectNames);
            int totalCount = projectNames.size() * countPerProject;

            // 第一步：收集所有项目的知识信息
            StringBuilder allKnowledgeInfo = new StringBuilder();
            for (String projectName : projectNames) {
                String info = collectKnowledgeInfo(projectName);
                allKnowledgeInfo.append("【").append(projectName).append("】\n");
                allKnowledgeInfo.append(info).append("\n\n");
            }

            // 第二步：使用 ChatClient 结构化输出生成题目
            String prompt = String.format("""
                    基于以下关于多个非遗项目的知识信息，生成 %d 道非遗知识题目。
                    涵盖项目：%s
                    每个项目生成 %d 道题，难度为「%s」。
                    
                    知识信息：
                    %s
                    
                    题目要求：
                    - 题目类型：单选题
                    - 每道题必须包含：题目文本、4个选项（A/B/C/D）、正确答案（只写字母）、答案解析、分值
                    - 题目要基于上述真实的非遗知识，不能编造
                    - 答案解析要简洁（不超过50字）
                    - 每道题分值为 10 分
                    - 选项格式：["A. 选项1", "B. 选项2", "C. 选项3", "D. 选项4"]
                    - 正确答案格式：只写字母，如 "A" 或 "B"
                    - 题目要均匀分布在各个项目上
                    
                    请严格按照 JSON 格式输出题目列表。
                    """, totalCount, projectList, countPerProject, difficultyText, allKnowledgeInfo.toString());

            try {
                // 使用 ChatClient 的结构化输出
                QuestionListDTO result = getChatClient()
                        .prompt()
                        .user(prompt)
                        .call()
                        .entity(QuestionListDTO.class);
                
                if (result == null || result.getQuestions() == null || result.getQuestions().isEmpty()) {
                    throw new RuntimeException("AI 返回的题目列表为空");
                }
                
                log.info("批量题目生成成功，共 {} 道题", result.getQuestions().size());
                return JSONUtil.toJsonStr(result.getQuestions());

            } catch (Exception e) {
                log.error("批量题目生成失败", e);
                throw new RuntimeException("批量题目生成失败: " + e.getMessage());
            }
        }
    }

    /**
     * 获取难度文本
     */
    private String getDifficultyText(int difficulty) {
        return switch (difficulty) {
            case 1 -> "简单";
            case 2 -> "中等";
            case 3 -> "困难";
            default -> "中等";
        };
    }

    /**
     * 提取最终答案
     */
    private String extractFinalAnswer(String agentResult) {
        // 1. 检查 lastFinalAnswer（当AI没有调用doTerminate但输出了完整回答时）
        String lastFinalAnswer = getLastFinalAnswer();
        if (lastFinalAnswer != null && !lastFinalAnswer.isEmpty()) {
            log.info("从 lastFinalAnswer 提取到最终答案");
            // 尝试从工具调用 JSON 中提取 finalAnswer
            String extracted = extractFromToolCallJson(lastFinalAnswer);
            return extracted != null ? extracted : lastFinalAnswer;
        }

        // 2. 从消息列表中获取最后一条助手消息
        List<org.springframework.ai.chat.messages.Message> messages = getMessageList();
        for (int i = messages.size() - 1; i >= 0; i--) {
            org.springframework.ai.chat.messages.Message msg = messages.get(i);
            if (msg instanceof org.springframework.ai.chat.messages.AssistantMessage) {
                String text = ((org.springframework.ai.chat.messages.AssistantMessage) msg).getText();
                if (text != null && !text.isEmpty()) {
                    log.info("从助手消息提取到最终答案");
                    String extracted = extractFromToolCallJson(text);
                    return extracted != null ? extracted : text;
                }
            }
        }

        // 3. 从工具响应消息中查找 doTerminate 的结果
        for (int i = messages.size() - 1; i >= 0; i--) {
            org.springframework.ai.chat.messages.Message msg = messages.get(i);
            if (msg instanceof org.springframework.ai.chat.messages.ToolResponseMessage) {
                org.springframework.ai.chat.messages.ToolResponseMessage toolMsg =
                        (org.springframework.ai.chat.messages.ToolResponseMessage) msg;
                for (org.springframework.ai.chat.messages.ToolResponseMessage.ToolResponse response : toolMsg.getResponses()) {
                    if ("doTerminate".equals(response.name())) {
                        String answer = response.responseData();
                        if (answer != null && !answer.isEmpty()) {
                            log.info("从 doTerminate 工具响应提取到最终答案");
                            return answer;
                        }
                    }
                }
            }
        }

        return agentResult;
    }

    /**
     * 从工具调用 JSON 中提取 finalAnswer
     * 处理格式: {"tool":"doTerminate","tool_input":{"finalAnswer":"[...]"}}
     */
    private String extractFromToolCallJson(String text) {
        if (text == null || !text.contains("\"tool\"") || !text.contains("\"finalAnswer\"")) {
            return null;
        }

        try {
            // 查找 "finalAnswer": 后面的内容
            int finalAnswerIndex = text.indexOf("\"finalAnswer\"");
            if (finalAnswerIndex == -1) {
                return null;
            }

            // 找到 finalAnswer 的值开始位置（跳过 "finalAnswer": 和可能的空格）
            int valueStart = text.indexOf(":", finalAnswerIndex) + 1;
            valueStart = text.indexOf("\"", valueStart) + 1; // 找到值的开始引号后的位置

            if (valueStart <= 0 || valueStart >= text.length()) {
                return null;
            }

            // 找到值的结束位置（需要处理转义的引号）
            int valueEnd = valueStart;
            while (valueEnd < text.length()) {
                if (text.charAt(valueEnd) == '"' && text.charAt(valueEnd - 1) != '\\') {
                    break;
                }
                valueEnd++;
            }

            if (valueEnd >= text.length()) {
                return null;
            }

            // 提取 finalAnswer 的值
            String finalAnswer = text.substring(valueStart, valueEnd);

            // 处理转义字符（JSON 中的 \" 需要转换为 "）
            finalAnswer = finalAnswer.replace("\\\"", "\"")
                    .replace("\\\\", "\\")
                    .replace("\\n", "\n")
                    .replace("\\r", "\r")
                    .replace("\\t", "\t");

            log.info("成功从工具调用 JSON 中提取 finalAnswer，长度: {}", finalAnswer.length());
            return finalAnswer;

        } catch (Exception e) {
            log.warn("从工具调用 JSON 提取 finalAnswer 失败: {}", e.getMessage());
            return null;
        }
    }

}
