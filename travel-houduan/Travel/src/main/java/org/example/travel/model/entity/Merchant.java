package org.example.travel.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 商户表：非遗体验工坊、文创店、老字号
 * @TableName merchant
 */
@TableName(value ="merchant")
@Data
public class Merchant implements Serializable {
    
    private static final long serialVersionUID = 1L;
    /**
     * 商户ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商户名称
     */
    private String name;

    /**
     * 商户类型
     */
    private String category;

    /**
     * 关联非遗项目ID（替代独立关联表，可为空）
     */
    @TableField("project_id")
    private Long projectId;

    /**
     * 与非遗项目的关联度评分
     */
    @TableField("relevance_score")
    private BigDecimal relevanceScore;

    /**
     * 纬度
     */
    private BigDecimal lat;

    /**
     * 经度
     */
    private BigDecimal lng;

    /**
     * 评分
     */
    private BigDecimal rating;

    /**
     * 第三方平台链接
     */
    @TableField("third_party_url")
    private String thirdPartyUrl;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private Date createdAt;

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
        Merchant other = (Merchant) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
            && (this.getCategory() == null ? other.getCategory() == null : this.getCategory().equals(other.getCategory()))
            && (this.getProjectId() == null ? other.getProjectId() == null : this.getProjectId().equals(other.getProjectId()))
            && (this.getRelevanceScore() == null ? other.getRelevanceScore() == null : this.getRelevanceScore().equals(other.getRelevanceScore()))
            && (this.getLat() == null ? other.getLat() == null : this.getLat().equals(other.getLat()))
            && (this.getLng() == null ? other.getLng() == null : this.getLng().equals(other.getLng()))
            && (this.getRating() == null ? other.getRating() == null : this.getRating().equals(other.getRating()))
            && (this.getThirdPartyUrl() == null ? other.getThirdPartyUrl() == null : this.getThirdPartyUrl().equals(other.getThirdPartyUrl()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getCategory() == null) ? 0 : getCategory().hashCode());
        result = prime * result + ((getProjectId() == null) ? 0 : getProjectId().hashCode());
        result = prime * result + ((getRelevanceScore() == null) ? 0 : getRelevanceScore().hashCode());
        result = prime * result + ((getLat() == null) ? 0 : getLat().hashCode());
        result = prime * result + ((getLng() == null) ? 0 : getLng().hashCode());
        result = prime * result + ((getRating() == null) ? 0 : getRating().hashCode());
        result = prime * result + ((getThirdPartyUrl() == null) ? 0 : getThirdPartyUrl().hashCode());
        result = prime * result + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", name=").append(name);
        sb.append(", category=").append(category);
        sb.append(", projectId=").append(projectId);
        sb.append(", relevanceScore=").append(relevanceScore);
        sb.append(", lat=").append(lat);
        sb.append(", lng=").append(lng);
        sb.append(", rating=").append(rating);
        sb.append(", thirdPartyUrl=").append(thirdPartyUrl);
        sb.append(", createdAt=").append(createdAt);
        sb.append("]");
        return sb.toString();
    }
}