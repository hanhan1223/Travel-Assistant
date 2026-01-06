package org.example.travel.service.impl;

import cn.hutool.core.util.RandomUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

import org.example.travel.exception.BusinessException;
import org.example.travel.exception.ErrorCode;
import org.example.travel.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String EMAIL_CODE_PREFIX = "EMAIL_CODE:";
    private static final String EMAIL_SEND_LIMIT_PREFIX = "EMAIL_SEND_LIMIT:";
    private static final int CODE_EXPIRE_TIME = 5 * 60; // 5分钟
    private static final int SEND_LIMIT_TIME = 60; // 1分钟内不能重复发送
    private static final int MAX_SEND_COUNT = 3; // 每天最多发送3次

    /**
     * 生成并发送验证码
     */
    public void sendVerificationCode(String email) {
        // 检查发送频率限制
        String limitKey = EMAIL_SEND_LIMIT_PREFIX + email;
        String countStr = redisTemplate.opsForValue().get(limitKey);
        int count = countStr == null ? 0 : Integer.parseInt(countStr);

        if (count >= MAX_SEND_COUNT) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, "今日发送验证码次数已达上限");
        }

        // 检查是否在冷却期内
        String lastSendTime = redisTemplate.opsForValue().get(limitKey + ":last");
        if (lastSendTime != null) {
            long lastTime = Long.parseLong(lastSendTime);
            if (System.currentTimeMillis() - lastTime < SEND_LIMIT_TIME * 1000) {
                throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS, "发送过于频繁，请稍后再试");
            }
        }

        // 生成6位随机数字验证码
        String code = RandomUtil.randomNumbers(6);

        // 存储验证码，设置5分钟过期
        String codeKey = EMAIL_CODE_PREFIX + email;
        redisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE_TIME, TimeUnit.SECONDS);
        log.info("验证码已存储 - key: {}, code: {}", codeKey, code);

        // 更新发送次数和时间
        redisTemplate.opsForValue().set(limitKey, String.valueOf(count + 1), 24, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(limitKey + ":last", String.valueOf(System.currentTimeMillis()), 24,
                TimeUnit.HOURS);

        // 发送邮件
        sendEmail(email, code);
    }

    /**
     * 验证验证码
     */
    public boolean verifyCode(String email, String code) {
        String key = EMAIL_CODE_PREFIX + email;
        String storedCode = redisTemplate.opsForValue().get(key);
        log.info("验证码校验 - key: {}, 存储的验证码: [{}], 用户输入的验证码: [{}]", key, storedCode, code);
        if (storedCode == null) {
            log.warn("验证码不存在或已过期，key: {}", key);
            return false;
        }
        boolean result = storedCode.equals(code);
        log.info("验证码比对结果: {}", result);
        return result;
    }

    /**
     * 清除验证码（验证成功后调用）
     */
    public void clearCode(String email) {
        String key = EMAIL_CODE_PREFIX + email;
        redisTemplate.delete(key);
    }

    /**
     * 使用 JavaMailSender 发送验证码邮件
     */
    private void sendEmail(String toEmail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("【非遗文化智能伴游】邮箱验证");

            String htmlContent = buildVerificationEmailTemplate(code);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("验证码邮件发送成功，收件人: {}", toEmail);
        } catch (MessagingException e) {
            log.error("发送邮件失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "邮件发送失败");
        } catch (Exception e) {
            log.error("发送邮件异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "邮件发送异常");
        }
    }

    /**
     * 构建验证码邮件HTML模板
     */
    private String buildVerificationEmailTemplate(String code) {
        return "<div style='font-family: \"Microsoft YaHei\", Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);'>"
                + "  <div style='background-color: white; border-radius: 12px; padding: 40px; box-shadow: 0 4px 20px rgba(0,0,0,0.15);'>"
                + "    <div style='text-align: center; margin-bottom: 30px;'>"
                + "      <h1 style='color: #764ba2; font-size: 24px; margin: 0;'>🏮 非遗文化智能伴游</h1>"
                + "      <p style='color: #999; font-size: 14px; margin-top: 8px;'>探索岭南非遗，感受文化魅力</p>"
                + "    </div>"
                + "    <hr style='border: none; border-top: 2px solid #f0f0f0; margin: 20px 0;'>"
                + "    <p style='color: #333; font-size: 16px; line-height: 1.8;'>尊敬的用户，您好！</p>"
                + "    <p style='color: #666; font-size: 16px; line-height: 1.8;'>感谢您注册非遗文化智能伴游平台。我们致力于为您提供最优质的非遗文化探索体验。</p>"
                + "    <p style='color: #666; font-size: 16px; line-height: 1.8;'>请使用以下验证码完成您的注册：</p>"
                + "    <div style='text-align: center; margin: 35px 0;'>"
                + "      <div style='display: inline-block; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 20px 40px; border-radius: 12px; box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);'>"
                + "        <span style='font-size: 36px; font-weight: bold; color: #fff; letter-spacing: 8px;'>" + code + "</span>"
                + "      </div>"
                + "    </div>"
                + "    <div style='background-color: #fff8e1; border-left: 4px solid #ffc107; padding: 15px; border-radius: 4px; margin: 25px 0;'>"
                + "      <p style='color: #856404; font-size: 14px; margin: 0;'>⏰ 验证码将在 <strong>5分钟</strong> 后失效，请尽快使用。</p>"
                + "    </div>"
                + "    <p style='color: #999; font-size: 14px; line-height: 1.6;'>如果您没有进行此操作，请忽略此邮件。如有疑问，请联系我们的客服团队。</p>"
                + "    <hr style='border: none; border-top: 1px solid #eee; margin: 30px 0;'>"
                + "    <div style='text-align: center;'>"
                + "      <p style='color: #999; font-size: 12px; margin: 5px 0;'>© 2026 非遗文化智能伴游平台</p>"
                + "      <p style='color: #999; font-size: 12px; margin: 5px 0;'>传承非遗文化 · 智享岭南之旅</p>"
                + "    </div>"
                + "  </div>"
                + "</div>";
    }
}
