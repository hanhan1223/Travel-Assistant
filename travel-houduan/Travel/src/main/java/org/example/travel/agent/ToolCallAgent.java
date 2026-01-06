package org.example.travel.agent;

import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.example.travel.agent.model.AgentState;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class ToolCallAgent extends ReActAgent{

    // 可用的工具
    private final ToolCallback[] availableTools;

    // 保存工具调用信息的响应结果（要调用那些工具）
    private ChatResponse toolCallChatResponse;

    // 禁用 Spring AI 内置的工具调用机制，自己维护选项和消息上下文
    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools) {
        super();
        this.availableTools = availableTools;
        // 禁用 Spring AI 内置的工具调用机制，自己维护选项和消息上下文
        this.chatOptions = DashScopeChatOptions.builder()
                .withInternalToolExecutionEnabled(false)
                .build();
    }

    /**
     * 处理当前状态并决定下一步行动
     *
     * @return 是否需要执行行动
     */
    @Override
    public boolean think() {
        // 1、校验提示词，拼接用户提示词
        if (StrUtil.isNotBlank(getNextStepPrompt())) {
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);
        }
        // 2、调用 AI 大模型，获取工具调用结果
        List<Message> messageList = getMessageList();
        Prompt prompt = new Prompt(messageList, this.chatOptions);
        try {
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .toolCallbacks(availableTools)
                    .call()
                    .chatResponse();
            // 记录响应，用于等下 Act
            this.toolCallChatResponse = chatResponse;
            // 3、解析工具调用结果，获取要调用的工具
            // 助手消息
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            // 获取要调用的工具列表
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            // 输出提示信息
            String result = assistantMessage.getText();
            log.info(getName() + "的思考：" + result);
            log.info(getName() + "选择了 " + toolCallList.size() + " 个工具来使用");
            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("工具名称：%s，参数：%s", toolCall.name(), toolCall.arguments()))
                    .collect(Collectors.joining("\n"));
            log.info(toolCallInfo);
            // 如果不需要调用工具，返回 false
            if (toolCallList.isEmpty()) {
                // 只有不调用工具时，才需要手动记录助手消息
                getMessageList().add(assistantMessage);
                
                // 如果AI输出了实质性内容但没有调用工具，说明它认为任务已完成
                // 自动结束对话，避免无限循环
                if (StrUtil.isNotBlank(result) && result.length() > 50) {
                    log.info("AI输出了完整回答但未调用doTerminate，自动结束对话");
                    setState(AgentState.FINISHED);
                    // 记录最终回答，供后续提取
                    this.lastFinalAnswer = result;
                }
                return false;
            } else {
                // 需要调用工具时，无需记录助手消息，因为调用工具时会自动记录
                return true;
            }
        } catch (Exception e) {
            log.error(getName() + "的思考过程遇到了问题：" + e.getMessage());
            getMessageList().add(new AssistantMessage("处理时遇到了错误：" + e.getMessage()));
            return false;
        }
    }
    
    // 保存最终回答（当AI没有调用doTerminate但输出了完整回答时）
    private String lastFinalAnswer;
    
    public String getLastFinalAnswer() {
        return lastFinalAnswer;
    }

    /**
     * 执行工具调用并处理结果
     *
     * @return 执行结果
     */
    @Override
    public String act() {
        if (!toolCallChatResponse.hasToolCalls()) {
            return "没有工具需要调用";
        }
        // 手动处理工具调用
        AssistantMessage assistantMessage = toolCallChatResponse.getResult().getOutput();
        getMessageList().add(assistantMessage);
        
        // 创建工具响应消息
        List<ToolResponseMessage.ToolResponse> responses = new java.util.ArrayList<>();
        for (AssistantMessage.ToolCall toolCall : assistantMessage.getToolCalls()) {
            // 查找对应的工具并执行
            for (ToolCallback tool : availableTools) {
                if (tool.getToolDefinition().name().equals(toolCall.name())) {
                    try {
                        String result = tool.call(toolCall.arguments());
                        responses.add(new ToolResponseMessage.ToolResponse(toolCall.id(), toolCall.name(), result));
                    } catch (Exception e) {
                        log.error("工具调用失败: " + toolCall.name(), e);
                        // 对doTerminate工具的错误做特殊处理，尝试从参数中提取finalAnswer
                        if ("doTerminate".equals(toolCall.name())) {
                            String fallbackAnswer = extractFinalAnswerFromArgs(toolCall.arguments());
                            responses.add(new ToolResponseMessage.ToolResponse(toolCall.id(), toolCall.name(), fallbackAnswer));
                        } else {
                            responses.add(new ToolResponseMessage.ToolResponse(toolCall.id(), toolCall.name(), "工具调用出错，请稍后再试"));
                        }
                    }
                    break;
                }
            }
        }
        
        ToolResponseMessage toolResponseMessage = new ToolResponseMessage(responses, java.util.Map.of());
        getMessageList().add(toolResponseMessage);
        
        // 判断是否调用了终止工具
        boolean terminateToolCalled = responses.stream()
                .anyMatch(response -> response.name().equals("doTerminate"));
        if (terminateToolCalled) {
            // 任务结束，更改状态
            setState(AgentState.FINISHED);
        }
        String results = responses.stream()
                .map(response -> "工具 " + response.name() + " 返回的结果：" + response.responseData())
                .collect(Collectors.joining("\n"));
        log.info(results);
        return results;
    }
    
    /**
     * 从doTerminate工具的参数中提取finalAnswer
     * 当工具调用因JSON解析失败时，尝试直接从参数字符串中提取
     */
    private String extractFinalAnswerFromArgs(String arguments) {
        if (StrUtil.isBlank(arguments)) {
            return "抱歉，处理您的请求时遇到了问题，请稍后再试。";
        }
        try {
            // 尝试提取finalAnswer字段的值
            // 参数格式可能是 {"finalAnswer": "xxx"} 或包含换行符的复杂格式
            String cleaned = arguments.replace("\\n", " ").replace("\n", " ");
            int startIndex = cleaned.indexOf("\"finalAnswer\"");
            if (startIndex == -1) {
                startIndex = cleaned.indexOf("'finalAnswer'");
            }
            if (startIndex != -1) {
                // 找到冒号后的值
                int colonIndex = cleaned.indexOf(":", startIndex);
                if (colonIndex != -1) {
                    // 找到值的开始引号
                    int valueStart = cleaned.indexOf("\"", colonIndex);
                    if (valueStart != -1) {
                        // 找到值的结束引号（考虑转义）
                        int valueEnd = valueStart + 1;
                        while (valueEnd < cleaned.length()) {
                            if (cleaned.charAt(valueEnd) == '"' && cleaned.charAt(valueEnd - 1) != '\\') {
                                break;
                            }
                            valueEnd++;
                        }
                        if (valueEnd < cleaned.length()) {
                            return cleaned.substring(valueStart + 1, valueEnd).replace("\\\"", "\"");
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("从参数中提取finalAnswer失败: {}", e.getMessage());
        }
        return "抱歉，处理您的请求时遇到了问题，请稍后再试。";
    }
}
