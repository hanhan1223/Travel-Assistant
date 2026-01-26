package org.example.travel.service.impl;

import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.alibaba.dashscope.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.example.travel.service.VisionService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * 视觉识别服务实现类
 * 使用阿里云通义千问 VL 模型进行图片识别
 */
@Slf4j
@Service
public class VisionServiceImpl implements VisionService {
    
    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;
    
    @Value("${spring.ai.dashscope.vision.model:qwen-vl-plus}")
    private String visionModel;
    
    // ========== 文件上传方式实现 ==========
    
    @Override
    public String recognizeImageByFile(MultipartFile file, String prompt) {
        // 直接使用 URL 方式识别（由调用方负责上传到 COS）
        throw new UnsupportedOperationException("请使用 recognizeImage(imageUrl, prompt) 方法");
    }
    
    @Override
    public String recognizeHeritageCraftByFile(MultipartFile file) {
        throw new UnsupportedOperationException("请使用 recognizeHeritageCraft(imageUrl) 方法");
    }
    
    @Override
    public String recognizeHeritageBuildingByFile(MultipartFile file) {
        throw new UnsupportedOperationException("请使用 recognizeHeritageBuilding(imageUrl) 方法");
    }
    
    @Override
    public String recognizeHeritageFoodByFile(MultipartFile file) {
        throw new UnsupportedOperationException("请使用 recognizeHeritageFood(imageUrl) 方法");
    }
    
    @Override
    public String recognizeTextByFile(MultipartFile file) {
        throw new UnsupportedOperationException("请使用 recognizeText(imageUrl) 方法");
    }
    
    // ========== URL方式实现（保留兼容性） ==========
    
    @Override
    public String recognizeImage(String imageUrl, String prompt) {
        try {
            log.info("开始识别图片: url={}, model={}, prompt={}", imageUrl, visionModel, prompt);
            
            // 构建多模态消息
            MultiModalMessage userMessage = MultiModalMessage.builder()
                    .role(Role.USER.getValue())
                    .content(Arrays.asList(
                            // 图片内容
                            Map.of("image", imageUrl),
                            // 文本提示
                            Map.of("text", prompt != null ? prompt : "请识别图片中的内容，如果是非遗相关的工艺品、建筑或美食，请详细描述其特征。")
                    ))
                    .build();
            
            // 构建请求参数
            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .apiKey(apiKey)
                    .model(visionModel)  // 使用配置的视觉模型
                    .message(userMessage)
                    .build();
            
            // 调用API
            MultiModalConversation conversation = new MultiModalConversation();
            MultiModalConversationResult result = conversation.call(param);
            
            // 提取结果
            String output = (String) result.getOutput().getChoices().get(0).getMessage().getContent().get(0).get("text");
            
            log.info("图片识别成功，结果长度: {}", output != null ? output.length() : 0);
            return output;
            
        } catch (ApiException | NoApiKeyException | UploadFileException e) {
            log.error("图片识别失败", e);
            return "图片识别失败: " + e.getMessage();
        }
    }
    
    @Override
    public String recognizeHeritageCraft(String imageUrl) {
        String prompt = """
                请识别图片中的物品，判断是否为非物质文化遗产相关的工艺品。
                
                如果是非遗工艺品，请简要回答：
                1. 工艺品名称
                2. 可能属于哪个非遗项目（如苏绣、景泰蓝、剪纸等）
                3. 主要特征（颜色、图案、工艺特点）
                
                如果不是非遗工艺品，请简要描述图片内容。
                回答要简洁，不超过200字。
                """;
        
        return recognizeImage(imageUrl, prompt);
    }
    
    @Override
    public String recognizeHeritageBuilding(String imageUrl) {
        String prompt = """
                请识别图片中的建筑，判断是否为非物质文化遗产相关的传统建筑。
                
                如果是传统建筑，请简要回答：
                1. 建筑类型（如古建筑、传统民居、寺庙等）
                2. 建筑风格（如徽派、闽南、藏式等）
                3. 主要特征（屋顶、门窗、装饰等）
                
                如果不是传统建筑，请简要描述图片内容。
                回答要简洁，不超过200字。
                """;
        
        return recognizeImage(imageUrl, prompt);
    }
    
    @Override
    public String recognizeHeritageFood(String imageUrl) {
        String prompt = """
                请识别图片中的美食，判断是否为非物质文化遗产相关的传统美食。
                
                如果是传统美食，请简要回答：
                1. 美食名称
                2. 可能属于哪个地区的非遗美食
                3. 主要特征（外观、食材、制作工艺）
                
                如果不是传统美食，请简要描述图片内容。
                回答要简洁，不超过200字。
                """;
        
        return recognizeImage(imageUrl, prompt);
    }
    
    @Override
    public String recognizeText(String imageUrl) {
        String prompt = "请识别图片中的所有文字内容，按原文输出。";
        return recognizeImage(imageUrl, prompt);
    }
}
