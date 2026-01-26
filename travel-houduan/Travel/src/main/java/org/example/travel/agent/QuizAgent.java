package org.example.travel.agent;

import cn.hutool.json.JSONUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.core.ParameterizedTypeReference;
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
            
            工作流程：
            1. 使用 searchKnowledge 工具从知识库检索非遗项目的详细信息
            2. 基于检索到的真实信息立即生成题目
            3. 生成题目后，立即调用 doTerminate 工具结束，将题目作为 finalAnswer
            
            【重要】不要重复搜索！获取信息后立即生成题目并调用 doTerminate 结束。
            """;
    
    private static final String QUIZ_NEXT_STEP_PROMPT = """
            根据上一步的结果，决定下一步行动：
            1. 如果还没有检索知识库，调用 searchKnowledge 工具
            2. 如果已经获取了信息，立即生成题目并调用 doTerminate 结束
            3. 【禁止】不要重复搜索，不要超过 3 步
            4. 【必须】最终输出必须是标准的 JSON 数组格式
            """;
    
    public QuizAgent(ToolCallback[] availableTools, ChatModel chatModel, java.util.concurrent.Executor toolExecutor) {
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
            
            String difficultyText = getDifficultyText(difficulty);
            
            // 创建结构化输出转换器
            BeanOutputConverter<QuestionListDTO> outputConverter = 
                new BeanOutputConverter<>(QuestionListDTO.class);
            
            String format = outputConverter.getFormat();
            
            String userPrompt = String.format("""
                    请生成 %d 道关于「%s」的非遗知识题目，难度为「%s」。
                    
                    步骤：
                    1. 使用 searchKnowledge 工具检索「%s」的知识库信息
                    2. 基于检索到的真实信息生成题目
                    3. 生成题目后，调用 doTerminate 工具结束，将题目作为 finalAnswer
                    
                    题目要求：
                    - 题目类型：单选题
                    - 每道题包含：题目、4个选项（A/B/C/D）、正确答案、答案解析
                    - 题目要基于真实的非遗知识
                    - 答案解析要简洁（不超过50字）
                    - 每道题分值为 10 分
                    
                    %s
                    """, count, projectName, difficultyText, projectName, format);
            
            try {
                // 重置 Agent 状态（关键！）
                setState(org.example.travel.agent.model.AgentState.IDLE);
                setCurrentStep(0);
                
                // 清空之前的消息
                getMessageList().clear();
                
                // 执行 Agent
                String result = run(userPrompt);
                log.debug("Agent 执行结果: {}", result != null && result.length() > 200 ? result.substring(0, 200) + "..." : result);
                
                // 提取最终答案
                String finalAnswer = extractFinalAnswer(result);
                if (finalAnswer == null || finalAnswer.trim().isEmpty()) {
                    log.error("无法提取最终答案，Agent 结果: {}", result);
                    throw new RuntimeException("AI 未返回有效的答案内容");
                }
                log.debug("提取的最终答案: {}", finalAnswer.length() > 200 ? finalAnswer.substring(0, 200) + "..." : finalAnswer);
                
                // 使用结构化输出转换器解析
                QuestionListDTO questionList;
                try {
                    questionList = outputConverter.convert(finalAnswer);
                } catch (Exception parseEx) {
                    log.warn("结构化输出解析失败，尝试手动提取 JSON: {}", parseEx.getMessage());
                    // 如果结构化输出失败，尝试手动提取 JSON
                    String jsonContent = extractJson(finalAnswer);
                    questionList = JSONUtil.toBean(jsonContent, QuestionListDTO.class);
                }
                
                if (questionList == null || questionList.getQuestions() == null || questionList.getQuestions().isEmpty()) {
                    throw new RuntimeException("AI 返回的题目列表为空");
                }
                
                // 转换为 JSON 字符串
                String jsonResult = JSONUtil.toJsonStr(questionList.getQuestions());
                
                log.info("题目生成成功，共 {} 道题", questionList.getQuestions().size());
                
                return jsonResult;
                
            } catch (Exception e) {
                log.error("题目生成失败: {}", e.getMessage(), e);
                throw new RuntimeException("题目生成失败: " + e.getMessage());
            }
        }
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
            
            // 创建结构化输出转换器
            BeanOutputConverter<QuestionListDTO> outputConverter = 
                new BeanOutputConverter<>(QuestionListDTO.class);
            
            String format = outputConverter.getFormat();
            
            String userPrompt = String.format("""
                    请生成 %d 道非遗知识题目，涵盖以下项目：%s
                    每个项目生成 %d 道题，难度为「%s」。
                    
                    步骤：
                    1. 对每个项目使用 searchKnowledge 工具检索知识库信息
                    2. 基于真实信息生成题目
                    3. 生成完成后调用 doTerminate 工具结束
                    
                    %s
                    """, totalCount, projectList, countPerProject, difficultyText, format);
            
            try {
                // 重置 Agent 状态
                setState(org.example.travel.agent.model.AgentState.IDLE);
                setCurrentStep(0);
                getMessageList().clear();
                
                String result = run(userPrompt);
                String finalAnswer = extractFinalAnswer(result);
                
                QuestionListDTO questionList;
                try {
                    questionList = outputConverter.convert(finalAnswer);
                } catch (Exception parseEx) {
                    String jsonContent = extractJson(finalAnswer);
                    questionList = JSONUtil.toBean(jsonContent, QuestionListDTO.class);
                }
                
                return JSONUtil.toJsonStr(questionList.getQuestions());
                
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
    
    /**
     * 提取 JSON 内容
     */
    private String extractJson(String response) {
        if (response == null || response.trim().isEmpty()) {
            throw new RuntimeException("响应内容为空");
        }
        
        String content = response.trim();
        
        // 记录原始响应用于调试
        log.debug("原始响应内容: {}", content.length() > 500 ? content.substring(0, 500) + "..." : content);
        
        // 1. 去除 markdown 代码块标记
        if (content.startsWith("```json")) {
            content = content.substring(7);
        } else if (content.startsWith("```")) {
            content = content.substring(3);
        }
        
        if (content.endsWith("```")) {
            content = content.substring(0, content.length() - 3);
        }
        
        content = content.trim();
        
        // 2. 查找 JSON 数组的开始和结束位置
        int startIndex = content.indexOf('[');
        int endIndex = content.lastIndexOf(']');
        
        if (startIndex == -1 || endIndex == -1 || endIndex <= startIndex) {
            log.error("无法找到有效的 JSON 数组标记，响应内容: {}", content);
            throw new RuntimeException("响应内容不包含有效的 JSON 数组格式");
        }
        
        // 3. 提取 JSON 数组部分
        content = content.substring(startIndex, endIndex + 1);
        
        // 4. 清理可能的多余空白字符和换行
        content = content.replaceAll("\\s+", " ")
                        .replace("[ ", "[")
                        .replace(" ]", "]")
                        .replace("{ ", "{")
                        .replace(" }", "}")
                        .trim();
        
        // 5. 验证是否以 [ 开头和 ] 结尾
        if (!content.startsWith("[") || !content.endsWith("]")) {
            log.error("提取后的内容不是有效的 JSON 数组: {}", content.substring(0, Math.min(100, content.length())));
            throw new RuntimeException("提取的内容不是有效的 JSON 数组格式");
        }
        
        log.debug("提取的 JSON 内容: {}", content.length() > 500 ? content.substring(0, 500) + "..." : content);
        
        return content;
    }
}
