package org.example.travel.tools;

import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import org.example.travel.model.entity.IchMedia;
import org.example.travel.model.entity.IchProject;
import org.example.travel.service.IchMediaService;
import org.example.travel.service.IchProjectService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 非遗项目查询工具
 */
@Component
public class IchProjectTool {

    @Resource
    private IchProjectService ichProjectService;

    @Resource
    private IchMediaService ichMediaService;

    @Tool(description = "根据非遗项目ID获取详细信息，包括名称、类别、简介、位置、图片等")
    public String getProjectById(
            @ToolParam(description = "非遗项目ID") Long projectId
    ) {
        IchProject project = ichProjectService.getById(projectId);
        if (project == null) {
            return "未找到该非遗项目";
        }
        return buildProjectWithImages(project);
    }

    @Tool(description = "根据城市名称查询该城市的非遗项目列表，包含图片信息")
    public String getProjectsByCity(
            @ToolParam(description = "城市名称，如：广州、苏州、杭州") String city
    ) {
        List<IchProject> projects = ichProjectService.lambdaQuery()
                .eq(IchProject::getCity, city)
                .list();
        if (projects.isEmpty()) {
            return "该城市暂无收录的非遗项目";
        }
        return buildProjectsWithImages(projects);
    }

    @Tool(description = "根据非遗类别查询项目列表，如手作、戏曲、美食等，包含图片信息")
    public String getProjectsByCategory(
            @ToolParam(description = "非遗类别，如：手作、戏曲、美食、民俗") String category
    ) {
        List<IchProject> projects = ichProjectService.lambdaQuery()
                .like(IchProject::getCategory, category)
                .list();
        if (projects.isEmpty()) {
            return "暂无该类别的非遗项目";
        }
        return buildProjectsWithImages(projects);
    }

    @Tool(description = "根据关键词搜索非遗项目，可搜索项目名称和简介，包含图片信息")
    public String searchProjects(
            @ToolParam(description = "搜索关键词，如：刺绣、陶瓷、昆曲") String keyword
    ) {
        List<IchProject> projects = ichProjectService.lambdaQuery()
                .like(IchProject::getName, keyword)
                .or()
                .like(IchProject::getDescription, keyword)
                .list();
        if (projects.isEmpty()) {
            return "未找到与'" + keyword + "'相关的非遗项目";
        }
        return buildProjectsWithImages(projects);
    }

    @Tool(description = "查询适合室内体验的非遗项目，适用于雨天或天气不佳时推荐，包含图片信息")
    public String getIndoorProjects(
            @ToolParam(description = "城市名称，如：广州、苏州、杭州") String city
    ) {
        List<IchProject> projects = ichProjectService.lambdaQuery()
                .eq(IchProject::getCity, city)
                .eq(IchProject::getIsIndoor, 1)
                .list();
        if (projects.isEmpty()) {
            return "该城市暂无适合室内体验的非遗项目";
        }
        return buildProjectsWithImages(projects);
    }

    /**
     * 构建单个项目信息（包含图片）
     */
    private String buildProjectWithImages(IchProject project) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", project.getId());
        result.put("name", project.getName());
        result.put("category", project.getCategory());
        result.put("description", project.getDescription());
        result.put("city", project.getCity());
        result.put("lat", project.getLat());
        result.put("lng", project.getLng());
        result.put("isIndoor", project.getIsIndoor());
        
        // 查询图片
        List<IchMedia> mediaList = ichMediaService.lambdaQuery()
                .eq(IchMedia::getProjectId, project.getId())
                .eq(IchMedia::getMediaType, "image")
                .list();
        List<String> images = mediaList.stream()
                .map(IchMedia::getMediaUrl)
                .collect(Collectors.toList());
        result.put("images", images);
        
        return JSONUtil.toJsonStr(result);
    }

    /**
     * 构建多个项目信息（包含图片）
     */
    private String buildProjectsWithImages(List<IchProject> projects) {
        List<Long> projectIds = projects.stream()
                .map(IchProject::getId)
                .collect(Collectors.toList());
        
        // 批量查询所有图片
        List<IchMedia> allMedia = ichMediaService.lambdaQuery()
                .in(IchMedia::getProjectId, projectIds)
                .eq(IchMedia::getMediaType, "image")
                .list();
        
        // 按项目ID分组
        Map<Long, List<String>> mediaMap = allMedia.stream()
                .collect(Collectors.groupingBy(
                        IchMedia::getProjectId,
                        Collectors.mapping(IchMedia::getMediaUrl, Collectors.toList())
                ));
        
        // 构建结果
        List<Map<String, Object>> resultList = projects.stream().map(project -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", project.getId());
            item.put("name", project.getName());
            item.put("category", project.getCategory());
            item.put("description", project.getDescription());
            item.put("city", project.getCity());
            item.put("lat", project.getLat());
            item.put("lng", project.getLng());
            item.put("isIndoor", project.getIsIndoor());
            item.put("images", mediaMap.getOrDefault(project.getId(), List.of()));
            return item;
        }).collect(Collectors.toList());
        
        return JSONUtil.toJsonStr(resultList);
    }
}
