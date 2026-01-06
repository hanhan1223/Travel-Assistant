package org.example.travel.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 非遗媒体表
 * @TableName ich_media
 */
@TableName(value ="ich_media")
@Data
public class IchMedia {
    /**
     * 媒体ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 非遗项目ID
     */
    @TableField("project_id")
    private Long projectId;

    /**
     * 媒体类型
     */
    @TableField("media_type")
    private String mediaType;

    /**
     * 媒体URL
     */
    @TableField("media_url")
    private String mediaUrl;

    /**
     * 来源（如联网搜索）
     */
    private String source;

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
        IchMedia other = (IchMedia) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getProjectId() == null ? other.getProjectId() == null : this.getProjectId().equals(other.getProjectId()))
            && (this.getMediaType() == null ? other.getMediaType() == null : this.getMediaType().equals(other.getMediaType()))
            && (this.getMediaUrl() == null ? other.getMediaUrl() == null : this.getMediaUrl().equals(other.getMediaUrl()))
            && (this.getSource() == null ? other.getSource() == null : this.getSource().equals(other.getSource()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getProjectId() == null) ? 0 : getProjectId().hashCode());
        result = prime * result + ((getMediaType() == null) ? 0 : getMediaType().hashCode());
        result = prime * result + ((getMediaUrl() == null) ? 0 : getMediaUrl().hashCode());
        result = prime * result + ((getSource() == null) ? 0 : getSource().hashCode());
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
        sb.append(", projectId=").append(projectId);
        sb.append(", mediaType=").append(mediaType);
        sb.append(", mediaUrl=").append(mediaUrl);
        sb.append(", source=").append(source);
        sb.append(", createdAt=").append(createdAt);
        sb.append("]");
        return sb.toString();
    }
}