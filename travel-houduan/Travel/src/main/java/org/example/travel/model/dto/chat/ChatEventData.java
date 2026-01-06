package org.example.travel.model.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 聊天事件数据
 * 用于SSE推送结构化数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatEventData {
    /**
     * 事件类型：text/location/product/tool_call
     */
    private String type;
    
    /**
     * 文本内容
     */
    private String content;
    
    /**
     * 地点数据（当type=location时）
     */
    private List<LocationData> locations;
    
    /**
     * 产品数据（当type=product时）
     */
    private List<ProductData> products;
    
    /**
     * 工具调用信息（当type=tool_call时）
     */
    private String toolName;
    
    /**
     * 创建文本事件
     */
    public static ChatEventData text(String content) {
        return ChatEventData.builder()
                .type("text")
                .content(content)
                .build();
    }
    
    /**
     * 创建地点事件
     */
    public static ChatEventData location(List<LocationData> locations) {
        return ChatEventData.builder()
                .type("location")
                .locations(locations)
                .build();
    }
    
    /**
     * 创建产品事件
     */
    public static ChatEventData product(List<ProductData> products) {
        return ChatEventData.builder()
                .type("product")
                .products(products)
                .build();
    }
    
    /**
     * 创建工具调用事件
     */
    public static ChatEventData toolCall(String toolName) {
        return ChatEventData.builder()
                .type("tool_call")
                .toolName(toolName)
                .build();
    }
}
