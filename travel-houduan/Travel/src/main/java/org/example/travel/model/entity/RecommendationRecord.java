package org.example.travel.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 推荐记录表
 * @TableName recommendation_record
 */
@TableName(value ="recommendation_record")
@Data
public class RecommendationRecord {
    /**
     * 推荐记录ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 环境快照ID
     */
    @TableField("env_id")
    private Long envId;

    /**
     * 推荐策略标识
     */
    private String strategy;

    /**
     * 推荐的非遗项目ID列表
     */
    @TableField("result_ids")
    private Object resultIds;

    /**
     * 推荐时间
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
        RecommendationRecord other = (RecommendationRecord) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
            && (this.getEnvId() == null ? other.getEnvId() == null : this.getEnvId().equals(other.getEnvId()))
            && (this.getStrategy() == null ? other.getStrategy() == null : this.getStrategy().equals(other.getStrategy()))
            && (this.getResultIds() == null ? other.getResultIds() == null : this.getResultIds().equals(other.getResultIds()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
        result = prime * result + ((getEnvId() == null) ? 0 : getEnvId().hashCode());
        result = prime * result + ((getStrategy() == null) ? 0 : getStrategy().hashCode());
        result = prime * result + ((getResultIds() == null) ? 0 : getResultIds().hashCode());
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
        sb.append(", userId=").append(userId);
        sb.append(", envId=").append(envId);
        sb.append(", strategy=").append(strategy);
        sb.append(", resultIds=").append(resultIds);
        sb.append(", createdAt=").append(createdAt);
        sb.append("]");
        return sb.toString();
    }
}