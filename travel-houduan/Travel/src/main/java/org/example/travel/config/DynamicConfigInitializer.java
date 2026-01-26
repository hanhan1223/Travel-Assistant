package org.example.travel.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.travel.service.SystemConfigService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 动态配置初始化器
 * 在应用启动时自动加载数据库中的配置
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "config.dynamic.enabled", havingValue = "true", matchIfMissing = true)
public class DynamicConfigInitializer {

    @Resource
    private SystemConfigService configService;

    @PostConstruct
    public void init() {
        try {
            log.info("开始加载动态配置...");
            configService.reloadConfigs();
            log.info("✅ 动态配置加载完成，系统将优先使用数据库配置");
        } catch (Exception e) {
            log.warn("⚠️ 动态配置加载失败，将使用 application.yml 中的配置: {}", e.getMessage());
            log.info("提示：如果是首次启动，请执行 SQL 脚本创建配置表");
        }
    }
}
