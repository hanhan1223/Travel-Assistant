package org.example.travel.model.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 地点推荐数据结构
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationData {
    /**
     * 地点ID
     */
    private Long id;
    
    /**
     * 地点类型：project/merchant
     */
    private String type;
    
    /**
     * 地点名称
     */
    private String name;
    
    /**
     * 详细地址
     */
    private String address;
    
    /**
     * 纬度（前端用于生成地图）
     */
    private BigDecimal lat;
    
    /**
     * 经度（前端用于生成地图）
     */
    private BigDecimal lng;
    
    /**
     * 联系电话
     */
    private String phone;
    
    /**
     * 评分
     */
    private BigDecimal rating;
    
    /**
     * 距离用户的距离
     */
    private String distance;
    
    /**
     * 图片URL列表（COS图片）
     */
    private List<String> images;
    
    /**
     * 类别
     */
    private String category;
    
    /**
     * 简介
     */
    private String description;
}
