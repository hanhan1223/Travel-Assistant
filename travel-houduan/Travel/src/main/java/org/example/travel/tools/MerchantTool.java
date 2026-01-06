package org.example.travel.tools;

import cn.hutool.json.JSONUtil;
import jakarta.annotation.Resource;
import org.example.travel.model.entity.Merchant;
import org.example.travel.service.MerchantService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 商户查询工具
 */
@Component
public class MerchantTool {

    @Resource
    private MerchantService merchantService;

    @Tool(description = "根据商户ID获取商户详细信息，包括名称、类别、评分、第三方链接等")
    public String getMerchantById(
            @ToolParam(description = "商户ID") Long merchantId
    ) {
        Merchant merchant = merchantService.getById(merchantId);
        if (merchant == null) {
            return "未找到该商户";
        }
        return JSONUtil.toJsonStr(merchant);
    }

    @Tool(description = "根据商户类别查询商户列表，如文创店、体验馆、老字号等")
    public String getMerchantsByCategory(
            @ToolParam(description = "商户类别，如：文创店、体验馆、老字号、餐饮") String category
    ) {
        List<Merchant> merchants = merchantService.lambdaQuery()
                .like(Merchant::getCategory, category)
                .list();
        if (merchants.isEmpty()) {
            return "暂无该类别的商户";
        }
        return JSONUtil.toJsonStr(merchants);
    }

    @Tool(description = "根据非遗项目ID查询关联的商户，用于推荐与非遗项目相关的文创店、体验馆等")
    public String getMerchantsByProject(
            @ToolParam(description = "非遗项目ID") Long projectId
    ) {
        // 直接从merchant表查询关联的商户（通过project_id字段）
        List<Merchant> merchants = merchantService.lambdaQuery()
                .eq(Merchant::getProjectId, projectId)
                .orderByDesc(Merchant::getRelevanceScore)
                .list();
        
        if (merchants.isEmpty()) {
            return "该非遗项目暂无关联商户";
        }
        
        return JSONUtil.toJsonStr(merchants);
    }

    @Tool(description = "查询评分较高的优质商户，用于推荐")
    public String getTopRatedMerchants(
            @ToolParam(description = "返回数量，默认10") Integer limit
    ) {
        int actualLimit = limit != null && limit > 0 ? limit : 10;
        List<Merchant> merchants = merchantService.lambdaQuery()
                .orderByDesc(Merchant::getRating)
                .last("LIMIT " + actualLimit)
                .list();
        if (merchants.isEmpty()) {
            return "暂无商户数据";
        }
        return JSONUtil.toJsonStr(merchants);
    }
}
