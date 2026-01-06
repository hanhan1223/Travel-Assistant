package org.example.travel.service;

import org.example.travel.model.dto.recommend.RecommendRequest;
import org.example.travel.model.dto.recommend.RecommendResponse;

/**
 * Python算法服务接口
 * 用于调用Python FastAPI服务进行推荐计算
 */
public interface PythonAlgorithmService {
    
    /**
     * 获取非遗项目推荐
     * @param request 推荐请求
     * @return 推荐结果
     */
    RecommendResponse getRecommendations(RecommendRequest request);
    
    /**
     * 计算文本向量
     * @param text 文本内容
     * @return 向量数组
     */
    float[] computeEmbedding(String text);
    
    /**
     * 计算两个向量的相似度
     * @param vector1 向量1
     * @param vector2 向量2
     * @return 相似度分数
     */
    float computeSimilarity(float[] vector1, float[] vector2);
}
