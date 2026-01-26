package org.example.travel.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.travel.service.CacheService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 缓存服务实现
 * 基于 RedisTemplate 的缓存操作
 */
@Service
@Slf4j
public class CacheServiceImpl implements CacheService {
    
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    @Resource
    private ObjectMapper objectMapper;
    
    @Override
    public <T> T get(String key, Supplier<T> loader, long timeout, TimeUnit unit, Class<T> clazz) {
        try {
            // 1. 尝试从缓存获取
            Object cached = redisTemplate.opsForValue().get(key);
            
            if (cached != null) {
                log.debug("缓存命中: {}", key);
                // 如果是目标类型，直接返回
                if (clazz.isInstance(cached)) {
                    return clazz.cast(cached);
                }
                // 否则尝试转换
                return objectMapper.convertValue(cached, clazz);
            }
            
            // 2. 缓存未命中，执行 loader
            log.debug("缓存未命中，加载数据: {}", key);
            T value = loader.get();
            
            // 3. 如果结果不为 null，存入缓存
            if (value != null) {
                redisTemplate.opsForValue().set(key, value, timeout, unit);
                log.debug("数据已缓存: {}, 过期时间: {} {}", key, timeout, unit);
            }
            
            return value;
            
        } catch (Exception e) {
            log.error("缓存操作失败: {}", key, e);
            // 缓存失败时直接执行 loader
            return loader.get();
        }
    }
    
    @Override
    public <T> List<T> getList(String key, Supplier<List<T>> loader, long timeout, TimeUnit unit, Class<T> clazz) {
        try {
            // 1. 尝试从缓存获取
            Object cached = redisTemplate.opsForValue().get(key);
            
            if (cached != null) {
                log.debug("缓存命中: {}", key);
                // 转换为 List
                return objectMapper.convertValue(cached, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
            }
            
            // 2. 缓存未命中，执行 loader
            log.debug("缓存未命中，加载数据: {}", key);
            List<T> value = loader.get();
            
            // 3. 如果结果不为 null 且不为空，存入缓存
            if (value != null && !value.isEmpty()) {
                redisTemplate.opsForValue().set(key, value, timeout, unit);
                log.debug("数据已缓存: {}, 过期时间: {} {}", key, timeout, unit);
            }
            
            return value;
            
        } catch (Exception e) {
            log.error("缓存操作失败: {}", key, e);
            // 缓存失败时直接执行 loader
            return loader.get();
        }
    }
    
    @Override
    public <T> void set(String key, T value, long timeout, TimeUnit unit) {
        try {
            if (value != null) {
                redisTemplate.opsForValue().set(key, value, timeout, unit);
                log.debug("缓存已设置: {}, 过期时间: {} {}", key, timeout, unit);
            }
        } catch (Exception e) {
            log.error("设置缓存失败: {}", key, e);
        }
    }
    
    @Override
    public void setString(String key, String value, long timeout, TimeUnit unit) {
        try {
            if (value != null) {
                redisTemplate.opsForValue().set(key, value, timeout, unit);
                log.debug("缓存已设置: {}, 过期时间: {} {}", key, timeout, unit);
            }
        } catch (Exception e) {
            log.error("设置缓存失败: {}", key, e);
        }
    }
    
    @Override
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("缓存已删除: {}", key);
        } catch (Exception e) {
            log.error("删除缓存失败: {}", key, e);
        }
    }
    
    @Override
    public void deletePattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("批量删除缓存: {}, 数量: {}", pattern, keys.size());
            }
        } catch (Exception e) {
            log.error("批量删除缓存失败: {}", pattern, e);
        }
    }
    
    @Override
    public boolean exists(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return exists != null && exists;
        } catch (Exception e) {
            log.error("检查缓存存在失败: {}", key, e);
            return false;
        }
    }
    
    @Override
    public boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            Boolean result = redisTemplate.expire(key, timeout, unit);
            return result != null && result;
        } catch (Exception e) {
            log.error("设置过期时间失败: {}", key, e);
            return false;
        }
    }
    
    @Override
    public Long getExpire(String key) {
        try {
            return redisTemplate.getExpire(key, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("获取过期时间失败: {}", key, e);
            return null;
        }
    }
}
