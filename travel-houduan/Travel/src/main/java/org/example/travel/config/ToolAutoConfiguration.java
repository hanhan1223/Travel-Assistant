package org.example.travel.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 工具自动配置
 * 让 Spring AI 自动扫描所有带 @Tool 注解的 Bean
 */
@Slf4j
@Configuration
public class ToolAutoConfiguration {

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 自动收集所有工具
     * Spring AI 会自动扫描所有 @Component Bean 中的 @Tool 方法
     */
    @Bean
    public ToolCallback[] allTools() {
        List<ToolCallback> callbacks = new ArrayList<>();
        
        // 方式1: 直接从 Spring 容器获取所有 ToolCallback Bean
        // Spring AI 会自动为每个 @Tool 方法创建 ToolCallback Bean
        Map<String, ToolCallback> toolCallbackBeans = applicationContext.getBeansOfType(ToolCallback.class);
        
        log.info("=== 自动扫描工具 ===");
        log.info("找到 {} 个 ToolCallback Bean", toolCallbackBeans.size());
        
        for (Map.Entry<String, ToolCallback> entry : toolCallbackBeans.entrySet()) {
            String beanName = entry.getKey();
            ToolCallback callback = entry.getValue();
            
            callbacks.add(callback);
            
            try {
                String toolName = callback.getToolDefinition().name();
                String description = callback.getToolDefinition().description();
                log.info("  ✓ 工具: {} (Bean: {})", toolName, beanName);
                log.info("    描述: {}", description.length() > 100 ? description.substring(0, 100) + "..." : description);
            } catch (Exception e) {
                log.warn("  ⚠ 无法获取工具信息: {}", beanName);
            }
        }
        
        log.info("=== 总共注册了 {} 个工具 ===", callbacks.size());
        
        return callbacks.toArray(new ToolCallback[0]);
    }
    
    @PostConstruct
    public void logToolScanInfo() {
        log.info("=== 工具自动配置已启用 ===");
        log.info("Spring AI 将自动扫描所有 @Component Bean 中的 @Tool 方法");
        log.info("包括手动工具和 MCP 工具");
    }
}
