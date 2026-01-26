package org.example.travel.service;

import org.example.travel.model.dto.config.ConfigUpdateRequest;
import org.example.travel.model.dto.config.ConfigVO;

import java.util.List;
import java.util.Map;

/**
 * 系统配置服务
 */
public interface SystemConfigService {

    /**
     * 获取所有配置（按分组）
     */
    Map<String, List<ConfigVO>> getAllConfigs();

    /**
     * 获取指定分组的配置
     */
    List<ConfigVO> getConfigsByGroup(String group);

    /**
     * 获取配置值
     */
    String getConfigValue(String key);

    /**
     * 更新配置
     */
    boolean updateConfig(ConfigUpdateRequest request);

    /**
     * 批量更新配置
     */
    boolean batchUpdateConfigs(List<ConfigUpdateRequest> requests);

    /**
     * 重新加载配置到 Spring Environment
     */
    void reloadConfigs();

    /**
     * 测试配置连接（AI、数据库等）
     */
    Map<String, Object> testConnection(String configKey);
}
