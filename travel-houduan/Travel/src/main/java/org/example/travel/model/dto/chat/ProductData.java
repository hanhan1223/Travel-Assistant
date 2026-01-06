package org.example.travel.model.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 产品推荐数据结构
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductData {
    /**
     * 产品ID
     */
    private Long id;
    
    /**
     * 产品名称
     */
    private String name;
    
    /**
     * 店铺名称
     */
    private String shop;
    
    /**
     * 价格
     */
    private String price;
    
    /**
     * 产品图片URL
     */
    private String imageUrl;
    
    /**
     * 店铺纬度
     */
    private BigDecimal shopLat;
    
    /**
     * 店铺经度
     */
    private BigDecimal shopLng;
}
