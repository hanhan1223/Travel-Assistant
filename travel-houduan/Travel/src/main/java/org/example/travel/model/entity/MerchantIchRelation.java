package org.example.travel.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import lombok.Data;

/**
 * 商户-非遗关联表（复合主键：merchant_id + project_id）
 * 
 * @TableName merchant_ich_relation
 */
@TableName(value = "merchant_ich_relation")
@Data
public class MerchantIchRelation {
    /**
     * 商户ID（复合主键之一）
     */
    @TableField(value = "merchant_id")
    private Long merchantId;

    /**
     * 非遗项目ID
     */
    @TableField(value = "project_id")
    private Long projectId;

    /**
     * 关联度评分
     */
    @TableField(value = "relevance_score")
    private BigDecimal relevanceScore;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        MerchantIchRelation other = (MerchantIchRelation) that;
        return (this.getMerchantId() == null ? other.getMerchantId() == null
                : this.getMerchantId().equals(other.getMerchantId()))
                && (this.getProjectId() == null ? other.getProjectId() == null
                        : this.getProjectId().equals(other.getProjectId()))
                && (this.getRelevanceScore() == null ? other.getRelevanceScore() == null
                        : this.getRelevanceScore().equals(other.getRelevanceScore()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getMerchantId() == null) ? 0 : getMerchantId().hashCode());
        result = prime * result + ((getProjectId() == null) ? 0 : getProjectId().hashCode());
        result = prime * result + ((getRelevanceScore() == null) ? 0 : getRelevanceScore().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", merchantId=").append(merchantId);
        sb.append(", projectId=").append(projectId);
        sb.append(", relevanceScore=").append(relevanceScore);
        sb.append("]");
        return sb.toString();
    }
}
