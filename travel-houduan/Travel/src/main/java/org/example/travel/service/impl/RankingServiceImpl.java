package org.example.travel.service.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.travel.model.dto.quiz.RankingDTO;
import org.example.travel.model.entity.User;
import org.example.travel.service.RankingService;
import org.example.travel.service.UserService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 排行榜服务实现
 * 使用 Redis ZSet 实现高性能排行榜
 */
@Slf4j
@Service
public class RankingServiceImpl implements RankingService {
    
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    @Resource
    private UserService userService;
    
    // Redis Key 常量
    private static final String RANKING_TOTAL_KEY = "quiz:ranking:total";
    private static final String RANKING_WEEKLY_KEY = "quiz:ranking:weekly";
    private static final String RANKING_MONTHLY_KEY = "quiz:ranking:monthly";
    
    @Override
    public void updateUserScore(Long userId, Integer points) {
        try {
            // 更新总排行榜（累加积分）
            redisTemplate.opsForZSet().incrementScore(RANKING_TOTAL_KEY, userId.toString(), points);
            
            // 更新周排行榜（累加积分）
            redisTemplate.opsForZSet().incrementScore(RANKING_WEEKLY_KEY, userId.toString(), points);
            
            // 更新月排行榜（累加积分）
            redisTemplate.opsForZSet().incrementScore(RANKING_MONTHLY_KEY, userId.toString(), points);
            
            log.info("更新用户排行榜: userId={}, points={}", userId, points);
            
        } catch (Exception e) {
            log.error("更新排行榜失败", e);
        }
    }
    
    @Override
    public List<RankingDTO> getPointsRanking(int topN) {
        return getRanking(RANKING_TOTAL_KEY, topN);
    }
    
    @Override
    public List<RankingDTO> getWeeklyRanking(int topN) {
        return getRanking(RANKING_WEEKLY_KEY, topN);
    }
    
    @Override
    public List<RankingDTO> getMonthlyRanking(int topN) {
        return getRanking(RANKING_MONTHLY_KEY, topN);
    }
    
    @Override
    public Long getUserRank(Long userId) {
        try {
            // ZSet 的 reverseRank 返回的是从大到小的排名（0开始）
            Long rank = redisTemplate.opsForZSet().reverseRank(RANKING_TOTAL_KEY, userId.toString());
            return rank != null ? rank + 1 : null; // 转换为从1开始的排名
        } catch (Exception e) {
            log.error("获取用户排名失败", e);
            return null;
        }
    }
    
    @Override
    public void clearWeeklyRanking() {
        try {
            redisTemplate.delete(RANKING_WEEKLY_KEY);
            log.info("清空周排行榜成功");
        } catch (Exception e) {
            log.error("清空周排行榜失败", e);
        }
    }
    
    @Override
    public void clearMonthlyRanking() {
        try {
            redisTemplate.delete(RANKING_MONTHLY_KEY);
            log.info("清空月排行榜成功");
        } catch (Exception e) {
            log.error("清空月排行榜失败", e);
        }
    }
    
    /**
     * 通用排行榜查询方法
     */
    private List<RankingDTO> getRanking(String key, int topN) {
        List<RankingDTO> rankings = new ArrayList<>();
        
        try {
            // 获取前 topN 名（按分数从高到低）
            Set<ZSetOperations.TypedTuple<Object>> tuples = 
                    redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, topN - 1);
            
            if (tuples == null || tuples.isEmpty()) {
                return rankings;
            }
            
            int rank = 1;
            for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
                String userIdStr = (String) tuple.getValue();
                Double score = tuple.getScore();
                
                if (userIdStr != null && score != null) {
                    Long userId = Long.parseLong(userIdStr);
                    
                    // 查询用户信息
                    User user = userService.getById(userId);
                    if (user != null) {
                        RankingDTO dto = new RankingDTO();
                        dto.setRank(rank);
                        dto.setUserId(userId);
                        dto.setUsername(user.getUsername());
                        dto.setAvatar(user.getUseravatar());
                        dto.setPoints(score.intValue());
                        
                        rankings.add(dto);
                        rank++;
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("获取排行榜失败: key={}", key, e);
        }
        
        return rankings;
    }
}
