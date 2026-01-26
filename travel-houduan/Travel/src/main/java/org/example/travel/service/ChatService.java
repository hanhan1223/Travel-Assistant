package org.example.travel.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.travel.model.dto.chat.ChatRequest;
import org.example.travel.model.dto.env.EnvContextDTO;
import org.example.travel.model.entity.ChatConversation;
import org.example.travel.model.entity.ChatMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 聊天服务接口
 */
public interface ChatService {

    /**
     * 发送消息并获取流式响应
     * @param request 聊天请求
     * @param envContext 环境上下文
     * @param userId 用户ID
     * @return SSE发射器
     */
    SseEmitter chat(ChatRequest request, EnvContextDTO envContext, Long userId);
    
    /**
     * 发送图片消息并获取流式响应（支持图片识别）
     * @param file 图片文件
     * @param conversationId 会话ID（可选）
     * @param message 附加文本消息（可选）
     * @param envContext 环境上下文
     * @param userId 用户ID
     * @return SSE发射器
     */
    SseEmitter chatWithImage(org.springframework.web.multipart.MultipartFile file, 
                             Long conversationId, 
                             String message, 
                             EnvContextDTO envContext, 
                             Long userId);

    /**
     * 创建新会话
     * @param userId 用户ID
     * @return 会话ID
     */
    Long createConversation(Long userId);

    /**
     * 获取会话历史消息
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 消息列表
     */
    List<ChatMessage> getChatHistory(Long conversationId, Long userId);

    /**
     * 获取用户的会话列表
     * @param userId 用户ID
     * @param current 当前页
     * @param pageSize 每页大小
     * @return 会话分页列表
     */
    Page<ChatConversation> getConversationList(Long userId, int current, int pageSize);

    /**
     * 获取会话详情
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 会话信息
     */
    ChatConversation getConversation(Long conversationId, Long userId);

    /**
     * 更新会话标题
     * @param conversationId 会话ID
     * @param title 新标题
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean updateConversationTitle(Long conversationId, String title, Long userId);

    /**
     * 删除会话（同时删除消息）
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteConversation(Long conversationId, Long userId);
}
