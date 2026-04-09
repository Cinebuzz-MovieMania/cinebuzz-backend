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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@Service
public class AuthMailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${cinebuzz.mail.from:cinebuzz2003@gmail.com}")
    private String fromAddress;

    @Value("${spring.mail.password:}")
    private String mailPasswordRaw;

    @Value("${cinebuzz.mail.send-login-welcome:true}")
    private boolean sendLoginWelcome;

    private String mailPassword;

    private static final DateTimeFormatter SENT_AT =
            DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a", Locale.ENGLISH);

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

    /**
     * Sent after successful account creation. Does not throw — failures are logged only.
     */
    @Async
    public void sendWelcomeAfterRegistration(String toEmail, String displayName) {
        sendWelcomeHtml(
                toEmail,
                displayName,
                "Welcome to CineBuzz",
                """
                <p style="margin:0 0 16px;">Thanks for joining us. Your account is ready — browse <strong>Now Showing</strong>, pick a showtime, and book seats in a few taps.</p>
                <p style="margin:0 0 16px;">If you ever need help, just reply to this email or visit our site and sign in with the address you used to register.</p>
                <p style="margin:0;color:#666;font-size:14px;">Enjoy the show!</p>
                """);
    }

    /**
     * Sent after successful login (optional; see {@code cinebuzz.mail.send-login-welcome}). Does not throw.
     */
    @Async
    public void sendWelcomeBackLogin(String toEmail, String displayName) {
        if (!sendLoginWelcome) {
            log.debug("[mail] Login welcome email disabled (cinebuzz.mail.send-login-welcome=false)");
            return;
        }
        String when = LocalDateTime.now().format(SENT_AT);
        sendWelcomeHtml(
                toEmail,
                displayName,
                "Welcome back to CineBuzz",
                """
                <p style="margin:0 0 16px;">You have successfully signed in to your account.</p>
                <p style="margin:0 0 16px;color:#666;font-size:14px;">Sign-in time: %s</p>
                <p style="margin:0;color:#666;font-size:14px;">If this wasn&apos;t you, change your password from the site or use &quot;Forgot password&quot; to secure your account.</p>
                """
                        .formatted(when));
    }

    private void sendWelcomeHtml(String toEmail, String displayName, String subject, String innerBodyHtml) {
        if (!StringUtils.hasText(toEmail)) {
            log.warn("[mail] Skip welcome email: empty recipient");
            return;
        }
        if (!StringUtils.hasText(mailPassword)) {
            log.debug("[mail] Skip welcome email — mail not configured");
            return;
        }
        String safeName = HtmlUtils.htmlEscape(displayName != null ? displayName.trim() : "there");
        String fullHtml =
                """
                <!DOCTYPE html>
                <html><body style="margin:0;padding:0;background:#f4f6f8;">
                <table role="presentation" cellpadding="0" cellspacing="0" width="100%%" style="background:#f4f6f8;padding:24px 12px;">
                <tr><td align="center">
                <table role="presentation" cellpadding="0" cellspacing="0" width="100%%" style="max-width:560px;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 24px rgba(0,0,0,0.08);">
                <tr><td style="background:#e50914;padding:20px 24px;">
                <p style="margin:0;font-size:22px;font-weight:800;color:#ffffff;letter-spacing:-0.02em;">CineBuzz</p>
                </td></tr>
                <tr><td style="padding:28px 24px 32px;font-family:Segoe UI,Roboto,Helvetica,Arial,sans-serif;line-height:1.55;color:#1a1d21;font-size:16px;">
                <p style="margin:0 0 16px;font-size:18px;font-weight:700;">Hello %s,</p>
                %s
                <p style="margin:24px 0 0;font-size:12px;color:#999;">This is an automated message from CineBuzz.</p>
                </td></tr>
                </table>
                </td></tr>
                </table>
                </body></html>
                """
                        .formatted(safeName, innerBodyHtml);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(fullHtml, true);
            mailSender.send(message);
            log.info("[mail] {} sent to {}", subject, toEmail);
        } catch (MailAuthenticationException e) {
            log.warn("[mail] Welcome email SMTP auth failed for {}: {}", toEmail, e.getMessage());
        } catch (MessagingException e) {
            log.warn("[mail] Welcome email failed for {}: {}", toEmail, e.getMessage());
        } catch (Exception e) {
            log.warn("[mail] Welcome email unexpected error for {}: {}", toEmail, e.getMessage());
        }
    }
}
