package org.example.travel.constants;

import java.util.concurrent.TimeUnit;

/**
 * 缓存常量
 * 定义所有缓存的 key 前缀和过期时间
 */
public class CacheConstants {
    
    // ========== 非遗项目缓存 ==========
    public static final String ICH_PROJECT_PREFIX = "ich:project:";
    public static final String ICH_PROJECT_LIST_PREFIX = "ich:project:list:";
    public static final long ICH_PROJECT_TIMEOUT = 1; // 1小时
    public static final long ICH_PROJECT_LIST_TIMEOUT = 30; // 30分钟
    public static final TimeUnit ICH_PROJECT_UNIT = TimeUnit.HOURS;
    public static final TimeUnit ICH_PROJECT_LIST_UNIT = TimeUnit.MINUTES;
    
    // ========== 商户缓存 ==========
    public static final String MERCHANT_PREFIX = "merchant:";
    public static final String MERCHANT_NEARBY_PREFIX = "merchant:nearby:";
    public static final long MERCHANT_TIMEOUT = 2; // 2小时
    public static final long MERCHANT_NEARBY_TIMEOUT = 15; // 15分钟
    public static final TimeUnit MERCHANT_UNIT = TimeUnit.HOURS;
    public static final TimeUnit MERCHANT_NEARBY_UNIT = TimeUnit.MINUTES;
    
    // ========== 题目缓存 ==========
    public static final String QUIZ_QUESTION_PREFIX = "quiz:question:";
    public static final String QUIZ_QUESTIONS_PREFIX = "quiz:questions:";
    public static final long QUIZ_QUESTION_TIMEOUT = 1; // 1小时
    public static final long QUIZ_QUESTIONS_TIMEOUT = 24; // 24小时
    public static final TimeUnit QUIZ_QUESTION_UNIT = TimeUnit.HOURS;
    public static final TimeUnit QUIZ_QUESTIONS_UNIT = TimeUnit.HOURS;
    
    // ========== 环境上下文缓存 ==========
    public static final String ENV_WEATHER_PREFIX = "env:weather:";
    public static final String ENV_GEOCODE_PREFIX = "env:geocode:";
    public static final String ENV_CONTEXT_PREFIX = "env:context:";
    public static final long ENV_WEATHER_TIMEOUT = 15; // 15分钟
    public static final long ENV_GEOCODE_TIMEOUT = 1; // 1小时
    public static final long ENV_CONTEXT_TIMEOUT = 15; // 15分钟
    public static final TimeUnit ENV_WEATHER_UNIT = TimeUnit.MINUTES;
    public static final TimeUnit ENV_GEOCODE_UNIT = TimeUnit.HOURS;
    public static final TimeUnit ENV_CONTEXT_UNIT = TimeUnit.MINUTES;
    
    // ========== 用户缓存 ==========
    public static final String USER_PREFIX = "user:";
    public static final String USER_ACCOUNT_PREFIX = "user:account:";
    public static final long USER_TIMEOUT = 30; // 30分钟
    public static final TimeUnit USER_UNIT = TimeUnit.MINUTES;
    
    // ========== 知识库缓存 ==========
    public static final String KNOWLEDGE_SEARCH_PREFIX = "knowledge:search:";
    public static final long KNOWLEDGE_SEARCH_TIMEOUT = 10; // 10分钟
    public static final TimeUnit KNOWLEDGE_SEARCH_UNIT = TimeUnit.MINUTES;
    
    // ========== 推荐缓存 ==========
    public static final String RECOMMEND_PREFIX = "recommend:";
    public static final long RECOMMEND_TIMEOUT = 5; // 5分钟
    public static final TimeUnit RECOMMEND_UNIT = TimeUnit.MINUTES;

    
    /**
     * 构建缓存 key
     */
    public static String buildKey(String prefix, Object... params) {
        StringBuilder sb = new StringBuilder(prefix);
        for (Object param : params) {
            sb.append(param).append(":");
        }
        // 移除最后一个冒号
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ':') {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }
}
