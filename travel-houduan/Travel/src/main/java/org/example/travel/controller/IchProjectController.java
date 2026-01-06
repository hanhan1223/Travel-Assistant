package org.example.travel.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.example.travel.common.BaseResponse;
import org.example.travel.common.Result;
import org.example.travel.exception.ErrorCode;
import org.example.travel.exception.ThrowUtils;
import org.example.travel.manager.CosManager;
import org.example.travel.model.dto.ich.IchProjectAddRequest;
import org.example.travel.model.dto.ich.IchProjectUpdateRequest;
import org.example.travel.model.entity.IchMedia;
import org.example.travel.model.entity.IchProject;
import org.example.travel.model.entity.Merchant;
import org.example.travel.model.entity.User;
import org.example.travel.service.IchMediaService;
import org.example.travel.service.MerchantService;
import org.example.travel.service.IchProjectService;
import org.example.travel.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.example.travel.model.vo.IchProjectVO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 非遗项目控制器
 */
@Slf4j
@RestController
@RequestMapping("/ich/project")
public class IchProjectController {

    @Resource
    private IchProjectService ichProjectService;

    @Resource
    private MerchantService merchantService;

    @Resource
    private IchMediaService ichMediaService;

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    /**
     * 允许上传的图片类型
     */
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    /**
     * 根据ID获取非遗项目详情
     */
    @GetMapping("/{id}")
    public BaseResponse<IchProject> getById(@PathVariable Long id) {
        IchProject project = ichProjectService.getById(id);
        ThrowUtils.throwIf(project == null, ErrorCode.NOT_FOUND_ERROR, "非遗项目不存在");
        return Result.success(project);
    }

    /**
     * 根据城市查询非遗项目
     */
    @GetMapping("/city/{city}")
    public BaseResponse<List<IchProject>> getByCity(@PathVariable String city) {
        List<IchProject> projects = ichProjectService.lambdaQuery()
                .eq(IchProject::getCity, city)
                .list();
        return Result.success(projects);
    }

    /**
     * 根据类别查询非遗项目
     */
    @GetMapping("/category/{category}")
    public BaseResponse<List<IchProject>> getByCategory(@PathVariable String category) {
        List<IchProject> projects = ichProjectService.lambdaQuery()
                .like(IchProject::getCategory, category)
                .list();
        return Result.success(projects);
    }

    /**
     * 分页查询非遗项目
     */
    @PostMapping("/list")
    public BaseResponse<Page<IchProject>> listProjects(@RequestBody IchProjectQueryRequest request) {
        Page<IchProject> page = new Page<>(request.getCurrent(), request.getPageSize());
        ichProjectService.lambdaQuery()
                .like(request.getName() != null, IchProject::getName, request.getName())
                .eq(request.getCity() != null, IchProject::getCity, request.getCity())
                .like(request.getCategory() != null, IchProject::getCategory, request.getCategory())
                .page(page);
        return Result.success(page);
    }

    /**
     * 分页查询非遗项目（包含媒体列表）
     */
    @PostMapping("/listWithMedia")
    public BaseResponse<Page<IchProjectVO>> listProjectsWithMedia(@RequestBody IchProjectQueryRequest request) {
        // 1. 分页查询项目
        Page<IchProject> projectPage = new Page<>(request.getCurrent(), request.getPageSize());
        ichProjectService.lambdaQuery()
                .like(request.getName() != null, IchProject::getName, request.getName())
                .eq(request.getCity() != null, IchProject::getCity, request.getCity())
                .like(request.getCategory() != null, IchProject::getCategory, request.getCategory())
                .orderByDesc(IchProject::getCreatedAt)
                .page(projectPage);
        
        List<IchProject> projects = projectPage.getRecords();
        if (projects.isEmpty()) {
            Page<IchProjectVO> emptyPage = new Page<>(request.getCurrent(), request.getPageSize());
            emptyPage.setTotal(0);
            emptyPage.setRecords(List.of());
            return Result.success(emptyPage);
        }
        
        // 2. 批量查询媒体
        List<Long> projectIds = projects.stream().map(IchProject::getId).collect(Collectors.toList());
        List<IchMedia> allMedia = ichMediaService.lambdaQuery()
                .in(IchMedia::getProjectId, projectIds)
                .list();
        
        // 3. 按项目ID分组
        Map<Long, List<IchMedia>> mediaMap = allMedia.stream()
                .collect(Collectors.groupingBy(IchMedia::getProjectId));
        
        // 4. 组装VO
        List<IchProjectVO> voList = projects.stream()
                .map(p -> IchProjectVO.fromEntity(p, mediaMap.getOrDefault(p.getId(), List.of())))
                .collect(Collectors.toList());
        
        // 5. 构建返回分页
        Page<IchProjectVO> resultPage = new Page<>(request.getCurrent(), request.getPageSize());
        resultPage.setTotal(projectPage.getTotal());
        resultPage.setRecords(voList);
        
        return Result.success(resultPage);
    }

