package org.example.travel.service;


public interface EmailService {

    /**
     * 发送验证码
     * @param email
     */
    void sendVerificationCode(String email);

    void clearCode(String email);

    boolean verifyCode(String email, String code);
}
