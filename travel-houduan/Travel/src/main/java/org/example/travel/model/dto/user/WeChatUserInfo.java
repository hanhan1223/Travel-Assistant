package org.example.travel.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 微信用户信息
 */
@Data
public class WeChatUserInfo implements Serializable {

    /**
     * 微信开放平台unionId
     */
    private String unionId;

    /**
     * 微信公众号openId
     */
    private String openId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String headImgUrl;

    private static final long serialVersionUID = 1L;
}
