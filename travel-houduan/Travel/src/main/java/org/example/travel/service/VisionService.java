package org.example.travel.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 视觉识别服务接口
 * 使用阿里云通义千问 VL 模型进行图片识别
 */
public interface VisionService {
    
    // ========== 文件上传方式（推荐） ==========
    
    /**
     * 识别图片中的非遗内容（文件上传）
     * 
     * @param file 图片文件
     * @param prompt 识别提示词，例如："这是什么非遗项目？"
     * @return 识别结果描述
     */
    String recognizeImageByFile(MultipartFile file, String prompt);
    
    /**
     * 识别非遗工艺品（文件上传）
     * 
     * @param file 图片文件
     * @return 识别结果，包含工艺品名称、特征、可能的非遗项目等
     */
    String recognizeHeritageCraftByFile(MultipartFile file);
    
    /**
     * 识别非遗建筑（文件上传）
     * 
     * @param file 图片文件
     * @return 识别结果，包含建筑风格、特征、可能的历史背景等
     */
    String recognizeHeritageBuildingByFile(MultipartFile file);
    
    /**
     * 识别非遗美食（文件上传）
     * 
     * @param file 图片文件
     * @return 识别结果，包含美食名称、制作工艺、文化背景等
     */
    String recognizeHeritageFoodByFile(MultipartFile file);
    
    /**
     * OCR文字识别（文件上传）
     * 
     * @param file 图片文件
     * @return 识别出的文字内容
     */
    String recognizeTextByFile(MultipartFile file);
    
    // ========== URL方式（保留兼容性） ==========
    
    /**
     * 识别图片中的非遗内容（URL方式）
     * 
     * @param imageUrl 图片URL（支持HTTP/HTTPS链接）
     * @param prompt 识别提示词，例如："这是什么非遗项目？"
     * @return 识别结果描述
     */
    String recognizeImage(String imageUrl, String prompt);
    
    /**
     * 识别非遗工艺品（URL方式）
     * 
     * @param imageUrl 图片URL
     * @return 识别结果，包含工艺品名称、特征、可能的非遗项目等
     */
    String recognizeHeritageCraft(String imageUrl);
    
    /**
     * 识别非遗建筑（URL方式）
     * 
     * @param imageUrl 图片URL
     * @return 识别结果，包含建筑风格、特征、可能的历史背景等
     */
    String recognizeHeritageBuilding(String imageUrl);
    
    /**
     * 识别非遗美食（URL方式）
     * 
     * @param imageUrl 图片URL
     * @return 识别结果，包含美食名称、制作工艺、文化背景等
     */
    String recognizeHeritageFood(String imageUrl);
    
    /**
     * OCR文字识别（URL方式）
     * 
     * @param imageUrl 图片URL
     * @return 识别出的文字内容
     */
    String recognizeText(String imageUrl);
}
