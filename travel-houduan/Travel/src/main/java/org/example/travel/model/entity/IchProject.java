package org.example.travel.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 非遗项目表
 * @TableName ich_project
 */
@TableName(value ="ich_project")
@Data
public class IchProject {
    /**
     * 非遗项目ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 非遗名称
     */
    private String name;

    /**
     * 非遗类别（手作/戏曲等）
     */
    private String category;

    /**
     * 非遗简介
     */
    private String description;

    /**
     * 所属城市
     */
    private String city;

    /**
     * 纬度
     */
    private BigDecimal lat;

    /**
     * 经度
     */
    private BigDecimal lng;

    /**
     * 是否室内（雨天推荐依据）
     */
    @TableField("is_indoor")
    private Integer isIndoor;

    /**
     * 开放状态
     */
    @TableField("open_status")
    private String openStatus;

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
        IchProject other = (IchProject) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
            && (this.getCategory() == null ? other.getCategory() == null : this.getCategory().equals(other.getCategory()))
            && (this.getDescription() == null ? other.getDescription() == null : this.getDescription().equals(other.getDescription()))
            && (this.getCity() == null ? other.getCity() == null : this.getCity().equals(other.getCity()))
            && (this.getLat() == null ? other.getLat() == null : this.getLat().equals(other.getLat()))
            && (this.getLng() == null ? other.getLng() == null : this.getLng().equals(other.getLng()))
            && (this.getIsIndoor() == null ? other.getIsIndoor() == null : this.getIsIndoor().equals(other.getIsIndoor()))
            && (this.getOpenStatus() == null ? other.getOpenStatus() == null : this.getOpenStatus().equals(other.getOpenStatus()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getCategory() == null) ? 0 : getCategory().hashCode());
        result = prime * result + ((getDescription() == null) ? 0 : getDescription().hashCode());
        result = prime * result + ((getCity() == null) ? 0 : getCity().hashCode());
        result = prime * result + ((getLat() == null) ? 0 : getLat().hashCode());
        result = prime * result + ((getLng() == null) ? 0 : getLng().hashCode());
        result = prime * result + ((getIsIndoor() == null) ? 0 : getIsIndoor().hashCode());
        result = prime * result + ((getOpenStatus() == null) ? 0 : getOpenStatus().hashCode());
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
        sb.append(", description=").append(description);
        sb.append(", city=").append(city);
        sb.append(", lat=").append(lat);
        sb.append(", lng=").append(lng);
        sb.append(", isIndoor=").append(isIndoor);
        sb.append(", openStatus=").append(openStatus);
        sb.append(", createdAt=").append(createdAt);
        sb.append("]");
        return sb.toString();
    }
}