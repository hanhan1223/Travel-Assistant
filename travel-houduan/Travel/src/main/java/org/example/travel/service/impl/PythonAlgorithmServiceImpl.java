package org.example.travel.service.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.travel.model.dto.recommend.RecommendRequest;
import org.example.travel.model.dto.recommend.RecommendResponse;
import org.example.travel.service.PythonAlgorithmService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Python算法服务实现
 * 调用Python FastAPI服务
 */
@Service
@Slf4j
public class PythonAlgorithmServiceImpl implements PythonAlgorithmService {

    @Value("${python.algorithm.base-url:http://localhost:8000}")
    private String pythonBaseUrl;

    @Override
    public RecommendResponse getRecommendations(RecommendRequest request) {
        try {
            String url = pythonBaseUrl + "/api/recommend";
            HttpResponse response = HttpRequest.post(url)
                    .header("Content-Type", "application/json")
                    .body(JSONUtil.toJsonStr(request))
                    .timeout(10000)
                    .execute();

            if (response.isOk()) {
                return JSONUtil.toBean(response.body(), RecommendResponse.class);
            } else {
                log.error("Python算法服务调用失败，status={}, body={}", 
                        response.getStatus(), response.body());
            }
        } catch (Exception e) {
            log.error("调用Python算法服务异常", e);
        }
        
        // 返回空结果
        return new RecommendResponse();
    }

    @Override
    public float[] computeEmbedding(String text) {
        try {
            String url = pythonBaseUrl + "/api/embedding";
            HttpResponse response = HttpRequest.post(url)
                    .header("Content-Type", "application/json")
                    .body(JSONUtil.createObj().set("text", text).toString())
                    .timeout(10000)
                    .execute();

            if (response.isOk()) {
                // 解析返回的向量数组
                Object[] arr = JSONUtil.parseObj(response.body())
                        .getJSONArray("embedding")
                        .toArray();
                float[] result = new float[arr.length];
                for (int i = 0; i < arr.length; i++) {
                    result[i] = ((Number) arr[i]).floatValue();
                }
                return result;
            }
        } catch (Exception e) {
            log.error("计算文本向量异常", e);
        }
        return new float[0];
    }

    @Override
    public float computeSimilarity(float[] vector1, float[] vector2) {
        if (vector1 == null || vector2 == null || vector1.length != vector2.length) {
            return 0f;
        }
        
        // 计算余弦相似度
        float dotProduct = 0f;
        float norm1 = 0f;
        float norm2 = 0f;
        
        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            norm1 += vector1[i] * vector1[i];
            norm2 += vector2[i] * vector2[i];
        }
        
        if (norm1 == 0 || norm2 == 0) {
            return 0f;
        }
        
        return dotProduct / (float) (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
