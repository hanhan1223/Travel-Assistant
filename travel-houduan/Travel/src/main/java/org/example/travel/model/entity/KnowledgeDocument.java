package org.example.travel.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;

/**
 * 知识库文档表
 */
@TableName(value = "knowledge_document")
@Data
public class KnowledgeDocument {
    /**
     * 文档ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 文档标题
     */
    private String title;
    
    /**
     * 文档类型（ich_intro/ich_history/merchant_info等）
     */
    @TableField("docType")
    private String docType;
    
    /**
     * 原始文件名
     */
    @TableField("fileName")
    private String fileName;
    
    /**
     * 文件存储路径
     */
    @TableField("filePath")
    private String filePath;
    
    /**
     * 文件大小（字节）
     */
    @TableField("fileSize")
    private Long fileSize;
    
    /**
     * 关联的非遗项目ID
     */
    @TableField("projectId")
    private Long projectId;
    
    /**
     * 关联的商户ID
     */
    @TableField("merchantId")
    private Long merchantId;
    
    /**
     * 标签
     */
    private String tags;
    
    /**
     * 切片数量
     */
    @TableField("chunkCount")
    private Integer chunkCount;
    
    /**
     * 向量化状态（0-待处理 1-处理中 2-已完成 3-失败）
     */
    @TableField("vectorStatus")
    private Integer vectorStatus;
    
    /**
     * 创建用户ID
     */
    @TableField("createUserId")
    private Long createUserId;
    
    /**
     * 创建时间
     */
    @TableField("createdAt")
    private Date createdAt;
    
    /**
     * 更新时间
     */
    @TableField("updatedAt")
    private Date updatedAt;
    
    /**
     * 是否删除
     */
    @TableLogic
    @TableField("isDelete")
    private Integer isDelete;
}
