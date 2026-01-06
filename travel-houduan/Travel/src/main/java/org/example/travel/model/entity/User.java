package org.example.travel.model.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户表
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 账号
     */
    @TableField("userAccount")
    private String useraccount;

    /**
     * 密码
     */
    @TableField("userPassword")
    private String userpassword;

    /**
     * 用户昵称
     */
    @TableField("userName")
    private String username;

    /**
     * 用户头像
     */
    @TableField("userAvatar")
    private String useravatar;

    /**
     * 用户角色: user/admin
     */
    @TableField("userRole")
    private String userrole;

    /**
     * 编辑时间
     */
    @TableField("editTime")
    private Date edittime;

    /**
     * 创建时间
     */
    @TableField("createTime")
    private Date createtime;

    /**
     * 更新时间
     */
    @TableField("updateTime")
    private Date updatetime;

    /**
     * 是否删除
     */
    @TableLogic
    @TableField("isDelete")
    private Integer isdelete;

    /**
     * 邮箱
     */
    @TableField("email")
    private String email;


}