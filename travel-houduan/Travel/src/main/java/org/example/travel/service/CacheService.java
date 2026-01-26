package org.example.travel.service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 缓存服务接口
 * 统一管理 Redis 缓存操作
 */
public interface CacheService {
    
    /**
     * 获取缓存，如果不存在则执行 loader 并缓存结果
     * 
     * @param key 缓存key
     * @param loader 数据加载器
     * @param timeout 过期时间
     * @param unit 时间单位
     * @param clazz 返回类型
     * @return 缓存数据
     */
    <T> T get(String key, Supplier<T> loader, long timeout, TimeUnit unit, Class<T> clazz);
    
    /**
     * 获取列表缓存
     */
    <T> List<T> getList(String key, Supplier<List<T>> loader, long timeout, TimeUnit unit, Class<T> clazz);
    
    /**
     * 设置缓存（对象）
     */
    <T> void set(String key, T value, long timeout, TimeUnit unit);
    
    /**
     * 设置缓存（字符串）
     */
    void setString(String key, String value, long timeout, TimeUnit unit);
    
    /**
     * 删除缓存
     */
    void delete(String key);
    
    /**
     * 删除匹配的缓存
     */
    void deletePattern(String pattern);
    
    /**
     * 判断缓存是否存在
     */
    boolean exists(String key);
    
    /**
     * 设置过期时间
     */
    boolean expire(String key, long timeout, TimeUnit unit);
    
    /**
     * 获取剩余过期时间（秒）
     */
    Long getExpire(String key);
}
