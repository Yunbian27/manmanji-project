package com.yunbian27.user.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailFrom;

    @Async("asyncExecutor")
    public void sendCode(String toEmail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(toEmail);
            helper.setSubject("慢慢记 - 注册验证码");
            helper.setText(buildHtml(code), true);
            mailSender.send(message);
            log.info("验证码已发送至 {}", toEmail);
        } catch (MessagingException e) {
            log.error("验证码邮件发送失败: {}", toEmail, e);
        }
    }

    private String buildHtml(String code) {
        return """
            <div style="max-width:400px;margin:0 auto;padding:24px;font-family:Inter,sans-serif;">
              <h2 style="color:#000;">慢慢记</h2>
              <p>您的注册验证码是：</p>
              <div style="font-size:32px;font-weight:700;letter-spacing:8px;color:#000;padding:16px 0;">
                %s
              </div>
              <p style="color:#5e5e5e;font-size:14px;">5 分钟内有效，请勿泄露给他人。</p>
            </div>
            """.formatted(code);
    }
}
