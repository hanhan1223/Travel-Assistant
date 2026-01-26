package org.example.travel.model.dto.quiz;

import lombok.Data;

import java.io.Serializable;

/**
 * 提交答案请求
 */
@Data
public class SubmitAnswerRequest implements Serializable {
    
    /**
     * 游戏记录ID
     */
    private Long gameRecordId;
    
    /**
     * 题目ID
     */
    private Long questionId;
    
    /**
     * 用户答案
     */
    private String userAnswer;
    
    /**
     * 答题用时（秒）
     */
    private Integer timeSpent;
    
    private static final long serialVersionUID = 1L;
}
