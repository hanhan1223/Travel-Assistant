package org.example.travel.model.dto.merchant;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商户新增请求DTO（不包含projectId，由非遗项目端关联）
 */
@Data
public class MerchantAddRequest {
    /**
     * 商户名称
     */
    private String name;

    /**
     * 商户类型（文创店/体验馆/老字号/餐饮）
     */
    private String category;

    /**
     * 纬度
     */
    private BigDecimal lat;

    /**
     * 经度
     */
    private BigDecimal lng;

    /**
     * 评分
     */
    private BigDecimal rating;

    /**
     * 第三方平台链接
     */
    private String thirdPartyUrl;
}
