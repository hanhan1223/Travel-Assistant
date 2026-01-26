package org.example.travel.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 系统配置实体
 */
@Data
@TableName("system_config")
public class SystemConfig implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 配置键（唯一）
     */
    private String configKey;

    /**
     * 配置值（加密存储）
     */
    private String configValue;

    /**
     * 配置分组（ai、database、storage、map等）
     */
    private String configGroup;

    /**
     * 配置描述
     */
    private String description;

    /**
     * 是否加密存储（1-是 0-否）
     */
    private Integer encrypted;

    /**
     * 是否启用（1-启用 0-禁用）
     */
    private Integer enabled;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
