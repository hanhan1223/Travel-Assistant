package org.example.travel.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 题目实体
 */
@Data
@TableName("quiz_question")
public class QuizQuestion implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 题目分类（非遗项目类型）
     */
    private String category;
    
    /**
     * 关联的非遗项目名称（不存储到数据库，仅用于生成题目时的临时字段）
     */
    @TableField(exist = false)
    private String projectName;
    
    /**
     * 难度等级（1-简单 2-中等 3-困难）
     */
    private Integer difficulty;
    
    /**
     * 题目类型（single-单选 multiple-多选 judge-判断）
     * 不存储到数据库，仅用于生成题目时的临时字段
     */
    @TableField(exist = false)
    private String questionType;
    
    /**
     * 题目内容
     */
    private String questionText;
    
    /**
     * 选项（JSON数组）
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private List<String> options;
    
    /**
     * 正确答案
     */
    private String correctAnswer;
    
    /**
     * 答案解析
     */
    private String explanation;
    
    /**
     * 题目分值
     */
    private Integer points;
    
    /**
     * 创建方式（AI/MANUAL）
     */
    private String createdBy;
    
    /**
     * 创建时间
     */
    private Date createdAt;
    
    /**
     * 更新时间
     */
    private Date updatedAt;
    
    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDeleted;
    
    private static final long serialVersionUID = 1L;
}
