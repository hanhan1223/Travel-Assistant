package org.example.travel.tools;

import cn.hutool.core.util.StrUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.travel.service.VisionService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * 图片识别工具
 * 整合视觉识别、知识库检索、联网搜索
 */
@Slf4j
@Component
public class ImageRecognitionTool {
    
    @Resource
    private VisionService visionService;
    
    @Resource
    private KnowledgeSearchTool knowledgeSearchTool;
    
    @Resource
    private ChatModel chatModel;
    
    /**
     * 识别图片中的非遗内容
     * 
     * @param imageUrl 图片URL（必须是可访问的HTTP/HTTPS链接）
     * @param recognitionType 识别类型：craft(工艺品)、building(建筑)、food(美食)、general(通用)
     * @return 识别结果，包含图片内容描述和相关非遗知识
     */
    @Tool(description = """
            识别图片中的非遗相关内容，并提供详细的知识介绍。
            
            工作流程：
            1. 使用视觉模型识别图片内容
            2. 提取关键词（如工艺品名称、建筑风格等）
            3. 从知识库检索相关非遗知识
            4. 整合所有信息，给出完整回答
            
            使用场景：
            - 用户上传非遗工艺品照片，想了解是什么
            - 用户拍摄传统建筑，想了解历史背景
            - 用户拍摄传统美食，想了解制作工艺
            - 用户需要识别景点标识牌上的文字
            
            注意：
            - imageUrl 必须是可访问的HTTP/HTTPS链接
            - 识别结果会自动整合知识库信息
            - 如果知识库信息不足，建议 Agent 调用 bing_search 工具补充
            - 如果图片不清晰或不是非遗相关内容，会如实告知用户
            """)
    public String recognizeHeritageImage(String imageUrl, String recognitionType) {
        try {
            log.info("开始识别非遗图片: url={}, type={}", imageUrl, recognitionType);
            
            // 参数校验
            if (StrUtil.isBlank(imageUrl)) {
                return "图片URL不能为空";
            }
            
            if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                return "图片URL必须是HTTP或HTTPS链接";
            }
            
            // 1. 视觉识别
            String visionResult;
            if ("craft".equalsIgnoreCase(recognitionType)) {
                visionResult = visionService.recognizeHeritageCraft(imageUrl);
            } else if ("building".equalsIgnoreCase(recognitionType)) {
                visionResult = visionService.recognizeHeritageBuilding(imageUrl);
            } else if ("food".equalsIgnoreCase(recognitionType)) {
                visionResult = visionService.recognizeHeritageFood(imageUrl);
            } else {
                // 通用识别
                visionResult = visionService.recognizeImage(imageUrl, 
                    "请识别图片中的内容，如果是非遗相关的工艺品、建筑或美食，请详细描述其特征和可能的名称。");
            }
            
            if (visionResult.contains("识别失败")) {
                return visionResult;
            }
            
            log.info("视觉识别完成: {}", visionResult.substring(0, Math.min(100, visionResult.length())));
            
            // 2. 提取关键词
            String keywords = extractKeywords(visionResult);
            log.info("提取关键词: {}", keywords);
            
            // 3. 知识库检索
            String knowledgeResult = "";
            if (StrUtil.isNotBlank(keywords)) {
                try {
                    knowledgeResult = knowledgeSearchTool.searchKnowledge(keywords);
                    log.info("知识库检索完成，结果长度: {}", knowledgeResult.length());
                } catch (Exception e) {
                    log.warn("知识库检索失败", e);
                }
            }
            
            // 4. 整合结果（不再自动调用联网搜索，让 Agent 决定）
            return integrateResults(visionResult, knowledgeResult, keywords);
            
        } catch (Exception e) {
            log.error("图片识别失败", e);
            return "图片识别失败: " + e.getMessage();
        }
    }
    
    /**
     * 从视觉识别结果中提取关键词
     */
    private String extractKeywords(String visionResult) {
        // 简单的关键词提取：查找可能的非遗项目名称
        // 这里可以使用更复杂的NLP技术，但为了简单起见，使用规则匹配
        
        // 常见非遗项目关键词
        String[] heritageKeywords = {
            "苏绣", "湘绣", "蜀绣", "粤绣",
            "景泰蓝", "剪纸", "泥塑", "木雕", "石雕",
            "青花瓷", "紫砂壶", "漆器",
            "京剧", "昆曲", "越剧", "黄梅戏",
            "太极拳", "少林功夫",
            "徽派建筑", "闽南建筑", "藏式建筑",
            "月饼", "粽子", "汤圆", "饺子"
        };
        
        for (String keyword : heritageKeywords) {
            if (visionResult.contains(keyword)) {
                return keyword;
            }
        }
        
        // 如果没有匹配到具体项目，提取前50个字符作为关键词
        return visionResult.substring(0, Math.min(50, visionResult.length()));
    }
    
    /**
     * 整合结果
     */
    private String integrateResults(String visionResult, String knowledgeResult, String keywords) {
        StringBuilder result = new StringBuilder();
        
        result.append("【图片识别结果】\n\n");
        result.append(visionResult);
        result.append("\n\n");
        
        if (StrUtil.isNotBlank(knowledgeResult) && !knowledgeResult.contains("未找到")) {
            result.append("【知识库信息】\n\n");
            result.append(knowledgeResult);
            result.append("\n\n");
        } else {
            // 提示 Agent 可以使用联网搜索
            result.append("【提示】知识库信息不足，建议使用 bing_search 工具搜索关键词：");
            result.append(keywords);
            result.append(" 非遗\n\n");
        }
        
        return result.toString().trim();
    }
}
