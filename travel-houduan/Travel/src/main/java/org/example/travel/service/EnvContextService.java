package org.example.travel.service;

import org.example.travel.model.dto.env.EnvContextDTO;
import java.math.BigDecimal;

/**
 * 环境上下文服务接口
 */
public interface EnvContextService {
    
    /**
     * 获取环境上下文信息
     * @param lat 纬度
     * @param lng 经度
     * @return 环境上下文
     */
    EnvContextDTO getEnvContext(BigDecimal lat, BigDecimal lng);
    
    /**
     * 获取天气信息
     * @param city 城市名称
     * @return 天气描述
     */
    String getWeather(String city);
    
    /**
     * 逆地理编码（坐标转地址）
     * @param lat 纬度
     * @param lng 经度
     * @return 地址信息
     */
    String reverseGeocode(BigDecimal lat, BigDecimal lng);
}
