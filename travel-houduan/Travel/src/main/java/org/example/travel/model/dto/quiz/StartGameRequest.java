package org.example.travel.model.dto.quiz;

import lombok.Data;

import java.io.Serializable;

/**
 * 开始游戏请求
 */
@Data
public class StartGameRequest implements Serializable {
    
    /**
     * 游戏模式（normal-普通 challenge-挑战 daily-每日）
     */
    private String gameMode = "normal";
    
    /**
     * 难度（1-简单 2-中等 3-困难）
     */
    private Integer difficulty = 1;
    
    /**
     * 题目数量
     */
    private Integer questionCount = 10;
    
    /**
     * 非遗项目名称（可选，不指定则随机）
     */
    private String projectName;
    
    private static final long serialVersionUID = 1L;
}
