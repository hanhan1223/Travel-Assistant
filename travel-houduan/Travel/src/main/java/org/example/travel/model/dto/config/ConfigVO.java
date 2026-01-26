package org.example.travel.model.dto.config;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 配置视图对象（脱敏）
 */
@Data
public class ConfigVO implements Serializable {

    private Long id;

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 配置值（敏感信息脱敏）
     */
    private String configValue;

    /**
     * 配置分组
     */
    private String configGroup;

    /**
     * 配置描述
     */
    private String description;

    /**
     * 是否加密
     */
    private Integer encrypted;

    /**
     * 是否启用
     */
    private Integer enabled;

    /**
     * 更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;
}
