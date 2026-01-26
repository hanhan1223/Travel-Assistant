package org.example.travel.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis 缓存配置
 * 为不同类型的数据设置不同的缓存策略
 */
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        // 默认缓存配置
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // 默认过期时间30分钟
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues(); // 不缓存 null 值
        
        // 针对不同缓存设置不同过期时间
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // ========== 非遗项目缓存 ==========
        // 单个项目：1小时（变化频率低）
        cacheConfigurations.put("ich:project", 
                defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // 项目列表：30分钟
        cacheConfigurations.put("ich:project:list", 
                defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // ========== 商户缓存 ==========
        // 单个商户：2小时（变化频率很低）
        cacheConfigurations.put("merchant", 
                defaultConfig.entryTtl(Duration.ofHours(2)));
        
        // 附近商户：15分钟（位置相关，更新频繁）
        cacheConfigurations.put("merchant:nearby", 
                defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // ========== 题目缓存 ==========
        // 题目列表：24小时（AI生成成本高，尽量复用）
        cacheConfigurations.put("quiz:questions", 
                defaultConfig.entryTtl(Duration.ofHours(24)));
        
        // 单个题目：1小时
        cacheConfigurations.put("quiz:question", 
                defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // ========== 环境上下文缓存 ==========
        // 天气信息：15分钟（天气变化较快）
        cacheConfigurations.put("env:weather", 
                defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // 逆地理编码：1小时（位置固定不变）
        cacheConfigurations.put("env:geocode", 
                defaultConfig.entryTtl(Duration.ofHours(1)));
        
        // 环境上下文：15分钟
        cacheConfigurations.put("env:context", 
                defaultConfig.entryTtl(Duration.ofMinutes(15)));
        
        // ========== 用户缓存 ==========
        // 用户信息：30分钟
        cacheConfigurations.put("user", 
                defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // 用户账号查询：30分钟
        cacheConfigurations.put("user:account", 
                defaultConfig.entryTtl(Duration.ofMinutes(30)));
        
        // ========== 知识库缓存 ==========
        // 知识检索结果：10分钟（向量检索成本高）
        cacheConfigurations.put("knowledge:search", 
                defaultConfig.entryTtl(Duration.ofMinutes(10)));
        
        // ========== 推荐缓存 ==========
        // 推荐结果：5分钟（实时性要求高）
        cacheConfigurations.put("recommend", 
                defaultConfig.entryTtl(Duration.ofMinutes(5)));
        
        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware() // 支持事务
                .build();
    }
}
