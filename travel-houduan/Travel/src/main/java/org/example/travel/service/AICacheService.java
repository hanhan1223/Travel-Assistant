package org.example.travel.service;

import cn.hutool.crypto.digest.DigestUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.travel.constants.CacheConstants;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * AI 响应缓存服务
 */
@Service
@Slf4j
public class AICacheService {
    
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 生成缓存 key
     * 
     * @param userMessage 用户消息
     * @param systemPrompt 系统提示词
     * @param envContext 环境上下文（可选）
     * @return 缓存 key
     */
    public String generateCacheKey(String userMessage, String systemPrompt, String envContext) {
        // 组合所有影响 AI 响应的因素
        StringBuilder sb = new StringBuilder();
        sb.append(userMessage);
        sb.append("|");
        sb.append(systemPrompt);
        if (envContext != null && !envContext.isEmpty()) {
            sb.append("|");
            sb.append(envContext);
        }
        
        // 使用 MD5 生成短 key
        String hash = DigestUtil.md5Hex(sb.toString());
        return CacheConstants.AI_RESPONSE_PREFIX + hash;
    }
    
    /**
     * 获取缓存的 AI 响应
     * 
     * @param cacheKey 缓存 key
     * @return AI 响应，如果不存在则返回 null
     */
    public String getCachedResponse(String cacheKey) {
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                log.info("AI 响应缓存命中: {}", cacheKey);
                return cached.toString();
            }
            return null;
        } catch (Exception e) {
            log.error("获取 AI 响应缓存失败", e);
            return null;
        }
    }
    
    /**
     * 缓存 AI 响应
     * 
     * @param cacheKey 缓存 key
     * @param response AI 响应
     * @param ttl 过期时间（秒）
     */
    public void cacheResponse(String cacheKey, String response, long ttl) {
        try {
            redisTemplate.opsForValue().set(cacheKey, response, ttl, TimeUnit.SECONDS);
            log.info("AI 响应已缓存: {}, TTL: {}秒", cacheKey, ttl);
        } catch (Exception e) {
            log.error("缓存 AI 响应失败", e);
        }
    }
    
    /**
     * 判断是否为常见问题（可以使用更长的缓存时间）
     * 
     * @param userMessage 用户消息
     * @return 是否为常见问题
     */
    public boolean isCommonQuestion(String userMessage) {
        String lowerMessage = userMessage.toLowerCase();
        
        // 常见问题关键词
        String[] commonKeywords = {
            "有哪些", "介绍", "是什么", "怎么样",
            "推荐", "哪里", "什么时候", "如何",
            "历史", "特点", "文化", "传统"
        };
        
        for (String keyword : commonKeywords) {
            if (lowerMessage.contains(keyword)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 获取合适的 TTL
     * 
     * @param userMessage 用户消息
     * @return TTL（秒）
     */
    public long getAppropriateTTL(String userMessage) {
        if (isCommonQuestion(userMessage)) {
            // 24小时，转换为秒
            return CacheConstants.AI_RESPONSE_COMMON_TIMEOUT * 3600;
        } else {
            // 1小时，转换为秒
            return CacheConstants.AI_RESPONSE_TIMEOUT * 3600;
        }
    }
    
    /**
     * 判断是否应该缓存（某些实时性要求高的问题不缓存）
     * 
     * @param userMessage 用户消息
     * @return 是否应该缓存
     */
    public boolean shouldCache(String userMessage) {
        String lowerMessage = userMessage.toLowerCase();
        
        // 不缓存的关键词（实时性要求高）
        String[] noCacheKeywords = {
            "现在", "当前", "实时", "最新",
            "今天", "天气", "路线"
        };
        
        for (String keyword : noCacheKeywords) {
            if (lowerMessage.contains(keyword)) {
                return false;
            }
        }
        
        return true;
    }
}
