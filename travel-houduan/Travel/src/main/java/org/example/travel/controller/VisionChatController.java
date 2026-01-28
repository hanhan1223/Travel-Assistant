package org.example.travel.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.travel.model.dto.chat.VisionChatRequest;
import org.example.travel.model.dto.env.EnvContextDTO;
import org.example.travel.model.entity.User;
import org.example.travel.service.ChatService;
import org.example.travel.service.EnvContextService;
import org.example.travel.service.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 图像识别聊天控制器
 * 专门处理带图片的对话请求
 */
@RestController
@RequestMapping("/vision")
@Tag(name = "图像识别聊天接口")
@Slf4j
public class VisionChatController {
    
    @Resource
    private ChatService chatService;
    
    @Resource
    private UserService userService;
    
    @Resource
    private EnvContextService envContextService;
    
    /**
     * 图像识别聊天（流式响应）
     */
    @PostMapping("/chat")
    @Operation(summary = "图像识别聊天", description = "上传图片URL进行识别和对话，返回流式响应")
    public SseEmitter visionChat(
            @RequestBody VisionChatRequest request,
            HttpServletRequest httpRequest) {
        
        // 获取登录用户
        User loginUser = userService.getLoginUser(httpRequest);
        
        // 获取环境上下文（可选）
        EnvContextDTO envContext = null;
        try {
            // 注意：EnvContextService.getEnvContext 需要经纬度参数
            // 这里暂时不获取环境上下文，或者可以从请求中获取
            // envContext = envContextService.getEnvContext(latitude, longitude);
        } catch (Exception e) {
            log.warn("获取环境上下文失败，使用默认值", e);
        }
        
        // 调用图像识别聊天服务
        return chatService.visionChat(request, envContext, loginUser.getId());
    }
}
