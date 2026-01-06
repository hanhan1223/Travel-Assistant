package org.example.travel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.example.travel.common.BaseResponse;
import org.example.travel.common.Result;
import org.example.travel.exception.ErrorCode;
import org.example.travel.exception.ThrowUtils;
import org.example.travel.model.dto.chat.ChatRequest;
import org.example.travel.model.dto.env.EnvContextDTO;
import org.example.travel.model.entity.ChatConversation;
import org.example.travel.model.entity.ChatMessage;
import org.example.travel.model.entity.User;
import org.example.travel.service.ChatService;
import org.example.travel.service.EnvContextService;
import org.example.travel.service.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 聊天控制器
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Resource
    private ChatService chatService;

    @Resource
    private EnvContextService envContextService;

    @Resource
    private UserService userService;

    /**
     * 发送消息（流式响应）
     * 支持继续历史对话：传入conversationId即可
     */
    @PostMapping("/send")
    public SseEmitter sendMessage(@RequestBody ChatRequest request, HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(request.getMessage() == null || request.getMessage().trim().isEmpty(),
                ErrorCode.PARAMS_ERROR, "消息不能为空");

        User loginUser = userService.getLoginUser(httpRequest);
        
        // 获取环境上下文
        EnvContextDTO envContext = null;
        if (request.getLat() != null && request.getLng() != null) {
            envContext = envContextService.getEnvContext(request.getLat(), request.getLng());
        }

        return chatService.chat(request, envContext, loginUser.getId());
    }

    /**
     * 初始化会话（获取欢迎语和环境信息）
     */
    @PostMapping("/init")
    public BaseResponse<ChatInitResponse> initChat(@RequestBody ChatInitRequest request,
                                                    HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        
        ChatInitResponse response = new ChatInitResponse();
        
        // 获取环境上下文
        if (request.getLat() != null && request.getLng() != null) {
            EnvContextDTO envContext = envContextService.getEnvContext(request.getLat(), request.getLng());
            response.setEnvContext(envContext);
            response.setWelcomeMessage(buildWelcomeMessage(envContext));
        } else {
            response.setWelcomeMessage("您好！我是您的非遗文化智能伴游助手。请允许获取您的位置信息，我可以为您推荐附近的非遗项目和文化体验。");
        }
        
        // 创建新会话
        Long conversationId = chatService.createConversation(loginUser.getId());
        response.setConversationId(conversationId);
        
        return Result.success(response);
    }

    /**
     * 获取用户的会话列表
     */
    @PostMapping("/conversations")
    public BaseResponse<Page<ChatConversation>> getConversationList(
            @RequestBody ConversationListRequest request,
            HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        int current = request.getCurrent() != null ? request.getCurrent() : 1;
        int pageSize = request.getPageSize() != null ? request.getPageSize() : 20;
        Page<ChatConversation> page = chatService.getConversationList(loginUser.getId(), current, pageSize);
        return Result.success(page);
    }

    /**
     * 获取会话详情（包含基本信息）
     */
    @GetMapping("/conversation/{conversationId}")
    public BaseResponse<ChatConversation> getConversation(
            @PathVariable Long conversationId,
            HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        ChatConversation conversation = chatService.getConversation(conversationId, loginUser.getId());
        return Result.success(conversation);
    }

    /**
     * 获取会话历史消息
     */
    @GetMapping("/history/{conversationId}")
    public BaseResponse<List<ChatMessage>> getChatHistory(
            @PathVariable Long conversationId,
            HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        List<ChatMessage> messages = chatService.getChatHistory(conversationId, loginUser.getId());
        return Result.success(messages);
    }

    /**
     * 更新会话标题
     */
    @PutMapping("/conversation/{conversationId}/title")
    public BaseResponse<Boolean> updateConversationTitle(
            @PathVariable Long conversationId,
            @RequestBody UpdateTitleRequest request,
            HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request.getTitle() == null || request.getTitle().trim().isEmpty(),
                ErrorCode.PARAMS_ERROR, "标题不能为空");
        User loginUser = userService.getLoginUser(httpRequest);
        boolean result = chatService.updateConversationTitle(conversationId, request.getTitle(), loginUser.getId());
        return Result.success(result);
    }

    /**
     * 删除会话（同时删除消息）
     */
    @DeleteMapping("/conversation/{conversationId}")
    public BaseResponse<Boolean> deleteConversation(
            @PathVariable Long conversationId,
            HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        boolean result = chatService.deleteConversation(conversationId, loginUser.getId());
        return Result.success(result);
    }

    /**
     * 构建欢迎消息
     */
    private String buildWelcomeMessage(EnvContextDTO envContext) {
        StringBuilder sb = new StringBuilder();
        sb.append("您好！我是您的非遗文化智能伴游助手。");
        
        if (envContext.getCity() != null) {
            sb.append("检测到您当前位于").append(envContext.getCity());
            if (envContext.getDistrict() != null) {
                sb.append(envContext.getDistrict());
            }
            sb.append("。");
        }
        
        if (envContext.getWeather() != null) {
            sb.append("今天天气").append(envContext.getWeather());
            if (envContext.getTemperature() != null) {
                sb.append("，温度").append(envContext.getTemperature()).append("℃");
            }
            sb.append("。");
        }
        
        if (Boolean.FALSE.equals(envContext.getOutdoorSuitable())) {
            sb.append("天气不太适合户外活动，我为您推荐一些室内的非遗体验项目吧！");
        } else {
            sb.append("天气不错，很适合出门探索非遗文化！有什么我可以帮您的吗？");
        }
        
        return sb.toString();
    }

    /**
     * 初始化请求
     */
    @lombok.Data
    public static class ChatInitRequest {
        private java.math.BigDecimal lat;
        private java.math.BigDecimal lng;
    }

    /**
     * 初始化响应
     */
    @lombok.Data
    public static class ChatInitResponse {
        private Long conversationId;
        private String welcomeMessage;
        private EnvContextDTO envContext;
    }

    /**
     * 会话列表请求
     */
    @lombok.Data
    public static class ConversationListRequest {
        private Integer current;
        private Integer pageSize;
    }

    /**
     * 更新标题请求
     */
    @lombok.Data
    public static class UpdateTitleRequest {
        private String title;
    }
}
