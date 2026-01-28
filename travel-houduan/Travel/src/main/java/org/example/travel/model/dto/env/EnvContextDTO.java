package org.example.travel.model.dto.env;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 环境上下文DTO - 聚合地理位置、天气、人流等信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnvContextDTO {
    /**
     * 用户纬度
     */
    private BigDecimal lat;
    
    /**
     * 用户经度
     */
    private BigDecimal lng;
    
    /**
     * 城市名称
     */
    private String city;
    
    /**
     * 区域名称
     */
    private String district;
    
    /**
     * 详细地址
     */
    private String address;
    
    /**
     * 天气状况（晴/阴/雨/雪等）
     */
    private String weather;
    
    /**
     * 温度（摄氏度）
     */
    private Integer temperature;
    
    /**
     * 是否适合户外活动
     */
    private Boolean outdoorSuitable;
    
    /**
     * 环境描述（供AI使用）
     */
    private String description;
}
