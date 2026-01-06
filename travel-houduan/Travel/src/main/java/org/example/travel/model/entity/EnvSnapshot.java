package org.example.travel.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 环境快照表
 * @TableName env_snapshot
 */
@TableName(value ="env_snapshot")
@Data
public class EnvSnapshot {
    /**
     * 环境快照ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 纬度
     */
    private BigDecimal lat;

    /**
     * 经度
     */
    private BigDecimal lng;

    /**
     * 天气
     */
    private String weather;

    /**
     * 人流拥挤度
     */
    @TableField("crowd_level")
    private Integer crowdLevel;

    /**
     * 记录时间
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
        EnvSnapshot other = (EnvSnapshot) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getLat() == null ? other.getLat() == null : this.getLat().equals(other.getLat()))
            && (this.getLng() == null ? other.getLng() == null : this.getLng().equals(other.getLng()))
            && (this.getWeather() == null ? other.getWeather() == null : this.getWeather().equals(other.getWeather()))
            && (this.getCrowdLevel() == null ? other.getCrowdLevel() == null : this.getCrowdLevel().equals(other.getCrowdLevel()))
            && (this.getCreatedAt() == null ? other.getCreatedAt() == null : this.getCreatedAt().equals(other.getCreatedAt()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getLat() == null) ? 0 : getLat().hashCode());
        result = prime * result + ((getLng() == null) ? 0 : getLng().hashCode());
        result = prime * result + ((getWeather() == null) ? 0 : getWeather().hashCode());
        result = prime * result + ((getCrowdLevel() == null) ? 0 : getCrowdLevel().hashCode());
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
        sb.append(", lat=").append(lat);
        sb.append(", lng=").append(lng);
        sb.append(", weather=").append(weather);
        sb.append(", crowdLevel=").append(crowdLevel);
        sb.append(", createdAt=").append(createdAt);
        sb.append("]");
        return sb.toString();
    }
}