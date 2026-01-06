package org.example.travel.model.dto.knowledge;

import lombok.Data;

/**
 * 知识库文档上传请求
 */
@Data
public class KnowledgeUploadRequest {
    /**
     * 文档标题
     */
    private String title;
    
    /**
     * 文档类型（ich_intro/ich_history/merchant_info等）
     */
    private String docType;
    
    /**
     * 关联的非遗项目ID（可选）
     */
    private Long projectId;
    
    /**
     * 关联的商户ID（可选）
     */
    private Long merchantId;
    
    /**
     * 标签（逗号分隔）
     */
    private String tags;
}
