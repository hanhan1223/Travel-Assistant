package org.example.travel.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 微信登录请求
 */
@Data
public class WeChatLoginRequest implements Serializable {

    /**
     * 微信授权码
     */
    private String code;

    private static final long serialVersionUID = 1L;
}
