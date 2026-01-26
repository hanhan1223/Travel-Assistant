package org.example.travel.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户积分实体
 */
@Data
@TableName("user_points")
public class UserPoints implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 总积分
     */
    private Integer totalPoints;
    
    /**
     * 当前等级
     */
    private Integer currentLevel;
    
    /**
     * 总游戏次数
     */
    private Integer totalGames;
    
    /**
     * 总答对题数
     */
    private Integer totalCorrect;
    
    /**
     * 总答题数
     */
    private Integer totalQuestions;
    
    /**
     * 最高分
     */
    private Integer bestScore;
    
    /**
     * 最高正确率
     */
    private BigDecimal bestAccuracy;
    
    /**
     * 连续签到天数
     */
    private Integer consecutiveDays;
    
    /**
     * 最后游戏日期
     */
    private Date lastPlayDate;
    
    /**
     * 创建时间
     */
    private Date createdAt;
    
    /**
     * 更新时间
     */
    private Date updatedAt;
    
    private static final long serialVersionUID = 1L;
}
