package org.example.travel.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.travel.agent.TravelAgent;
import org.example.travel.agent.VisionAgent;
import org.example.travel.exception.BusinessException;
import org.example.travel.exception.ErrorCode;
import org.example.travel.model.dto.chat.ChatRequest;
import org.example.travel.model.dto.chat.VisionChatRequest;
import org.example.travel.model.dto.chat.LocationData;
import org.example.travel.model.dto.chat.StreamChunk;
import org.example.travel.model.dto.env.EnvContextDTO;
import org.example.travel.model.entity.ChatConversation;
import org.example.travel.model.entity.ChatMessage;
import org.example.travel.service.ChatConversationService;
import org.example.travel.service.ChatMessageService;
import org.example.travel.service.ChatService;
import org.example.travel.tools.PDFGenerationTool;
import org.example.travel.tools.RecommendTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 聊天服务实现
 * 使用自定义的 TravelAgent（基于 ToolCallAgent）处理对话
 */
@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    @Resource
    private ChatModel chatModel;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatConversationService conversationService;

    @Resource
    private ChatMessageService messageService;

    @Resource
    private ChatDataExtractorService dataExtractorService;

    @Resource
    private org.example.travel.manager.CosManager cosManager;

    @Override
    public SseEmitter chat(ChatRequest request, EnvContextDTO envContext, Long userId) {
        SseEmitter emitter = new SseEmitter(300000L); // 5分钟超时

        final Long finalConversationId;
        
        // 获取或创建会话
        if (request.getConversationId() != null) {
            // 验证会话归属
            ChatConversation conversation = conversationService.getById(request.getConversationId());
            if (conversation == null || !conversation.getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "会话不存在");
            }
            finalConversationId = request.getConversationId();
        } else {
            finalConversationId = createConversation(userId);
        }

        // 用于收集工具调用中提取的地点数据
        List<LocationData> extractedLocations = new ArrayList<>();

        CompletableFuture.runAsync(() -> {
            try {
                // 设置工具的用户上下文
                PDFGenerationTool.setCurrentUserId(userId);
                RecommendTool.setCurrentUserId(userId);
                
                // 发送 start 事件（包含会话ID）
                emitter.send(JSONUtil.toJsonStr(StreamChunk.start(finalConversationId)));
                
                // 保存用户消息
                saveMessage(finalConversationId, "user", request.getMessage(), null);

                // 创建 ChatClient
                ChatClient chatClient = ChatClient.builder(chatModel).build();

                // 创建 TravelAgent
                TravelAgent agent = new TravelAgent(allTools, chatClient);
                
                // 设置工具调用回调，实时提取地点数据
                agent.setToolCallCallback((toolName, toolResult) -> {
                    List<LocationData> locations = dataExtractorService.extractLocationsFromToolResult(toolName, toolResult);
                    extractedLocations.addAll(locations);
                });
                
                // 设置环境上下文
                if (envContext != null && envContext.getDescription() != null) {
                    agent.setEnvContext(envContext.getDescription());
                }

                // 加载历史消息到Agent上下文
                loadHistoryToAgent(agent, finalConversationId);

                // 执行Agent（工具调用阶段）- 这里会调用工具
                String result = agent.run(request.getMessage());
                
                // 提取最终回答（必须在 agent.run() 之后、cleanup 之前）
                String finalAnswer = extractFinalAnswer(result, agent);
                
                // 清理工具调用结果
                agent.clearToolCallResults();

                // 流式输出
                if (StrUtil.isNotBlank(finalAnswer)) {
                    // 流式输出文本内容
                    streamTextContent(emitter, finalAnswer);
                    
                    // 如果有地点数据，去重后发送 location 事件
                    if (!extractedLocations.isEmpty()) {
                        List<LocationData> uniqueLocations = deduplicateLocations(extractedLocations);
                        emitter.send(JSONUtil.toJsonStr(StreamChunk.location(uniqueLocations)));
                    }
                    
                    // 发送 done 事件（携带会话ID和状态）
                    emitter.send(JSONUtil.toJsonStr(StreamChunk.done(finalConversationId)));
                    
                    // 保存助手回复
                    String toolCallJson = extractedLocations.isEmpty() ? null : JSONUtil.toJsonStr(extractedLocations);
                    saveMessage(finalConversationId, "assistant", finalAnswer, toolCallJson);
                    
                    // 更新会话标题
                    updateConversationTitleIfNeeded(finalConversationId, request.getMessage());

                    emitter.complete();
                } else {
                    // 发送错误事件和done事件
                    emitter.send(JSONUtil.toJsonStr(StreamChunk.error("未能获取到回答")));
                    emitter.send(JSONUtil.toJsonStr(StreamChunk.doneWithError(finalConversationId)));
                    emitter.complete();
                }

            } catch (Exception e) {
                log.error("聊天处理异常", e);
                try {
                    emitter.send(JSONUtil.toJsonStr(StreamChunk.error("抱歉，系统出现了问题，请稍后重试")));
                    emitter.send(JSONUtil.toJsonStr(StreamChunk.doneWithError(finalConversationId)));
                    emitter.complete();
                } catch (IOException ex) {
                    emitter.completeWithError(ex);
                }
            } finally {
                // 清理工具的用户上下文，防止内存泄漏
                PDFGenerationTool.clearContext();
                RecommendTool.clearContext();
            }
        });

        // 设置超时和完成回调
        emitter.onTimeout(() -> {
            log.warn("SSE连接超时");
            emitter.complete();
        });

        emitter.onCompletion(() -> {
            log.info("SSE连接完成");
        });

        return emitter;
    }

    /**
     * 流式输出文本内容
     */
    private void streamTextContent(SseEmitter emitter, String content) throws IOException {
        // 先将转义的换行符还原
        content = content.replace("\\n", "\n");
        
        // 按段落分割（双换行）
        String[] paragraphs = content.split("\n\n");
        
        for (String paragraph : paragraphs) {
            if (StrUtil.isNotBlank(paragraph)) {
                // 段落内按句子分割
                String[] sentences = paragraph.split("(?<=[。！？])|(?<=\\. )|(?<=! )|(?<=\\? )");
                
                for (String sentence : sentences) {
                    if (StrUtil.isNotBlank(sentence)) {
                        emitter.send(JSONUtil.toJsonStr(StreamChunk.text(sentence.trim())));
                        
                        try {
                            Thread.sleep(30);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
                
                // 段落之间发送换行
                emitter.send(JSONUtil.toJsonStr(StreamChunk.text("\n\n")));
            }
        }
    }

    /**
     * 地点数据去重（按名称去重，保留信息最完整的）
     */
    private List<LocationData> deduplicateLocations(List<LocationData> locations) {
        java.util.Map<String, LocationData> uniqueMap = new java.util.LinkedHashMap<>();
        
        for (LocationData loc : locations) {
            if (loc.getName() == null) continue;
            
            String key = loc.getName().trim();
            LocationData existing = uniqueMap.get(key);
            
            if (existing == null) {
                uniqueMap.put(key, loc);
            } else {
                // 合并信息：保留更完整的数据
                if (existing.getImages() == null && loc.getImages() != null) {
                    existing.setImages(loc.getImages());
                }
                if (existing.getAddress() == null && loc.getAddress() != null) {
                    existing.setAddress(loc.getAddress());
                }
                if (existing.getDistance() == null && loc.getDistance() != null) {
                    existing.setDistance(loc.getDistance());
                }
                if (existing.getRating() == null && loc.getRating() != null) {
                    existing.setRating(loc.getRating());
                }
            }
        }
        
        return new ArrayList<>(uniqueMap.values());
    }

    /**
     * 从AI回复中提取地点数据（备用方法）
     * 当工具调用回调未能提取到数据时使用
     */
    private List<LocationData> extractLocationsFromResponse(String response) {
        // 这个方法现在作为备用，主要通过工具调用回调提取数据
        return new ArrayList<>();
    }

    /**
     * 加载历史消息到Agent
     */
    private void loadHistoryToAgent(TravelAgent agent, Long conversationId) {
        List<ChatMessage> history = messageService.lambdaQuery()
                .eq(ChatMessage::getConversationId, conversationId)
                .orderByAsc(ChatMessage::getCreatedAt)
                .last("LIMIT 10") // 只加载最近10条
                .list();

        for (ChatMessage msg : history) {
            if ("user".equals(msg.getRole())) {
                agent.getMessageList().add(new org.springframework.ai.chat.messages.UserMessage(msg.getContent()));
            } else if ("assistant".equals(msg.getRole())) {
                agent.getMessageList().add(new org.springframework.ai.chat.messages.AssistantMessage(msg.getContent()));
            }
        }
    }

    /**
     * 从Agent执行结果中提取最终回答
     * 优先从doTerminate工具的返回结果中提取
     */
    private String extractFinalAnswer(String agentResult, TravelAgent agent) {
        // 1. 首先尝试从工具调用结果中获取doTerminate的返回值
        List<TravelAgent.ToolCallResult> toolResults = agent.getToolCallResults();
        for (int i = toolResults.size() - 1; i >= 0; i--) {
            TravelAgent.ToolCallResult result = toolResults.get(i);
            if ("doTerminate".equals(result.getToolName())) {
                String answer = result.getResult();
                // 过滤掉错误信息
                if (StrUtil.isNotBlank(answer) && !isErrorMessage(answer)) {
                    log.info("从doTerminate工具提取到最终回答");
                    return answer;
                }
            }
        }
        
        // 2. 检查是否有lastFinalAnswer（AI输出了完整回答但没调用doTerminate的情况）
        String lastFinalAnswer = agent.getLastFinalAnswer();
        if (StrUtil.isNotBlank(lastFinalAnswer) && !isErrorMessage(lastFinalAnswer)) {
            log.info("从lastFinalAnswer提取到最终回答");
            return lastFinalAnswer;
        }
        
        // 3. 从消息列表中获取最后一条助手消息
        List<org.springframework.ai.chat.messages.Message> messages = agent.getMessageList();
        for (int i = messages.size() - 1; i >= 0; i--) {
            org.springframework.ai.chat.messages.Message msg = messages.get(i);
            if (msg instanceof org.springframework.ai.chat.messages.AssistantMessage) {
                String text = ((org.springframework.ai.chat.messages.AssistantMessage) msg).getText();
                if (StrUtil.isNotBlank(text) && !isErrorMessage(text)) {
                    return text;
                }
            }
        }
        
        // 4. 如果都没有找到，返回默认提示
        if (isErrorMessage(agentResult)) {
            return "抱歉，处理您的请求时遇到了一些问题，请稍后再试。";
        }
        return agentResult;
    }
    
    /**
     * 判断是否是错误信息
     */
    private boolean isErrorMessage(String text) {
        if (text == null) return false;
        return text.contains("Error:") 
                || text.contains("Exception") 
                || text.contains("failed")
                || text.contains("Conversion from JSON");
    }

    /**
     * 更新会话标题（使用第一条消息的前20个字符）
     */
    private void updateConversationTitleIfNeeded(Long conversationId, String firstMessage) {
        ChatConversation conversation = conversationService.getById(conversationId);
        if (conversation != null && "新对话".equals(conversation.getTitle())) {
            String title = firstMessage.length() > 20 ? 
                    firstMessage.substring(0, 20) + "..." : firstMessage;
            conversation.setTitle(title);
            conversationService.updateById(conversation);
        }
    }

    @Override
    public Long createConversation(Long userId) {
        ChatConversation conversation = new ChatConversation();
        conversation.setUserId(userId);
        conversation.setTitle("新对话");
        conversation.setCreatedAt(new Date());
        conversationService.save(conversation);
        return conversation.getId();
    }

    @Override
    public List<ChatMessage> getChatHistory(Long conversationId, Long userId) {
        // 验证会话归属
        ChatConversation conversation = conversationService.getById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "会话不存在");
        }

        return messageService.lambdaQuery()
                .eq(ChatMessage::getConversationId, conversationId)
                .orderByAsc(ChatMessage::getCreatedAt)
                .list();
    }

    @Override
    public Page<ChatConversation> getConversationList(Long userId, int current, int pageSize) {
        Page<ChatConversation> page = new Page<>(current, pageSize);
        return conversationService.lambdaQuery()
                .eq(ChatConversation::getUserId, userId)
                .orderByDesc(ChatConversation::getUpdatedAt)
                .page(page);
    }

    @Override
    public ChatConversation getConversation(Long conversationId, Long userId) {
        ChatConversation conversation = conversationService.getById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "会话不存在");
        }
        return conversation;
    }

    @Override
    public boolean updateConversationTitle(Long conversationId, String title, Long userId) {
        ChatConversation conversation = conversationService.getById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "会话不存在");
        }
        conversation.setTitle(title);
        return conversationService.updateById(conversation);
    }

    @Override
    public boolean deleteConversation(Long conversationId, Long userId) {
        ChatConversation conversation = conversationService.getById(conversationId);
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "会话不存在");
        }
        
        // 删除会话消息
        messageService.lambdaUpdate()
                .eq(ChatMessage::getConversationId, conversationId)
                .remove();
        
        // 删除会话
        return conversationService.removeById(conversationId);
    }

    /**
     * 保存消息
     */
    private void saveMessage(Long conversationId, String role, String content, String toolCallJson) {
        if (StrUtil.isBlank(content)) {
            return;
        }
        
        // 处理转义的换行符，确保保存到数据库的是真实的换行符
        String processedContent = content.replace("\\n", "\n");
        
        ChatMessage message = new ChatMessage();
        message.setConversationId(conversationId);
        message.setRole(role);
        message.setContent(processedContent);
        message.setMsgType("text");
        if (StrUtil.isNotBlank(toolCallJson)) {
            message.setToolCall(toolCallJson);
            message.setMsgType("card_event");
        }
        message.setCreatedAt(new Date());
        messageService.save(message);
        
        // 更新会话的更新时间
        ChatConversation conversation = new ChatConversation();
        conversation.setId(conversationId);
        conversation.setUpdatedAt(new Date());
        conversationService.updateById(conversation);
    }

    /**
     * 图像识别聊天（使用独立的 VisionAgent）
     */
    @Override
    public SseEmitter visionChat(VisionChatRequest request, EnvContextDTO envContext, Long userId) {
        SseEmitter emitter = new SseEmitter(300000L); // 5分钟超时

        final Long finalConversationId;
        
        // 获取或创建会话
        if (request.getConversationId() != null) {
            // 验证会话归属
            ChatConversation conversation = conversationService.getById(request.getConversationId());
            if (conversation == null || !conversation.getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "会话不存在");
            }
            finalConversationId = request.getConversationId();
        } else {
            finalConversationId = createConversation(userId);
        }

        // 用于收集工具调用中提取的地点数据
        List<LocationData> extractedLocations = new ArrayList<>();

        CompletableFuture.runAsync(() -> {
            try {
                // 发送 start 事件（包含会话ID）
                emitter.send(JSONUtil.toJsonStr(StreamChunk.start(finalConversationId)));
                
                // 构建用户消息（包含图片信息）
                String userMessage = request.getMessage() != null ? request.getMessage() : "请识别这张图片";
                String fullMessage = String.format("[图片识别] %s\n图片URL: %s", userMessage, request.getImageUrl());
                
                // 保存用户消息
                saveMessage(finalConversationId, "user", fullMessage, null);

                // 创建 ChatClient
                ChatClient chatClient = ChatClient.builder(chatModel).build();

                // 创建 VisionAgent（使用独立的图像识别 Agent）
                VisionAgent agent = new VisionAgent(allTools, chatClient);
                
                // 设置工具调用回调，实时提取地点数据
                agent.setToolCallCallback((toolName, toolResult) -> {
                    List<LocationData> locations = dataExtractorService.extractLocationsFromToolResult(toolName, toolResult);
                    extractedLocations.addAll(locations);
                });

                // 加载历史消息到Agent上下文
                loadHistoryToVisionAgent(agent, finalConversationId);

                // 构建 Agent 提示词（包含图片URL和识别类型）
                String agentPrompt = String.format("""
                        用户上传了一张图片，请识别并讲解。
                        
                        图片URL: %s
                        识别类型: %s
                        用户问题: %s
                        
                        请按以下步骤操作：
                        1. 使用 recognizeHeritageImage 工具识别图片（传入图片URL和识别类型）
                        2. 根据识别结果，使用 searchKnowledge 从知识库检索详细信息
                        3. 如果知识库信息不足，可以使用 webSearch 联网搜索
                        4. 整合所有信息，给出完整、专业的回答
                        5. 调用 doTerminate 结束对话
                        
                        回答要求：
                        - 使用标准 Markdown 格式
                        - 生动有趣，像一位博学的文化专家
                        - 如果识别出具体的非遗项目，要详细讲解其历史和文化
                        """,
                        request.getImageUrl(),
                        request.getRecognitionType(),
                        userMessage
                );

                // 执行Agent
                String result = agent.run(agentPrompt);
                
                // 提取最终回答
                String finalAnswer = extractFinalAnswerFromVisionAgent(result, agent);
                
                // 清理工具调用结果
                agent.clearToolCallResults();

                // 流式输出
                if (StrUtil.isNotBlank(finalAnswer)) {
                    // 流式输出文本内容
                    streamTextContent(emitter, finalAnswer);
                    
                    // 如果有地点数据，去重后发送 location 事件
                    if (!extractedLocations.isEmpty()) {
                        List<LocationData> uniqueLocations = deduplicateLocations(extractedLocations);
                        emitter.send(JSONUtil.toJsonStr(StreamChunk.location(uniqueLocations)));
                    }
                    
                    // 发送 done 事件
                    emitter.send(JSONUtil.toJsonStr(StreamChunk.done(finalConversationId)));
                    
                    // 保存助手回复
                    String toolCallJson = extractedLocations.isEmpty() ? null : JSONUtil.toJsonStr(extractedLocations);
                    saveMessage(finalConversationId, "assistant", finalAnswer, toolCallJson);
                    
                    // 更新会话标题
                    updateConversationTitleIfNeeded(finalConversationId, userMessage);

                    emitter.complete();
                } else {
                    emitter.send(JSONUtil.toJsonStr(StreamChunk.error("未能获取到回答")));
                    emitter.send(JSONUtil.toJsonStr(StreamChunk.doneWithError(finalConversationId)));
                    emitter.complete();
                }

            } catch (Exception e) {
                log.error("图像识别聊天处理异常", e);
                try {
                    emitter.send(JSONUtil.toJsonStr(StreamChunk.error("抱歉，图像识别失败，请稍后重试")));
                    emitter.send(JSONUtil.toJsonStr(StreamChunk.doneWithError(finalConversationId)));
                    emitter.complete();
                } catch (IOException ex) {
                    emitter.completeWithError(ex);
                }
            }
        });

        // 设置超时和完成回调
        emitter.onTimeout(() -> {
            log.warn("SSE连接超时");
            emitter.complete();
        });

        emitter.onCompletion(() -> {
            log.info("SSE连接完成");
        });

        return emitter;
    }

    /**
     * 加载历史消息到 VisionAgent
     */
    private void loadHistoryToVisionAgent(VisionAgent agent, Long conversationId) {
        List<ChatMessage> history = messageService.lambdaQuery()
                .eq(ChatMessage::getConversationId, conversationId)
                .orderByAsc(ChatMessage::getCreatedAt)
                .last("LIMIT 10") // 只加载最近10条
                .list();

        for (ChatMessage msg : history) {
            if ("user".equals(msg.getRole())) {
                agent.getMessageList().add(new org.springframework.ai.chat.messages.UserMessage(msg.getContent()));
            } else if ("assistant".equals(msg.getRole())) {
                agent.getMessageList().add(new org.springframework.ai.chat.messages.AssistantMessage(msg.getContent()));
            }
        }
    }

    /**
     * 从 VisionAgent 执行结果中提取最终回答
     */
    private String extractFinalAnswerFromVisionAgent(String agentResult, VisionAgent agent) {
        // 1. 首先尝试从工具调用结果中获取doTerminate的返回值
        List<VisionAgent.ToolCallResult> toolResults = agent.getToolCallResults();
        for (int i = toolResults.size() - 1; i >= 0; i--) {
            VisionAgent.ToolCallResult result = toolResults.get(i);
            if ("doTerminate".equals(result.getToolName())) {
                String answer = result.getResult();
                if (StrUtil.isNotBlank(answer) && !isErrorMessage(answer)) {
                    log.info("从doTerminate工具提取到最终回答");
                    return answer;
                }
            }
        }
        
        // 2. 检查是否有lastFinalAnswer
        String lastFinalAnswer = agent.getLastFinalAnswer();
        if (StrUtil.isNotBlank(lastFinalAnswer) && !isErrorMessage(lastFinalAnswer)) {
            log.info("从lastFinalAnswer提取到最终回答");
            return lastFinalAnswer;
        }
        
        // 3. 从消息列表中获取最后一条助手消息
        List<org.springframework.ai.chat.messages.Message> messages = agent.getMessageList();
        for (int i = messages.size() - 1; i >= 0; i--) {
            org.springframework.ai.chat.messages.Message msg = messages.get(i);
            if (msg instanceof org.springframework.ai.chat.messages.AssistantMessage) {
                String text = ((org.springframework.ai.chat.messages.AssistantMessage) msg).getText();
                if (StrUtil.isNotBlank(text) && !isErrorMessage(text)) {
                    return text;
                }
            }
        }
        
        // 4. 如果都没有找到，返回默认提示
        if (isErrorMessage(agentResult)) {
            return "抱歉，图像识别失败，请稍后再试。";
        }
        return agentResult;
    }

    /**
     * 图片消息聊天（文件上传方式）
     */
    @Override
    public SseEmitter chatWithImage(
            org.springframework.web.multipart.MultipartFile file,
            Long conversationId,
            String message,
            String recognitionType,
            EnvContextDTO envContext,
            Long userId) {
        
        SseEmitter emitter = new SseEmitter(300000L); // 5分钟超时

        final Long finalConversationId;
        
        // 获取或创建会话
        if (conversationId != null) {
            // 验证会话归属
            ChatConversation conversation = conversationService.getById(conversationId);
            if (conversation == null || !conversation.getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "会话不存在");
            }
            finalConversationId = conversationId;
        } else {
            finalConversationId = createConversation(userId);
        }

        // 用于收集工具调用中提取的地点数据
        List<LocationData> extractedLocations = new ArrayList<>();

        CompletableFuture.runAsync(() -> {
            String imageUrl = null;
            java.io.File tempFile = null;
            
            try {
                // 发送 start 事件（包含会话ID）
                emitter.send(JSONUtil.toJsonStr(StreamChunk.start(finalConversationId)));
                
                // 1. 上传图片到 COS
                String filename = file.getOriginalFilename();
                String filepath = String.format("/chat-images/%s/%s_%s", 
                        userId, 
                        System.currentTimeMillis(), 
                        filename);
                
                tempFile = java.io.File.createTempFile("chat-image-", null);
                file.transferTo(tempFile);
                cosManager.putObject(filepath, tempFile);
                imageUrl = cosManager.getObjectUrl(filepath);
                
                log.info("图片上传成功: {}", imageUrl);
                
                // 2. 构建用户消息（包含图片信息）
                String userMessage = message != null && !message.trim().isEmpty() 
                        ? message 
                        : "请识别这张图片";
                
                // 保存用户消息（图片URL保存到toolCall字段）
                String toolCallJson = JSONUtil.toJsonStr(Map.of(
                    "type", "image",
                    "url", imageUrl
                ));
                saveMessage(finalConversationId, "user", userMessage, toolCallJson);

                // 3. 创建 ChatClient
                ChatClient chatClient = ChatClient.builder(chatModel).build();

                // 4. 创建 VisionAgent（使用独立的图像识别 Agent）
                VisionAgent agent = new VisionAgent(allTools, chatClient);
                
                // 设置工具调用回调，实时提取地点数据
                agent.setToolCallCallback((toolName, toolResult) -> {
                    List<LocationData> locations = dataExtractorService.extractLocationsFromToolResult(toolName, toolResult);
                    extractedLocations.addAll(locations);
                });

                // 5. 加载历史消息到Agent上下文
                loadHistoryToVisionAgent(agent, finalConversationId);

                // 6. 构建 Agent 提示词（包含图片URL和识别类型）
                String finalRecognitionType = recognitionType != null ? recognitionType : "general";
                String agentPrompt = String.format("""
                        用户上传了一张图片，请识别并讲解。
                        
                        图片URL: %s
                        识别类型: %s
                        用户问题: %s
                        
                        请按以下步骤操作：
                        1. 使用 recognizeHeritageImage 工具识别图片（传入图片URL和识别类型）
                        2. 根据识别结果，使用 searchKnowledge 从知识库检索详细信息
                        3. 如果知识库信息不足，可以使用 webSearch 联网搜索
                        4. 整合所有信息，给出完整、专业的回答
                        5. 调用 doTerminate 结束对话
                        
                        回答要求：
                        - 使用标准 Markdown 格式
                        - 生动有趣，像一位博学的文化专家
                        - 如果识别出具体的非遗项目，要详细讲解其历史和文化
                        """,
                        imageUrl,
                        finalRecognitionType,
                        userMessage
                );

                // 7. 执行Agent
                String result = agent.run(agentPrompt);
                
                // 8. 提取最终回答
                String finalAnswer = extractFinalAnswerFromVisionAgent(result, agent);
                
                // 清理工具调用结果
                agent.clearToolCallResults();

                // 9. 流式输出
                if (StrUtil.isNotBlank(finalAnswer)) {
                    // 流式输出文本内容
                    streamTextContent(emitter, finalAnswer);
                    
                    // 如果有地点数据，去重后发送 location 事件
                    if (!extractedLocations.isEmpty()) {
                        List<LocationData> uniqueLocations = deduplicateLocations(extractedLocations);
                        emitter.send(JSONUtil.toJsonStr(StreamChunk.location(uniqueLocations)));
                    }
                    
                    // 发送 done 事件
                    emitter.send(JSONUtil.toJsonStr(StreamChunk.done(finalConversationId)));
                    
                    // 保存助手回复
                    String toolCallJson = extractedLocations.isEmpty() ? null : JSONUtil.toJsonStr(extractedLocations);
                    saveMessage(finalConversationId, "assistant", finalAnswer, toolCallJson);
                    
                    // 更新会话标题
                    updateConversationTitleIfNeeded(finalConversationId, userMessage);

                    emitter.complete();
                } else {
                    emitter.send(JSONUtil.toJsonStr(StreamChunk.error("未能获取到回答")));
                    emitter.send(JSONUtil.toJsonStr(StreamChunk.doneWithError(finalConversationId)));
                    emitter.complete();
                }

            } catch (Exception e) {
                log.error("图片消息处理异常", e);
                try {
                    emitter.send(JSONUtil.toJsonStr(StreamChunk.error("抱歉，图片识别失败，请稍后重试")));
                    emitter.send(JSONUtil.toJsonStr(StreamChunk.doneWithError(finalConversationId)));
                    emitter.complete();
                } catch (IOException ex) {
                    emitter.completeWithError(ex);
                }
            } finally {
                // 清理临时文件
                if (tempFile != null && tempFile.exists()) {
                    boolean deleted = tempFile.delete();
                    if (!deleted) {
                        log.warn("临时文件删除失败: {}", tempFile.getAbsolutePath());
                    }
                }
            }
        });

        // 设置超时和完成回调
        emitter.onTimeout(() -> {
            log.warn("SSE连接超时");
            emitter.complete();
        });

        emitter.onCompletion(() -> {
            log.info("SSE连接完成");
        });

        return emitter;
    }

}