    /**
     * 查询室内非遗项目（雨天推荐）
     */
    @GetMapping("/indoor/{city}")
    public BaseResponse<List<IchProject>> getIndoorProjects(@PathVariable String city) {
        List<IchProject> projects = ichProjectService.lambdaQuery()
                .eq(IchProject::getCity, city)
                .eq(IchProject::getIsIndoor, 1)
                .list();
        return Result.success(projects);
    }

    // ==================== 管理后台接口 ====================

    /**
     * 新增非遗项目（管理员）- 支持图片文件上传
     * @param images 图片文件列表（可选）
     * @param request 项目信息
     */
    @PostMapping("/add")
    public BaseResponse<Long> addProject(
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            IchProjectAddRequest request,
            HttpServletRequest httpRequest) {
        // 验证管理员权限
        User loginUser = userService.getLoginUser(httpRequest);
        ThrowUtils.throwIf(!"admin".equals(loginUser.getUserrole()), ErrorCode.NO_AUTH_ERROR, "无权限操作");
        
        ThrowUtils.throwIf(request.getName() == null || request.getName().isEmpty(), 
                ErrorCode.PARAMS_ERROR, "项目名称不能为空");
        
        // 创建项目实体
        IchProject project = new IchProject();
        BeanUtils.copyProperties(request, project);
        project.setCreatedAt(new Date());
        ichProjectService.save(project);
        
        // 上传图片并保存到ich_media表
        if (images != null && images.length > 0) {
            List<IchMedia> mediaList = uploadImages(images, project.getId());
            if (!mediaList.isEmpty()) {
                ichMediaService.saveBatch(mediaList);
            }
        }
        
        // 关联商户
        if (request.getMerchantIds() != null && !request.getMerchantIds().isEmpty()) {
            merchantService.lambdaUpdate()
                    .in(Merchant::getId, request.getMerchantIds())
                    .set(Merchant::getProjectId, project.getId())
                    .update();
        }
        
        return Result.success(project.getId());
    }

    /**
     * 更新非遗项目（管理员）- 支持图片文件上传
     * @param id 项目ID
     * @param images 新增的图片文件列表（可选）
     * @param deleteMediaIds 要删除的媒体ID列表（可选，逗号分隔）
     * @param request 项目信息
     */
    @PutMapping("/update/{id}")
    public BaseResponse<Boolean> updateProject(
            @PathVariable Long id,
            @RequestParam(value = "images", required = false) MultipartFile[] images,
            @RequestParam(value = "deleteMediaIds", required = false) String deleteMediaIds,
            @RequestParam(value = "merchantIds", required = false) String merchantIdsStr,
            IchProjectUpdateRequest request,
            HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        ThrowUtils.throwIf(!"admin".equals(loginUser.getUserrole()), ErrorCode.NO_AUTH_ERROR, "无权限操作");
        
        IchProject existing = ichProjectService.getById(id);
        ThrowUtils.throwIf(existing == null, ErrorCode.NOT_FOUND_ERROR, "非遗项目不存在");
        
        // 更新项目信息（只有当request中有非空字段时才更新）
        boolean hasFieldToUpdate = request.getName() != null || request.getCategory() != null 
                || request.getDescription() != null || request.getCity() != null
                || request.getLat() != null || request.getLng() != null
                || request.getIsIndoor() != null || request.getOpenStatus() != null;
        
        boolean result = true;
        if (hasFieldToUpdate) {
            IchProject project = new IchProject();
            BeanUtils.copyProperties(request, project);
            project.setId(id);
            result = ichProjectService.updateById(project);
        }
        
        // 删除指定的媒体
        if (deleteMediaIds != null && !deleteMediaIds.isEmpty()) {
            String[] ids = deleteMediaIds.split(",");
            for (String mediaIdStr : ids) {
                try {
                    Long mediaId = Long.parseLong(mediaIdStr.trim());
                    IchMedia media = ichMediaService.getById(mediaId);
                    if (media != null && media.getProjectId().equals(id)) {
                        // 从COS删除文件
                        String mediaUrl = media.getMediaUrl();
                        if (mediaUrl != null && mediaUrl.contains("/")) {
                            String key = mediaUrl.substring(mediaUrl.lastIndexOf("/ich/") + 1);
                            if (key.startsWith("ich/")) {
                                try {
                                    cosManager.deleteObject(key);
                                } catch (Exception e) {
                                    log.warn("删除COS文件失败: {}", key, e);
                                }
                            }
                        }
                        ichMediaService.removeById(mediaId);
                    }
                } catch (NumberFormatException e) {
                    log.warn("无效的媒体ID: {}", mediaIdStr);
                }
            }
        }
        
        // 上传新图片
        if (images != null && images.length > 0) {
            List<IchMedia> mediaList = uploadImages(images, id);
            if (!mediaList.isEmpty()) {
                ichMediaService.saveBatch(mediaList);
            }
        }
        
        // 解析商户ID（支持逗号分隔的字符串格式）
        List<Long> merchantIds = null;
        if (merchantIdsStr != null && !merchantIdsStr.trim().isEmpty()) {
            merchantIds = new ArrayList<>();
            for (String idStr : merchantIdsStr.split(",")) {
                try {
                    merchantIds.add(Long.parseLong(idStr.trim()));
                } catch (NumberFormatException e) {
                    log.warn("无效的商户ID: {}", idStr);
                }
            }
        }
        
        // 更新商户关联（先清除原有关联，再设置新关联）
        if (merchantIds != null) {
            // 清除原有关联
            merchantService.lambdaUpdate()
                    .eq(Merchant::getProjectId, id)
                    .set(Merchant::getProjectId, null)
                    .update();
            // 设置新关联
            if (!merchantIds.isEmpty()) {
                merchantService.lambdaUpdate()
                        .in(Merchant::getId, merchantIds)
                        .set(Merchant::getProjectId, id)
                        .update();
            }
        }
        
        return Result.success(result);
    }

