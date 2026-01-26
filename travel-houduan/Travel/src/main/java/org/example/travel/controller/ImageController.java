package org.example.travel.controller;

import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.travel.common.BaseResponse;
import org.example.travel.common.Result;
import org.example.travel.exception.BusinessException;
import org.example.travel.exception.ErrorCode;
import org.example.travel.model.entity.User;
import org.example.travel.service.UserService;
import org.example.travel.service.VisionService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


/**
 * 图片识别控制器
 */
@RestController
@RequestMapping("/image")
@Tag(name = "图片识别接口")
@Slf4j
public class ImageController {
    
    @Resource
    private VisionService visionService;
    
    @Resource
    private UserService userService;
    
    /**
     * 识别图片中的非遗内容（直接上传文件）
     */
    @PostMapping("/recognize")
    @Operation(summary = "识别图片（文件上传）")
    public BaseResponse<String> recognizeByFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false, defaultValue = "general") String type,
            HttpServletRequest httpRequest) {
        
        // 验证登录
        User loginUser = userService.getLoginUser(httpRequest);
        
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片文件不能为空");
        }
        
        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "只支持图片文件");
        }
        
        // 验证文件大小（最大10MB）
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片文件不能超过10MB");
        }
        
        String result;
        switch (type.toLowerCase()) {
            case "craft":
                result = visionService.recognizeHeritageCraftByFile(file);
                break;
            case "building":
                result = visionService.recognizeHeritageBuildingByFile(file);
                break;
            case "food":
                result = visionService.recognizeHeritageFoodByFile(file);
                break;
            case "text":
                result = visionService.recognizeTextByFile(file);
                break;
            default:
                result = visionService.recognizeImageByFile(file, null);
        }
        
        return Result.success(result);
    }
    
    /**
     * 识别图片中的非遗内容（通过URL，保留兼容性）
     */
    @PostMapping("/recognize/url")
    @Operation(summary = "识别图片（URL方式）")
    public BaseResponse<String> recognizeByUrl(
            @RequestParam String imageUrl,
            @RequestParam(required = false, defaultValue = "general") String type,
            HttpServletRequest httpRequest) {
        
        // 验证登录
        User loginUser = userService.getLoginUser(httpRequest);
        
        if (StrUtil.isBlank(imageUrl)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片URL不能为空");
        }
        
        String result;
        switch (type.toLowerCase()) {
            case "craft":
                result = visionService.recognizeHeritageCraft(imageUrl);
                break;
            case "building":
                result = visionService.recognizeHeritageBuilding(imageUrl);
                break;
            case "food":
                result = visionService.recognizeHeritageFood(imageUrl);
                break;
            case "text":
                result = visionService.recognizeText(imageUrl);
                break;
            default:
                result = visionService.recognizeImage(imageUrl, null);
        }
        
        return Result.success(result);
    }
    
    /**
     * 识别非遗工艺品（文件上传）
     */
    @PostMapping("/recognize/craft")
    @Operation(summary = "识别非遗工艺品")
    public BaseResponse<String> recognizeCraft(@RequestParam("file") MultipartFile file, HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片文件不能为空");
        }
        
        String result = visionService.recognizeHeritageCraftByFile(file);
        return Result.success(result);
    }
    
    /**
     * 识别非遗建筑（文件上传）
     */
    @PostMapping("/recognize/building")
    @Operation(summary = "识别非遗建筑")
    public BaseResponse<String> recognizeBuilding(@RequestParam("file") MultipartFile file, HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片文件不能为空");
        }
        
        String result = visionService.recognizeHeritageBuildingByFile(file);
        return Result.success(result);
    }
    
    /**
     * 识别非遗美食（文件上传）
     */
    @PostMapping("/recognize/food")
    @Operation(summary = "识别非遗美食")
    public BaseResponse<String> recognizeFood(@RequestParam("file") MultipartFile file, HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片文件不能为空");
        }
        
        String result = visionService.recognizeHeritageFoodByFile(file);
        return Result.success(result);
    }
    
    /**
     * OCR文字识别（文件上传）
     */
    @PostMapping("/recognize/text")
    @Operation(summary = "OCR文字识别")
    public BaseResponse<String> recognizeText(@RequestParam("file") MultipartFile file, HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片文件不能为空");
        }
        
        String result = visionService.recognizeTextByFile(file);
        return Result.success(result);
    }
}
