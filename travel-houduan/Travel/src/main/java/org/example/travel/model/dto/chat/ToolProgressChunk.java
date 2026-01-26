package org.example.travel.model.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工具调用进度事件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ToolProgressChunk {
    private String type = "tool_progress";
    private String toolName;
    private String status; // "start" | "running" | "done" | "error"
    private String message;
    
    public static ToolProgressChunk start(String toolName, String message) {
        return new ToolProgressChunk("tool_progress", toolName, "start", message);
    }
    
    public static ToolProgressChunk running(String toolName, String message) {
        return new ToolProgressChunk("tool_progress", toolName, "running", message);
    }
    
    public static ToolProgressChunk done(String toolName, String message) {
        return new ToolProgressChunk("tool_progress", toolName, "done", message);
    }
    
    public static ToolProgressChunk error(String toolName, String message) {
        return new ToolProgressChunk("tool_progress", toolName, "error", message);
    }
}
