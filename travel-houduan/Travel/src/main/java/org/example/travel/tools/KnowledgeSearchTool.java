package org.example.travel.tools;

import jakarta.annotation.Resource;
import org.example.travel.rag.CustomPgVectorStore;
import org.example.travel.service.KnowledgeService;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 知识库检索工具
 */
@Component
public class KnowledgeSearchTool {

    @Resource
    private KnowledgeService knowledgeService;

    @Resource(name = "customVectorStore")
    private CustomPgVectorStore vectorStore;

    @Tool(description = "从知识库中检索与查询内容相关的非遗文化知识，用于回答用户关于非遗历史、工艺、传承等方面的问题")
    public String searchKnowledge(
            @ToolParam(description = "查询内容，如：苏绣的历史、昆曲的特点、制瓷工艺等") String query,
            int i) {
        String result = knowledgeService.searchRelevantContent(query, 3);
        if (result.isEmpty()) {
            return "知识库中暂无相关内容";
        }
        return result;
    }

    @Tool(description = "根据非遗项目ID检索该项目的详细知识，用于深度讲解特定非遗项目")
    public String searchByProject(
            @ToolParam(description = "查询内容") String query,
            @ToolParam(description = "非遗项目ID") Long projectId
    ) {
        List<Document> results = vectorStore.searchByProjectId(query, projectId, 3);
        if (results.isEmpty()) {
            return "该非遗项目暂无详细知识内容";
        }
        return results.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n---\n\n"));
    }
}
