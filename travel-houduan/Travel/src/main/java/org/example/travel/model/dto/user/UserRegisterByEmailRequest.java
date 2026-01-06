package org.example.travel.model.dto.user;

import lombok.Data;

@Data
public class UserRegisterByEmailRequest {
    private static final long serialVersionUID = 8735650154179439661L;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 确认密码
     */
    private String checkPassword;

    /**
     * 验证码
     */
    private String code;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;
}
