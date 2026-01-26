package org.example.travel.service;

import org.example.travel.model.dto.quiz.RankingDTO;

import java.util.List;

/**
 * 排行榜服务接口
 */
public interface RankingService {
    
    /**
     * 更新用户积分到排行榜
     */
    void updateUserScore(Long userId, Integer points);
    
    /**
     * 获取积分排行榜
     * 
     * @param topN 前N名
     * @return 排行榜列表
     */
    List<RankingDTO> getPointsRanking(int topN);
    
    /**
     * 获取用户排名
     */
    Long getUserRank(Long userId);
    
    /**
     * 获取周排行榜
     */
    List<RankingDTO> getWeeklyRanking(int topN);
    
    /**
     * 获取月排行榜
     */
    List<RankingDTO> getMonthlyRanking(int topN);
    
    /**
     * 清空周排行榜（定时任务调用）
     */
    void clearWeeklyRanking();
    
    /**
     * 清空月排行榜（定时任务调用）
     */
    void clearMonthlyRanking();
}
