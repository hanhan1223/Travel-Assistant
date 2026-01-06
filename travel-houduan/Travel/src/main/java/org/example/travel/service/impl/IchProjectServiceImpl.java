package org.example.travel.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.travel.model.entity.IchProject;
import org.example.travel.rag.CustomPgVectorStore;
import org.example.travel.service.IchProjectService;
import org.example.travel.mapper.IchProjectMapper;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * 非遗项目服务实现
 * 支持自动向量化到 ich_project_vector 表
 */
@Service
@Slf4j
public class IchProjectServiceImpl extends ServiceImpl<IchProjectMapper, IchProject>
    implements IchProjectService {

    @Resource(name = "customVectorStore")
    private CustomPgVectorStore vectorStore;

    @Override
    public boolean save(IchProject project) {
        boolean saved = super.save(project);
        if (saved) {
            // 异步向量化
            asyncVectorize(project);
        }
        return saved;
    }

    @Override
    public boolean updateById(IchProject project) {
        boolean updated = super.updateById(project);
        if (updated) {
            // 获取完整的项目信息（因为传入的可能只有部分字段）
            IchProject fullProject = this.getById(project.getId());
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




