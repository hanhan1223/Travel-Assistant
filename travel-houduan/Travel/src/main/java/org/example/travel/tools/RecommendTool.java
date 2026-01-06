package org.example.travel.tools;

import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import org.example.travel.model.dto.env.EnvContextDTO;
import org.example.travel.model.dto.recommend.RecommendRequest;
import org.example.travel.model.dto.recommend.RecommendResponse;
import org.example.travel.model.entity.EnvSnapshot;
import org.example.travel.service.EnvContextService;
import org.example.travel.service.EnvSnapshotService;
import org.example.travel.service.PythonAlgorithmService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 智能推荐工具
 * 调用Python算法服务进行个性化推荐
 */
@Component
public class RecommendTool {

    private static final ThreadLocal<Long> currentUserIdHolder = new ThreadLocal<>();

    @Resource
    private PythonAlgorithmService pythonAlgorithmService;

    @Resource
    private EnvContextService envContextService;

    @Resource
    private EnvSnapshotService envSnapshotService;

    /**
     * 设置当前用户ID（在聊天服务中调用）
     */
    public static void setCurrentUserId(Long userId) {
        currentUserIdHolder.set(userId);
    }

    /**
     * 清理上下文
     */
    public static void clearContext() {
        currentUserIdHolder.remove();
    }

    /**
     * 获取当前用户ID
     */
    private static Long getCurrentUserId() {
        return currentUserIdHolder.get();
    }

    @Tool(description = "根据用户位置、天气、兴趣等因素，智能推荐非遗项目和商户。当用户询问'推荐附近的非遗'、'有什么好玩的'、'推荐体验馆'等问题时使用此工具")
    public String getSmartRecommendations(
            @ToolParam(description = "用户纬度，如 23.129163") BigDecimal lat,
            @ToolParam(description = "用户经度，如 113.264435") BigDecimal lng,
            @ToolParam(description = "用户兴趣标签，多个用逗号分隔，如：手作,戏曲,美食。可为空") String interestTags
    ) {
        try {
            // 获取环境上下文（位置、天气）
            EnvContextDTO envContext = envContextService.getEnvContext(lat, lng);

            // 保存环境快照
            EnvSnapshot snapshot = new EnvSnapshot();
            snapshot.setLat(lat);
            snapshot.setLng(lng);
            snapshot.setWeather(envContext.getWeather());
            snapshot.setCreatedAt(new Date());
            envSnapshotService.save(snapshot);

            // 解析兴趣标签
            List<String> tags = null;
            if (interestTags != null && !interestTags.trim().isEmpty()) {
                tags = Arrays.asList(interestTags.split(","));
            }

            // 构建推荐请求
            RecommendRequest request = RecommendRequest.builder()
                    .userId(getCurrentUserId()) 
                    .lat(lat)
                    .lng(lng)
                    .interestTags(tags)
                    .weather(envContext.getWeather())
                    .outdoorSuitable(envContext.getOutdoorSuitable())
                    .limit(5)
                    .build();

            // 调用Python算法服务
            RecommendResponse response = pythonAlgorithmService.getRecommendations(request);

            // 如果Python服务返回空结果，返回特殊标记让智能体跳过
            if (!response.isSuccess() || !response.hasData()) {
                return "[SKIP_RECOMMEND]";
            }

            // 构建返回结果（使用分号分隔，避免换行符导致JSON解析问题）
            StringBuilder result = new StringBuilder();
            result.append("当前位置：").append(envContext.getCity());
            if (envContext.getDistrict() != null) {
                result.append(envContext.getDistrict());
            }
            result.append("，天气：").append(envContext.getWeather());
            result.append("，温度：").append(envContext.getTemperature()).append("℃。");

            // 按类型分组展示
            List<RecommendResponse.RecommendItem> projects = response.getData().stream()
                    .filter(RecommendResponse.RecommendItem::isIchProject)
                    .toList();
            List<RecommendResponse.RecommendItem> merchants = response.getData().stream()
                    .filter(RecommendResponse.RecommendItem::isMerchant)
                    .toList();

            if (!projects.isEmpty()) {
                result.append("【推荐非遗项目】");
                for (int i = 0; i < projects.size(); i++) {
                    RecommendResponse.RecommendItem item = projects.get(i);
                    result.append(i + 1).append(". ").append(item.getName());
                    result.append("（").append(item.getFormattedDistance()).append("）");
                    String reasons = item.getCombinedReasons();
                    if (!reasons.isEmpty()) {
                        result.append("：").append(reasons);
                    }
                    result.append("；");
                }
            }

            if (!merchants.isEmpty()) {
                result.append("【推荐商户】");
                for (int i = 0; i < merchants.size(); i++) {
                    RecommendResponse.RecommendItem item = merchants.get(i);
                    result.append(i + 1).append(". ").append(item.getName());
                    if (item.getCategory() != null) {
                        result.append("（").append(item.getCategory()).append("）");
                    }
                    result.append(" - ").append(item.getFormattedDistance());
                    if (item.getRating() != null) {
                        result.append("，评分").append(item.getRating());
                    }
                    result.append("；");
                }
            }

            return result.toString();
        } catch (Exception e) {
            return "[SKIP_RECOMMEND]";
        }
    }
}
