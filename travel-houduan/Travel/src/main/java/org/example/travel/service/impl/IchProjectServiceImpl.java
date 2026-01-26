package org.example.travel.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.travel.constants.CacheConstants;
import org.example.travel.model.entity.IchProject;
import org.example.travel.rag.CustomPgVectorStore;
import org.example.travel.service.CacheService;
import org.example.travel.service.IchProjectService;
import org.example.travel.mapper.IchProjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 非遗项目服务实现
 * 支持自动向量化到 ich_project_vector 表
 * 使用 Redis 缓存提升查询性能
 */
@Service
@Slf4j
public class IchProjectServiceImpl extends ServiceImpl<IchProjectMapper, IchProject>
    implements IchProjectService {

    @Resource(name = "customVectorStore")
    private CustomPgVectorStore vectorStore;
    
    @Resource
    private CacheService cacheService;

    @Override
    public IchProject getById(java.io.Serializable id) {
        // 使用缓存
        String cacheKey = CacheConstants.buildKey(CacheConstants.ICH_PROJECT_PREFIX, id);
        return cacheService.get(
            cacheKey,
            () -> super.getById(id),
            CacheConstants.ICH_PROJECT_TIMEOUT,
            CacheConstants.ICH_PROJECT_UNIT,
            IchProject.class
        );
    }
    
    /**
     * 按类别和城市查询项目列表（带缓存）
     */
    public List<IchProject> listByCategory(String category, String city) {
        String cacheKey = CacheConstants.buildKey(
            CacheConstants.ICH_PROJECT_LIST_PREFIX, 
            category, 
            city != null ? city : "all"
        );
        
        return cacheService.getList(
            cacheKey,
            () -> lambdaQuery()
                    .eq(IchProject::getCategory, category)
                    .eq(city != null, IchProject::getCity, city)
                    .list(),
            CacheConstants.ICH_PROJECT_LIST_TIMEOUT,
            CacheConstants.ICH_PROJECT_LIST_UNIT,
            IchProject.class
        );
    }

    @Override
    public boolean save(IchProject project) {
        boolean saved = super.save(project);
        if (saved) {
            // 清空列表缓存
            cacheService.deletePattern(CacheConstants.ICH_PROJECT_LIST_PREFIX + "*");
            // 异步向量化
            asyncVectorize(project);
        }
        return saved;
    }

    @Override
    public boolean updateById(IchProject project) {
        boolean updated = super.updateById(project);
        if (updated) {
            // 删除单个项目缓存
            String cacheKey = CacheConstants.buildKey(CacheConstants.ICH_PROJECT_PREFIX, project.getId());
            cacheService.delete(cacheKey);
            // 清空列表缓存
            cacheService.deletePattern(CacheConstants.ICH_PROJECT_LIST_PREFIX + "*");
            
            // 获取完整的项目信息（因为传入的可能只有部分字段）
            IchProject fullProject = super.getById(project.getId());
            if (fullProject != null) {
                asyncVectorize(fullProject);
            }
        }
        return updated;
    }

    @Override
    public boolean removeById(java.io.Serializable id) {
        boolean removed = super.removeById(id);
        if (removed && id instanceof Long) {
            // 删除单个项目缓存
            String cacheKey = CacheConstants.buildKey(CacheConstants.ICH_PROJECT_PREFIX, id);
            cacheService.delete(cacheKey);
            // 清空列表缓存
            cacheService.deletePattern(CacheConstants.ICH_PROJECT_LIST_PREFIX + "*");
            
            // 异步删除向量
            CompletableFuture.runAsync(() -> {
                vectorStore.deleteProjectVector((Long) id);
            });
        }
        return removed;
    }

    /**
     * 异步向量化非遗项目
     */
    private void asyncVectorize(IchProject project) {
        CompletableFuture.runAsync(() -> {
            try {
                // 构建用于向量化的文本内容
                String content = buildVectorContent(project);
                if (StrUtil.isNotBlank(content)) {
                    vectorStore.saveProjectVector(project.getId(), content);
                }
            } catch (Exception e) {
                log.error("非遗项目向量化失败, projectId={}", project.getId(), e);
            }
        });
    }

    /**
     * 构建用于向量化的文本内容
     * 包含：名称、类别、描述、城市
     */
    private String buildVectorContent(IchProject project) {
        StringBuilder sb = new StringBuilder();
        
        if (StrUtil.isNotBlank(project.getName())) {
            sb.append("非遗项目：").append(project.getName()).append("。");
        }
        if (StrUtil.isNotBlank(project.getCategory())) {
            sb.append("类别：").append(project.getCategory()).append("。");
        }
        if (StrUtil.isNotBlank(project.getCity())) {
            sb.append("所在城市：").append(project.getCity()).append("。");
        }
        if (StrUtil.isNotBlank(project.getDescription())) {
            sb.append("简介：").append(project.getDescription());
        }
        
        return sb.toString().trim();
    }
}




