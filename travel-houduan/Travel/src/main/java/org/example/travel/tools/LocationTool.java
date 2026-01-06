package org.example.travel.tools;

import jakarta.annotation.Resource;
import org.example.travel.service.EnvContextService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 位置信息工具
 */
@Component
public class LocationTool {

    @Resource
    private EnvContextService envContextService;

    @Tool(description = "根据经纬度坐标获取详细的地址信息，包括省市区街道等")
    public String getAddressByLocation(
            @ToolParam(description = "纬度坐标") BigDecimal lat,
            @ToolParam(description = "经度坐标") BigDecimal lng
    ) {
        return envContextService.reverseGeocode(lat, lng);
    }
}
