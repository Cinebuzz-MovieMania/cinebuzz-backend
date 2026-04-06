package com.cinebuzz.service;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class AuthMailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${cinebuzz.mail.from:cinebuzz2003@gmail.com}")
    private String fromAddress;

    @Value("${spring.mail.password:}")
    private String mailPasswordRaw;

    private String mailPassword;

    @PostConstruct
    void trimMailPassword() {
        mailPassword = mailPasswordRaw != null ? mailPasswordRaw.trim() : "";
    }

    public void sendRegistrationOtp(String toEmail, String otp, int expiresInSeconds) {
        if (!StringUtils.hasText(toEmail)) {
            throw new IllegalArgumentException("Recipient email is required");
        }
        if (!StringUtils.hasText(mailPassword)) {
            throw new IllegalStateException(
                    "Mail is not configured (spring.mail.password). Cannot send registration OTP.");
        }

        String safeOtp = HtmlUtils.htmlEscape(otp);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("Cinebuzz — Your verification code");
            helper.setText(
                    """
                    <!DOCTYPE html>
                    <html><body style="font-family:Segoe UI,Roboto,sans-serif;line-height:1.5;color:#222;">
                    <p>Your verification code is:</p>
                    <p style="font-size:28px;font-weight:700;letter-spacing:4px;">%s</p>
                    <p style="color:#666;font-size:14px;">This code expires in %d minutes.</p>
                    <p style="font-size:12px;color:#999;">If you did not request this, you can ignore this email.</p>
                    </body></html>
                    """
                            .formatted(safeOtp, Math.max(1, expiresInSeconds / 60)),
                    true);
            mailSender.send(message);
            log.info("[mail] Registration OTP sent to {}", toEmail);
        } catch (MailAuthenticationException e) {
            log.error("[mail] SMTP auth failed while sending registration OTP: {}", e.getMessage());
            throw new IllegalStateException("Could not send email. Check mail configuration.", e);
        } catch (MessagingException e) {
            log.error("[mail] Failed to send registration OTP: {}", e.getMessage());
            throw new IllegalStateException("Could not send verification email.", e);
        }
    }

    public void sendPasswordResetCode(String toEmail, String code, int expiresInSeconds) {
        if (!StringUtils.hasText(toEmail)) {
            throw new IllegalArgumentException("Recipient email is required");
        }
        if (!StringUtils.hasText(mailPassword)) {
            throw new IllegalStateException(
                    "Mail is not configured (spring.mail.password). Cannot send password reset email.");
        }

        String safeCode = HtmlUtils.htmlEscape(code);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("Cinebuzz — Reset your password");
            helper.setText(
                    """
                    <!DOCTYPE html>
                    <html><body style="font-family:Segoe UI,Roboto,sans-serif;line-height:1.5;color:#222;">
                    <p>Use this one-time code to set a new password:</p>
                    <p style="font-size:26px;font-weight:700;letter-spacing:3px;">%s</p>
                    <p style="color:#666;font-size:14px;">This code expires in %d minutes. If you did not request a reset, ignore this email.</p>
                    </body></html>
                    """
                            .formatted(safeCode, Math.max(1, expiresInSeconds / 60)),
                    true);
            mailSender.send(message);
            log.info("[mail] Password reset code sent to {}", toEmail);
        } catch (MailAuthenticationException e) {
            log.error("[mail] SMTP auth failed while sending password reset: {}", e.getMessage());
            throw new IllegalStateException("Could not send email. Check mail configuration.", e);
        } catch (MessagingException e) {
            log.error("[mail] Failed to send password reset email: {}", e.getMessage());
            throw new IllegalStateException("Could not send password reset email.", e);
        }
    }
}
