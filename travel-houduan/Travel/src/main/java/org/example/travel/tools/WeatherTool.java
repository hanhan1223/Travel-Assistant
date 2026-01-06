package org.example.travel.tools;

import jakarta.annotation.Resource;
import org.example.travel.service.EnvContextService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

/**
 * 天气查询工具
 */
@Component
public class WeatherTool {

    @Resource
    private EnvContextService envContextService;

    @Tool(description = "查询指定城市的实时天气信息，包括天气状况、温度、风向风力等")
    public String getWeather(
            @ToolParam(description = "城市名称，如：苏州、杭州、北京") String city
    ) {
        return envContextService.getWeather(city);
    }
}
