package org.example.travel.model.dto.recommend;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 推荐请求DTO - 发送给Python算法服务
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendRequest {
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户纬度
     */
    private BigDecimal lat;
    
    /**
     * 用户经度
     */
    private BigDecimal lng;
    
    /**
     * 用户兴趣标签
     */
    private List<String> interestTags;
    
    /**
     * 当前天气
     */
    private String weather;
    
    /**
     * 是否适合户外
     */
    private Boolean outdoorSuitable;
    
    /**
     * 用户意图向量（可选）
     */
    private List<Float> intentVector;
    
    /**
     * 最大推荐数量
     */
    private Integer limit;
}
