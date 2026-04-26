package org.example.travel.config;

import lombok.Data;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 微信配置
 */
@Configuration
@ConfigurationProperties(prefix = "wechat.mp")
@Data
public class WeChatConfig {

    /**
     * 微信公众号AppID
     */
    private String appId;

    /**
     * 微信公众号AppSecret
     */
    private String appSecret;

    /**
     * 微信公众号Token
     */
    private String token;

    /**
     * 微信公众号AES Key
     */
    private String aesKey;

    @Bean
    public WxMpService wxMpService() {
        WxMpService wxMpService = new WxMpServiceImpl();
        WxMpDefaultConfigImpl config = new WxMpDefaultConfigImpl();
        config.setAppId(appId);
        config.setSecret(appSecret);
        config.setToken(token);
        config.setAesKey(aesKey);
        wxMpService.setWxMpConfigStorage(config);
        return wxMpService;
    }
}
