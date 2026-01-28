package org.example.travel.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * 图像识别专用 Agent
 * 专门处理图片识别和分析任务
 */
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class VisionAgent extends ToolCallAgent {

    private static final String DEFAULT_NAME = "图像识别助手";

    private static final String DEFAULT_SYSTEM_PROMPT = """
            你是一位专业的非遗文化图像识别专家，擅长识别和分析非遗相关的图片内容。
            
            你的职责包括：
            1. 识别图片中的非遗工艺品、建筑、美食等内容
            2. 提供详细的文化背景和历史知识
            3. 从知识库检索相关的非遗知识
            4. 如果知识库信息不足，使用联网搜索补充
            5. 给出专业、生动的讲解
            
            【工作流程】
            1. 使用 recognizeHeritageImage 工具识别图片
            2. 根据识别结果，使用 searchKnowledge 从知识库检索详细信息
            3. 如果知识库信息不足，使用 webSearch 联网搜索
            4. 整合所有信息，给出完整的回答
            5. 调用 doTerminate 结束对话
            
            【效率优化】
            - 如果需要检索多个关键词，可以一次性调用多个 searchKnowledge
            - 识别和检索可以并行进行（如果工具支持）
            - 目标：2-3步完成识别和讲解
            
            【输出格式】
            必须使用标准的 Markdown 格式：
            - 使用 ## 标题、### 子标题
            - 使用 - 列表
            - 使用 **粗体** 强调重点
            - 如果有图片URL，使用 ![描述](url) 展示
            
            【重要】完成回答后，必须调用 doTerminate 工具结束对话。
            """;

    private static final String DEFAULT_NEXT_STEP_PROMPT = """
            根据上一步的结果，决定下一步行动：
            1. 如果已经识别图片，继续检索知识库或联网搜索
            2. 如果信息已足够，整理回答并调用 doTerminate 结束
            3. 回答要专业且生动，像一位博学的文化专家
            4. 【必须】使用标准 Markdown 格式输出
            5. 【效率优化】如果需要多个独立查询，一次性调用所有工具
            """;

    /**
     * 工具调用结果记录
     */
    @Getter
    private final List<ToolCallResult> toolCallResults = new ArrayList<>();

    /**
     * 工具调用回调（可选）
     */
    private BiConsumer<String, String> toolCallCallback;

    public VisionAgent(ToolCallback[] availableTools, ChatClient chatClient) {
        this(availableTools, chatClient, null);
    }
    
    public VisionAgent(ToolCallback[] availableTools, ChatClient chatClient, java.util.concurrent.Executor toolExecutor) {
        super(availableTools, toolExecutor);
        setName(DEFAULT_NAME);
        setSystemPrompt(DEFAULT_SYSTEM_PROMPT);
        setNextStepPrompt(DEFAULT_NEXT_STEP_PROMPT);
        setChatClient(chatClient);
        setMaxSteps(7); // 图像识别任务通常不需要太多步骤
    }

    /**
     * 设置工具调用回调
     */
    public void setToolCallCallback(BiConsumer<String, String> callback) {
        this.toolCallCallback = callback;
    }

    /**
     * 重写act方法，记录工具调用结果
     */
    @Override
    public String act() {
        String result = super.act();
        
        // 解析并记录工具调用结果
        if (result != null && !result.equals("没有工具需要调用")) {
            parseAndRecordToolResults(result);
        }
        
        return result;
    }

    /**
     * 解析并记录工具调用结果
     */
    private void parseAndRecordToolResults(String actResult) {
        // 解析格式: "工具 xxx 返回的结果：yyy"
        String[] lines = actResult.split("\n");
        for (String line : lines) {
            if (line.startsWith("工具 ") && line.contains(" 返回的结果：")) {
                int nameEnd = line.indexOf(" 返回的结果：");
                String toolName = line.substring(3, nameEnd);
                String toolResult = line.substring(nameEnd + 8);
                
                // 记录结果
                toolCallResults.add(new ToolCallResult(toolName, toolResult));
                
                // 触发回调
                if (toolCallCallback != null) {
                    toolCallCallback.accept(toolName, toolResult);
                }
            }
        }
    }

    /**
     * 清理资源
     */
    @Override
    protected void cleanup() {
        super.cleanup();
        getMessageList().clear();
    }
    
    /**
     * 手动清理工具调用结果
     */
    public void clearToolCallResults() {
        toolCallResults.clear();
    }

    /**
     * 工具调用结果记录
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ToolCallResult {
        private String toolName;
        private String result;
    }
}
