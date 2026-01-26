package org.example.travel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.example.travel.common.BaseResponse;
import org.example.travel.common.Result;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 缓存监控控制器
 * 用于查看和管理 Redis 缓存
 */
@RestController
@RequestMapping("/monitor/cache")
@Tag(name = "缓存监控接口")
@Slf4j
public class CacheMonitorController {
    
    @Resource
    private CacheManager cacheManager;
    
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 获取所有缓存统计信息
     */
    @GetMapping("/stats")
    @Operation(summary = "获取缓存统计")
    public BaseResponse<Map<String, CacheStats>> getCacheStats() {
        Map<String, CacheStats> stats = new HashMap<>();
        
        Collection<String> cacheNames = cacheManager.getCacheNames();
        for (String cacheName : cacheNames) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                CacheStats cacheStats = new CacheStats();
                cacheStats.setCacheName(cacheName);
                
                // 获取缓存中的 key 数量
                Set<String> keys = redisTemplate.keys(cacheName + "::*");
                cacheStats.setKeyCount(keys != null ? keys.size() : 0);
                
                // 估算内存使用（简化版）
                if (keys != null && !keys.isEmpty()) {
                    long totalSize = 0;
                    for (String key : keys) {
                        Long size = redisTemplate.opsForValue().size(key);
                        if (size != null) {
                            totalSize += size;
                        }
                    }
                    cacheStats.setEstimatedSize(formatSize(totalSize));
                }
                
                stats.put(cacheName, cacheStats);
            }
        }
        
        return Result.success(stats);
    }
    
    /**
     * 获取指定缓存的所有 key
     */
    @GetMapping("/keys/{cacheName}")
    @Operation(summary = "获取缓存的所有key")
    public BaseResponse<List<String>> getCacheKeys(@PathVariable String cacheName) {
        Set<String> keys = redisTemplate.keys(cacheName + "::*");
        List<String> keyList = keys != null ? new ArrayList<>(keys) : new ArrayList<>();
        return Result.success(keyList);
    }
    
    /**
     * 清空指定缓存
     */
    @DeleteMapping("/clear/{cacheName}")
    @Operation(summary = "清空指定缓存")
    public BaseResponse<String> clearCache(@PathVariable String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
            log.info("缓存已清空: {}", cacheName);
            return Result.success("缓存已清空: " + cacheName);
        }
        return (BaseResponse<String>) Result.error(404, "缓存不存在: " + cacheName);
    }
    
    /**
     * 清空所有缓存
     */
    @DeleteMapping("/clear-all")
    @Operation(summary = "清空所有缓存")
    public BaseResponse<String> clearAllCache() {
        Collection<String> cacheNames = cacheManager.getCacheNames();
        int count = 0;
        for (String cacheName : cacheNames) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                cache.clear();
                count++;
            }
        }
        log.info("已清空 {} 个缓存", count);
        return Result.success("已清空 " + count + " 个缓存");
    }
    
    /**
     * 删除指定缓存的某个 key
     */
    @DeleteMapping("/evict/{cacheName}/{key}")
    @Operation(summary = "删除指定缓存的某个key")
    public BaseResponse<String> evictCacheKey(
            @PathVariable String cacheName,
            @PathVariable String key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
            log.info("缓存key已删除: {}::{}", cacheName, key);
            return Result.success("缓存key已删除");
        }
        return (BaseResponse<String>) Result.error(404, "缓存不存在: " + cacheName);
    }
    
    /**
     * 获取 Redis 服务器信息
     */
    @GetMapping("/redis-info")
    @Operation(summary = "获取Redis服务器信息")
    public BaseResponse<Map<String, Object>> getRedisInfo() {
        Map<String, Object> info = new HashMap<>();
        
        try {
            // 获取 Redis 信息
            Properties properties = redisTemplate.getConnectionFactory()
                    .getConnection()
                    .info();
            
            if (properties != null) {
                // 内存使用
                info.put("usedMemory", properties.getProperty("used_memory_human"));
                info.put("maxMemory", properties.getProperty("maxmemory_human"));
                
                // 连接数
                info.put("connectedClients", properties.getProperty("connected_clients"));
                
                // 命中率
                String hits = properties.getProperty("keyspace_hits");
                String misses = properties.getProperty("keyspace_misses");
                if (hits != null && misses != null) {
                    long hitsCount = Long.parseLong(hits);
                    long missesCount = Long.parseLong(misses);
                    long total = hitsCount + missesCount;
                    double hitRate = total > 0 ? (double) hitsCount / total * 100 : 0;
                    info.put("hitRate", String.format("%.2f%%", hitRate));
                    info.put("hits", hitsCount);
                    info.put("misses", missesCount);
                }
                
                // 总key数
                info.put("totalKeys", properties.getProperty("db0"));
            }
        } catch (Exception e) {
            log.error("获取Redis信息失败", e);
            return (BaseResponse<Map<String, Object>>) Result.error(500, "获取Redis信息失败: " + e.getMessage());
        }
        
        return Result.success(info);
    }
    
    /**
     * 预热缓存（可选）
     */
    @PostMapping("/warmup")
    @Operation(summary = "预热缓存")
    public BaseResponse<String> warmupCache() {
        // TODO: 实现缓存预热逻辑
        // 例如：预加载热门非遗项目、商户等
        log.info("缓存预热开始");
        
        // 示例：预加载前100个非遗项目
        // List<IchProject> projects = ichProjectService.list(new Page<>(1, 100));
        // for (IchProject project : projects) {
        //     ichProjectService.getById(project.getId()); // 触发缓存
        // }
        
        log.info("缓存预热完成");
        return Result.success("缓存预热完成");
    }
    
    /**
     * 格式化字节大小
     */
    private String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
    
    /**
     * 缓存统计信息
     */
    @Data
    public static class CacheStats {
        private String cacheName;
        private int keyCount;
        private String estimatedSize;
    }
}
