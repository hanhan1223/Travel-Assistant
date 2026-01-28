package org.example.travel.tools;

import jakarta.annotation.Resource;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolRegistration {

    @Resource
    private BaiduMapTool baiduMapTool;
    
    @Resource
    private WeatherTool weatherTool;
    
    @Resource
    private TerminateTool terminateTool;
    
    @Resource
    private ImageRecognitionTool imageRecognitionTool;
    
    @Resource
    private KnowledgeSearchTool knowledgeSearchTool;
    
    @Resource
    private LocationTool locationTool;
    
    @Resource
    private MerchantTool merchantTool;
    
    @Resource
    private PDFGenerationTool pdfGenerationTool;
    
    @Resource
    private RecommendTool recommendTool;
    
    @Resource
    private WebSearchTool webSearchTool;

    @Bean
    public ToolCallback[] allTools() {
        return ToolCallbacks.from(
                baiduMapTool,
                weatherTool,
                terminateTool,
                imageRecognitionTool,
                knowledgeSearchTool,
                locationTool,
                merchantTool,
                pdfGenerationTool,
                recommendTool,
                webSearchTool
        );
    }
}
