package org.example.travel.model.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * SSE 流式响应块
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreamChunk {
    
    /**
     * 事件类型：start/text/location/error/done
     */
    private String type;
    
    /**
     * 会话ID（仅 start 事件）
     */
    private Long conversationId;
    
    /**
     * 文本内容（仅 text 事件）
     */
    private String content;
    
    /**
     * 地点数据（仅 location 事件）
     */
    private List<LocationData> locations;
    
    /**
     * 错误信息（仅 error 事件）
     */
    private String error;
    
    /**
     * 状态（仅 done 事件）：success/error
     */
    private String status;
    
    // ========== 静态工厂方法 ==========
    
    public static StreamChunk start(Long conversationId) {
        return StreamChunk.builder()
                .type("start")
                .conversationId(conversationId)
                .build();
    }
    
    public static StreamChunk text(String content) {
        return StreamChunk.builder()
                .type("text")
                .content(content)
                .build();
    }
    
    public static StreamChunk location(List<LocationData> locations) {
        return StreamChunk.builder()
                .type("location")
                .locations(locations)
                .build();
    }
    
    public static StreamChunk error(String message) {
        return StreamChunk.builder()
                .type("error")
                .error(message)
                .build();
    }
    
    public static StreamChunk done(Long conversationId) {
        return StreamChunk.builder()
                .type("done")
                .status("success")
                .conversationId(conversationId)
                .build();
    }
    
    public static StreamChunk doneWithError(Long conversationId) {
        return StreamChunk.builder()
                .type("done")
                .status("error")
                .conversationId(conversationId)
                .build();
    }
}
