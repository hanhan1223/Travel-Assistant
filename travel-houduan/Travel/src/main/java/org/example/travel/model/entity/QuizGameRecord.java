package org.example.travel.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 游戏记录实体
 */
@Data
@TableName("quiz_game_record")
public class QuizGameRecord implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 游戏模式（normal-普通 challenge-挑战 daily-每日）
     */
    private String gameMode;
    
    /**
     * 总题数
     */
    private Integer totalQuestions;
    
    /**
     * 答对题数
     */
    private Integer correctCount;
    
    /**
     * 总得分
     */
    private Integer totalScore;
    
    /**
     * 正确率
     */
    private BigDecimal accuracy;
    
    /**
     * 用时（秒）
     */
    private Integer timeSpent;
    
    /**
     * 状态（playing-进行中 completed-已完成 abandoned-已放弃）
     */
    private String status;
    
    /**
     * 开始时间
     */
    private Date startedAt;
    
    /**
     * 完成时间
     */
    private Date completedAt;
    
    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDeleted;
    
    private static final long serialVersionUID = 1L;
}
