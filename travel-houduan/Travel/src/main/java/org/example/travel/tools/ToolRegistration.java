package org.example.travel.tools;

import jakarta.annotation.Resource;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 工具注册配置
 */
@Configuration
public class ToolRegistration {

    @Resource
    private WeatherTool weatherTool;

    @Resource
    private LocationTool locationTool;

    @Resource
    private IchProjectTool ichProjectTool;

    @Resource
    private MerchantTool merchantTool;

    @Resource
    private KnowledgeSearchTool knowledgeSearchTool;

    @Resource
    private PDFGenerationTool pdfGenerationTool;

    @Resource
    private RecommendTool recommendTool;

    @Resource
    private TerminateTool terminateTool;

    @Resource
    private BaiduMapTool baiduMapTool;

    @Bean
    public ToolCallback[] allTools() {
        List<ToolCallback> callbacks = new ArrayList<>();
        
        // 使用 MethodToolCallbackProvider 从带有 @Tool 注解的对象中提取工具
        Object[] tools = {
                weatherTool,
                locationTool,
                ichProjectTool,
                merchantTool,
                knowledgeSearchTool,
                pdfGenerationTool,
                recommendTool,
                terminateTool,
                baiduMapTool
        };
        
        for (Object tool : tools) {
            MethodToolCallbackProvider provider = MethodToolCallbackProvider.builder()
                    .toolObjects(tool)
                    .build();
            callbacks.addAll(Arrays.asList(provider.getToolCallbacks()));
        }
        
        return callbacks.toArray(new ToolCallback[0]);
    }
}
