package org.example.travel.utils;

import cn.hutool.crypto.SecureUtil;

import java.nio.charset.StandardCharsets;

/**
 * 配置加密工具类
 * 用于加密敏感配置
 */
public class ConfigEncryptUtil {

    private static final String DEFAULT_KEY = "travel-system-key-2024-secure";

    /**
     * 加密配置值
     */
    public static String encrypt(String value) {
        return encrypt(value, DEFAULT_KEY);
    }

    /**
     * 加密配置值（指定密钥）
     */
    public static String encrypt(String value, String key) {
        byte[] keyBytes = getFixedLengthKey(key, 16);
        return SecureUtil.aes(keyBytes).encryptHex(value);
    }

    /**
     * 解密配置值
     */
    public static String decrypt(String encryptedValue) {
        return decrypt(encryptedValue, DEFAULT_KEY);
    }

    /**
     * 解密配置值（指定密钥）
     */
    public static String decrypt(String encryptedValue, String key) {
        byte[] keyBytes = getFixedLengthKey(key, 16);
        return SecureUtil.aes(keyBytes).decryptStr(encryptedValue);
    }

    /**
     * 获取固定长度的密钥
     */
    private static byte[] getFixedLengthKey(String key, int length) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] result = new byte[length];

        if (keyBytes.length >= length) {
            System.arraycopy(keyBytes, 0, result, 0, length);
        } else {
            for (int i = 0; i < length; i++) {
                result[i] = keyBytes[i % keyBytes.length];
            }
        }

        return result;
    }

    /**
     * 测试加密解密
     */
    public static void main(String[] args) {
        String original = "sk-81eccc95705f446bb0f1d82b4ec6cc1c";
        System.out.println("原文: " + original);

        String encrypted = encrypt(original);
        System.out.println("加密: " + encrypted);

        String decrypted = decrypt(encrypted);
        System.out.println("解密: " + decrypted);

        System.out.println("验证: " + original.equals(decrypted));
    }
}
