package org.example.travel.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.travel.exception.BusinessException;
import org.example.travel.exception.ErrorCode;
import org.example.travel.mapper.SystemConfigMapper;
import org.example.travel.model.dto.config.ConfigUpdateRequest;
import org.example.travel.model.dto.config.ConfigVO;
import org.example.travel.model.entity.SystemConfig;
import org.example.travel.service.SystemConfigService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统配置服务实现
 */
@Service
@Slf4j
public class SystemConfigServiceImpl implements SystemConfigService {

    @Resource
    private SystemConfigMapper configMapper;

    @Resource
    private ConfigurableEnvironment environment;

    @Value("${config.encrypt.key:travel-system-key-2024-secure}")
    private String encryptKey;

    private static final String PROPERTY_SOURCE_NAME = "dynamicConfig";

    @Override
    public Map<String, List<ConfigVO>> getAllConfigs() {
        QueryWrapper<SystemConfig> wrapper = new QueryWrapper<>();
        wrapper.eq("enabled", 1);
        wrapper.orderByAsc("config_group", "config_key");

        List<SystemConfig> configs = configMapper.selectList(wrapper);

        return configs.stream()
                .map(this::toVO)
                .collect(Collectors.groupingBy(ConfigVO::getConfigGroup));
    }

    @Override
    public List<ConfigVO> getConfigsByGroup(String group) {
        QueryWrapper<SystemConfig> wrapper = new QueryWrapper<>();
        wrapper.eq("config_group", group);
        wrapper.eq("enabled", 1);
        wrapper.orderByAsc("config_key");

        List<SystemConfig> configs = configMapper.selectList(wrapper);

        return configs.stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    public String getConfigValue(String key) {
        QueryWrapper<SystemConfig> wrapper = new QueryWrapper<>();
        wrapper.eq("config_key", key);
        wrapper.eq("enabled", 1);

        SystemConfig config = configMapper.selectOne(wrapper);
        if (config == null) {
            return null;
        }

        // 解密
        if (config.getEncrypted() == 1) {
            return decrypt(config.getConfigValue());
        }

        return config.getConfigValue();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateConfig(ConfigUpdateRequest request) {
        if (StrUtil.isBlank(request.getConfigKey())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "配置键不能为空");
        }

        QueryWrapper<SystemConfig> wrapper = new QueryWrapper<>();
        wrapper.eq("config_key", request.getConfigKey());

        SystemConfig config = configMapper.selectOne(wrapper);
        if (config == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "配置不存在");
        }

        // 加密敏感配置
        String value = request.getConfigValue();
        if (config.getEncrypted() == 1 && StrUtil.isNotBlank(value)) {
            value = encrypt(value);
        }

        config.setConfigValue(value);
        if (StrUtil.isNotBlank(request.getDescription())) {
            config.setDescription(request.getDescription());
        }
        config.setUpdateTime(new Date());

        int result = configMapper.updateById(config);

        // 更新成功后重新加载配置
        if (result > 0) {
            reloadConfigs();
        }

        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchUpdateConfigs(List<ConfigUpdateRequest> requests) {
        for (ConfigUpdateRequest request : requests) {
            updateConfig(request);
        }
        return true;
    }

    @Override
    public void reloadConfigs() {
        log.info("重新加载系统配置到 Spring Environment");

        QueryWrapper<SystemConfig> wrapper = new QueryWrapper<>();
        wrapper.eq("enabled", 1);
        List<SystemConfig> configs = configMapper.selectList(wrapper);

        Map<String, Object> configMap = new HashMap<>();
        for (SystemConfig config : configs) {
            String value = config.getConfigValue();
            // 解密
            if (config.getEncrypted() == 1) {
                value = decrypt(value);
            }
            configMap.put(config.getConfigKey(), value);
        }

        // 移除旧的动态配置
        environment.getPropertySources().remove(PROPERTY_SOURCE_NAME);

        // 添加新的动态配置（优先级最高）
        environment.getPropertySources().addFirst(
                new MapPropertySource(PROPERTY_SOURCE_NAME, configMap)
        );

        log.info("配置重新加载完成，共 {} 项配置", configMap.size());
    }

    @Override
    public Map<String, Object> testConnection(String configKey) {
        Map<String, Object> result = new HashMap<>();
        result.put("configKey", configKey);
        result.put("success", false);

        try {
            // 根据配置键测试不同的连接
            if (configKey.contains("api-key")) {
                // 测试 AI API
                result.put("message", "AI API 连接测试功能待实现");
                result.put("success", true);
            } else if (configKey.contains("datasource")) {
                // 测试数据库连接
                result.put("message", "数据库连接测试功能待实现");
                result.put("success", true);
            } else {
                result.put("message", "不支持的配置类型");
            }
        } catch (Exception e) {
            result.put("message", "测试失败: " + e.getMessage());
            log.error("配置连接测试失败", e);
        }

        return result;
    }

    /**
     * 转换为 VO（脱敏）
     */
    private ConfigVO toVO(SystemConfig config) {
        ConfigVO vo = new ConfigVO();
        BeanUtil.copyProperties(config, vo);

        // 敏感信息脱敏
        if (config.getEncrypted() == 1 && StrUtil.isNotBlank(config.getConfigValue())) {
            String value = config.getConfigValue();
            if (value.length() > 8) {
                vo.setConfigValue(value.substring(0, 4) + "****" + value.substring(value.length() - 4));
            } else {
                vo.setConfigValue("****");
            }
        }

        return vo;
    }

    /**
     * 加密
     */
    private String encrypt(String value) {
        // 确保密钥长度为 16 字节（128位）
        byte[] keyBytes = getFixedLengthKey(encryptKey, 16);
        return SecureUtil.aes(keyBytes).encryptHex(value);
    }

    /**
     * 解密
     */
    private String decrypt(String encryptedValue) {
        try {
            // 确保密钥长度为 16 字节（128位）
            byte[] keyBytes = getFixedLengthKey(encryptKey, 16);
            return SecureUtil.aes(keyBytes).decryptStr(encryptedValue);
        } catch (Exception e) {
            log.error("配置解密失败", e);
            return encryptedValue;
        }
    }

    /**
     * 获取固定长度的密钥
     */
    private byte[] getFixedLengthKey(String key, int length) {
        byte[] keyBytes = key.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] result = new byte[length];
        
        if (keyBytes.length >= length) {
            // 如果密钥长度足够，直接截取
            System.arraycopy(keyBytes, 0, result, 0, length);
        } else {
            // 如果密钥长度不够，循环填充
            for (int i = 0; i < length; i++) {
                result[i] = keyBytes[i % keyBytes.length];
            }
        }
        
        return result;
    }
}
