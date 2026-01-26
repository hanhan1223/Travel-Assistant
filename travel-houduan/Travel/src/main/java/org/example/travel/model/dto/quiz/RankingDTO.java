package org.example.travel.model.dto.quiz;

import lombok.Data;

import java.io.Serializable;

/**
 * 排行榜 DTO
 */
@Data
public class RankingDTO implements Serializable {
    
    /**
     * 排名
     */
    private Integer rank;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 头像
     */
    private String avatar;
    
    /**
     * 积分
     */
    private Integer points;
    
    /**
     * 等级
     */
    private Integer level;
    
    /**
     * 总游戏次数
     */
    private Integer totalGames;
    
    /**
     * 最高正确率
     */
    private Double bestAccuracy;
    
    private static final long serialVersionUID = 1L;
}
