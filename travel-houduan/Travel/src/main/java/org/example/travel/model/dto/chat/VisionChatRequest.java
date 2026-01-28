package org.example.travel.model.dto.chat;

import lombok.Data;

/**
 * 图像识别聊天请求
 */
@Data
public class VisionChatRequest {
    
    /**
     * 会话ID（可选，不传则创建新会话）
     */
    private Long conversationId;
    
    /**
     * 用户消息文本（可选）
     */
    private String message;
    
    /**
     * 图片URL（必须是可访问的HTTP/HTTPS链接）
     */
    private String imageUrl;
    
    /**
     * 识别类型：craft(工艺品)、building(建筑)、food(美食)、general(通用)
     */
    private String recognitionType = "general";
}
