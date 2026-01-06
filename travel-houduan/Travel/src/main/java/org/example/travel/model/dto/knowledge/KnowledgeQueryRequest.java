package org.example.travel.model.dto.knowledge;

import lombok.Data;

/**
 * 知识库查询请求
 */
@Data
public class KnowledgeQueryRequest {
    /**
     * 当前页码
     */
    private Integer current = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 10;
    
    /**
     * 文档标题（模糊搜索）
     */
    private String title;
    
    /**
     * 文档类型
     */
    private String docType;
    
    /**
     * 关联的非遗项目ID
     */
    private Long projectId;
}
