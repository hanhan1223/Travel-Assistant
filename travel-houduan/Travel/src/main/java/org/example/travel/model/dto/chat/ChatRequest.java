package org.example.travel.model.dto.chat;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 聊天请求DTO
 */
@Data
public class ChatRequest {
    /**
     * 会话ID（可选，不传则创建新会话）
     */
    private Long conversationId;
    
    /**
     * 用户消息
     */
    private String message;
    
    /**
     * 用户纬度
     */
    private BigDecimal lat;
    
    /**
     * 用户经度
     */
    private BigDecimal lng;
}