    /**
     * 删除非遗项目（管理员）
     */
    @DeleteMapping("/{id}")
    public BaseResponse<Boolean> deleteProject(@PathVariable Long id, HttpServletRequest httpRequest) {
        User loginUser = userService.getLoginUser(httpRequest);
        ThrowUtils.throwIf(!"admin".equals(loginUser.getUserrole()), ErrorCode.NO_AUTH_ERROR, "无权限操作");
        
        IchProject existing = ichProjectService.getById(id);
        ThrowUtils.throwIf(existing == null, ErrorCode.NOT_FOUND_ERROR, "非遗项目不存在");
        
        boolean result = ichProjectService.removeById(id);
        return Result.success(result);
    }

    /**
     * 查询请求
     */
    @lombok.Data
    public static class IchProjectQueryRequest {
        private Integer current = 1;
        private Integer pageSize = 10;
        private String name;
        private String city;
        private String category;
    }

    /**
     * 获取项目的所有媒体文件
     */
    @GetMapping("/{id}/media")
    public BaseResponse<List<IchMedia>> getProjectMedia(@PathVariable Long id) {
        IchProject project = ichProjectService.getById(id);
        ThrowUtils.throwIf(project == null, ErrorCode.NOT_FOUND_ERROR, "非遗项目不存在");
        
        List<IchMedia> mediaList = ichMediaService.lambdaQuery()
                .eq(IchMedia::getProjectId, id)
                .list();
        return Result.success(mediaList);
    }

    /**
     * 上传图片到COS并创建IchMedia记录
     */
    private List<IchMedia> uploadImages(MultipartFile[] images, Long projectId) {
        List<IchMedia> mediaList = new ArrayList<>();
        
        for (MultipartFile image : images) {
            if (image == null || image.isEmpty()) {
                continue;
            }
            
            // 验证文件类型
            String contentType = image.getContentType();
            if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
                log.warn("不支持的图片类型: {}", contentType);
                continue;
            }
            
            // 生成唯一文件名
            String originalFilename = image.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String key = "ich/project/" + projectId + "/" + UUID.randomUUID() + extension;
            
            try {
                // 上传到COS
                cosManager.putObject(image, key);
                String imageUrl = cosManager.getObjectUrl(key);
                
                // 创建媒体记录
                IchMedia media = new IchMedia();
                media.setProjectId(projectId);
                media.setMediaType("image");
                media.setMediaUrl(imageUrl);
                media.setSource("admin_upload");
                media.setCreatedAt(new Date());
                mediaList.add(media);
                
                log.info("图片上传成功: {}", imageUrl);
            } catch (IOException e) {
                log.error("图片上传失败: {}", originalFilename, e);
            }
        }
        
        return mediaList;
    }
}
