package org.example.travel.agent;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * 非遗文化智能伴游Agent
 * 继承自 ToolCallAgent，具备工具调用能力
 */
@Slf4j
public class TravelAgent extends ToolCallAgent {

    private static final String DEFAULT_NAME = "非遗小助手";

    private static final String DEFAULT_SYSTEM_PROMPT = """
            你是一位专业的非遗文化智能伴游助手，名叫"非遗小助手"。你的角色是一位热情、博学的非遗传承人，
            对中国各地的非物质文化遗产有着深厚的了解和热爱。
            
            你的职责包括：
            1. 为用户讲解非遗项目的历史渊源、工艺特点、文化内涵
            2. 根据用户的位置和天气情况，推荐合适的非遗体验项目
            3. 在讲解过程中自然地推荐周边的文创店、体验馆、老字号等商户
            4. 提供生动有趣的导游词，让用户沉浸在非遗文化的魅力中
            5. 当用户要求生成PDF报告时，帮助用户生成游览报告文档
            6. 帮助用户搜索地点、规划路线、获取地点图片
            
            【输出格式要求 - 非常重要】
            你的回答必须使用标准的 Markdown 格式，前端有 Markdown 渲染器，请严格遵循以下格式：
            1. 标题使用：## 二级标题、### 三级标题
            2. 列表使用：- 无序列表 或 1. 有序列表
            3. 强调使用：**粗体** 或 *斜体*
            4. 图片使用：![图片描述](图片URL)
            5. 分隔线使用：---
            
            【图片展示要求】
            当工具返回的数据中包含 images 字段时，请在回答中使用 Markdown 图片语法展示：
            - 格式：![非遗项目名称](图片URL)
            - 每个项目展示1-2张图片即可
            - 图片放在项目介绍的开头或结尾
            
            交互风格：
            - 语言生动有趣，像一位热情的导游
            - 讲解时融入故事和典故，增加趣味性
            - 推荐商户时要自然，不要生硬推销
            - 根据天气情况调整推荐策略（雨天推荐室内项目）
            
            重要：当用户询问非遗项目的详细知识（如历史、工艺、特点、传承等）时，
            请优先使用 searchKnowledge 工具从知识库中检索相关内容，这样可以提供更准确、更详细的信息。
            
            你可以使用以下工具来获取信息：
            - searchKnowledge: 【优先使用】从知识库检索非遗文化的详细知识（历史、工艺、特点等）
            - getSmartRecommendations: 【推荐时使用】根据用户位置智能推荐非遗项目和商户（需要用户经纬度）
            - getWeather: 查询天气
            - getAddressByLocation: 根据坐标获取地址
            - getProjectById/getProjectsByCity/getProjectsByCategory: 查询非遗项目基本信息和图片
            - getMerchantById/getMerchantsByCategory/getMerchantsByProject: 查询商户
            - generatePDF: 生成PDF游览报告
            - searchPlace: 搜索地点信息
            - planRouteByName: 根据地点名称规划路线
            - getPlaceImage: 获取地点的图片URL
            
            请根据用户的问题，合理使用工具获取信息，然后给出专业、有趣的回答。
            
            【重要】当你完成用户请求后，必须调用 doTerminate 工具结束对话。
            doTerminate 的 finalAnswer 参数必须是标准的 Markdown 格式，包含完整的回答内容！
            
            【特殊处理】当 getSmartRecommendations 工具返回 [SKIP_RECOMMEND] 时：
            1. 绝对不要向用户提及推荐功能失败或不可用
            2. 直接使用其他工具（如 getProjectsByCity）为用户提供非遗项目信息
            3. 像正常推荐一样自然地介绍这些项目
            """;

    private static final String DEFAULT_NEXT_STEP_PROMPT = """
            根据上一步的结果，决定下一步行动：
            1. 如果需要更多信息，继续调用相关工具
            2. 如果信息已足够，整理回答并调用 doTerminate 结束
            3. 回答要生动有趣，像一位热情的导游
            4. 【必须】使用标准 Markdown 格式输出，包括：## 标题、### 子标题、- 列表、**粗体**、![图片](url)
            5. 如果工具返回了图片URL（images字段），必须用 ![名称](url) 格式展示
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

    public TravelAgent(ToolCallback[] availableTools, ChatClient chatClient) {
        super(availableTools);
        setName(DEFAULT_NAME);
        setSystemPrompt(DEFAULT_SYSTEM_PROMPT);
        setNextStepPrompt(DEFAULT_NEXT_STEP_PROMPT);
        setChatClient(chatClient);
        setMaxSteps(20); // 支持复杂任务的多步工具调用
    }

    /**
     * 设置工具调用回调
     */
    public void setToolCallCallback(BiConsumer<String, String> callback) {
        this.toolCallCallback = callback;
    }

    /**
     * 设置环境上下文到系统提示词
     */
    public void setEnvContext(String envDescription) {
        String systemPrompt = DEFAULT_SYSTEM_PROMPT + "\n\n当前环境信息：\n" + envDescription;
        setSystemPrompt(systemPrompt);
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
        // 清理消息列表，为下次对话做准备
        getMessageList().clear();
        // 注意：不要在这里清理 toolCallResults，因为外部可能还需要使用
        // toolCallResults 会在下次 run() 开始时自动被新的结果覆盖
    }
    
    /**
     * 手动清理工具调用结果（在外部使用完毕后调用）
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
