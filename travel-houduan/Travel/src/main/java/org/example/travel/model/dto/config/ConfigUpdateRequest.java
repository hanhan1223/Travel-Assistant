package org.example.travel.model.dto.config;

import lombok.Data;

import java.io.Serializable;

/**
 * 配置更新请求
 */
@Data
public class ConfigUpdateRequest implements Serializable {

    /**
     * 配置键
     */
    private String configKey;

    /**
     * 配置值
     */
    private String configValue;

    /**
     * 配置描述
     */
    private String description;

    private static final long serialVersionUID = 1L;
}
