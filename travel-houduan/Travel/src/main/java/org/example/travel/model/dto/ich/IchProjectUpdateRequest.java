package org.example.travel.model.dto.ich;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 非遗项目更新请求DTO
 */
@Data
public class IchProjectUpdateRequest {
    /**
     * 非遗名称
     */
    private String name;

    /**
     * 非遗类别（手作/戏曲等）
     */
    private String category;

    /**
     * 非遗简介
     */
    private String description;

    /**
     * 所属城市
     */
    private String city;

    /**
     * 纬度
     */
    private BigDecimal lat;

    /**
     * 经度
     */
    private BigDecimal lng;

    /**
     * 是否室内（雨天推荐依据）：0-否，1-是
     */
    private Integer isIndoor;

    /**
     * 开放状态
     */
    private String openStatus;

    /**
     * 关联的商户ID列表（会覆盖原有关联）
     */
    private List<Long> merchantIds;
}
