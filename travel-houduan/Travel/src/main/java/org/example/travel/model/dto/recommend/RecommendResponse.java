package org.example.travel.model.dto.recommend;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 推荐响应DTO - Python算法服务返回
 */
@Data
public class RecommendResponse {
    /**
     * 响应码
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 推荐数据列表
     */
    private List<RecommendItem> data;
    
    /**
     * 总数
     */
    private Integer total;
    
    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return code != null && code == 200;
    }
    
    /**
     * 判断是否有数据
     */
    public boolean hasData() {
        return data != null && !data.isEmpty();
    }
    
    @Data
    public static class RecommendItem {
        /**
         * ID（商户ID或非遗项目ID）
         */
        private Long id;
        
        /**
         * 类型：merchant-商户，ich_project-非遗项目
         */
        private String type;
        
        /**
         * 名称
         */
        private String name;
        
        /**
         * 分类
         */
        private String category;
        
        /**
         * 纬度
         */
        private BigDecimal lat;
        
        /**
         * 经度
         */
        private BigDecimal lng;
        
        /**
         * 距离（公里）
         */
        private Double distance;
        
        /**
         * 评分
         */
        private BigDecimal rating;
        
        /**
         * 推荐分数
         */
        private Double score;
        
        /**
         * 推荐理由列表
         */
        private List<String> reasons;
        
        /**
         * 获取格式化的距离字符串
         */
        public String getFormattedDistance() {
            if (distance == null) return "";
            if (distance < 1) {
                return String.format("%.0f米", distance * 1000);
            }
            return String.format("%.1f公里", distance);
        }
        
        /**
         * 获取合并的推荐理由
         */
        public String getCombinedReasons() {
            if (reasons == null || reasons.isEmpty()) return "";
            return String.join("，", reasons);
        }
        
        /**
         * 是否是商户类型
         */
        public boolean isMerchant() {
            return "merchant".equals(type);
        }
        
        /**
         * 是否是非遗项目类型
         */
        public boolean isIchProject() {
            return "ich_project".equals(type);
        }
    }
}
