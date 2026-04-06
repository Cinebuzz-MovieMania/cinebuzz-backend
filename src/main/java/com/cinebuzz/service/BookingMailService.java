package com.cinebuzz.service;

import com.cinebuzz.dto.response.BookingResponseDto;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.HtmlUtils;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@Service
public class BookingMailService {

    private static final DateTimeFormatter SHOW_TIME =
            DateTimeFormatter.ofPattern("EEE, d MMM yyyy 'at' h:mm a", Locale.ENGLISH);

    private static final DateTimeFormatter BOOKED_AT =
            DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a", Locale.ENGLISH);

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

    @EventListener(ApplicationReadyEvent.class)
    public void logMailReadiness() {
        if (!StringUtils.hasText(mailPassword)) {
            log.warn("[mail] spring.mail.password is empty — booking emails will be skipped. "
                    + "Copy application-local.yml.example to application-local.yml and set the App Password (same folder as application.yml), "
                    + "or set env MAIL_PASSWORD or SPRING_MAIL_PASSWORD in your IDE run config / OS environment.");
        } else {
            log.info("[mail] SMTP ready for sender {} (app password length={} — expected 16 for Gmail)",
                    fromAddress, mailPassword.length());
        }
    }

    public void sendBookingConfirmation(String toEmail, BookingResponseDto dto) {
        if (!StringUtils.hasText(toEmail)) {
            log.warn("[mail] Skip confirmation: empty recipient");
            return;
        }
        if (!StringUtils.hasText(mailPassword)) {
            log.warn("[mail] Skip confirmation: set env MAIL_PASSWORD to the Gmail App Password for {}", fromAddress);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(fromAddress);
            helper.setTo(toEmail);
            helper.setSubject("Cinebuzz — Booking confirmed #" + dto.getBookingId());
            helper.setText(buildHtmlBody(dto), true);
            mailSender.send(message);
            log.info("[mail] Booking confirmation sent bookingId={} to={}", dto.getBookingId(), toEmail);
        } catch (MailAuthenticationException e) {
            log.error(
                    "[mail] Gmail rejected SMTP login (535). Use a 16-char App Password for {}, not your normal Gmail password. "
                            + "Revoke old app passwords in Google Account / Security / App passwords, create a new one, "
                            + "set MAIL_PASSWORD (no spaces), restart the app from the same environment. Underlying: {}",
                    fromAddress, e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        } catch (MessagingException e) {
            log.error("[mail] Failed to send booking confirmation bookingId={} to={}: {}",
                    dto.getBookingId(), toEmail, e.getMessage());
        } catch (Exception e) {
            log.error("[mail] Failed to send booking confirmation bookingId={}", dto.getBookingId(), e);
        }
    }

    private String buildHtmlBody(BookingResponseDto b) {
        String movie = esc(b.getMovieTitle());
        String theatre = esc(b.getTheatreName());
        String screen = esc(b.getScreenName());
        String userName = esc(b.getUserName());
        String seats = esc(String.join(", ", b.getSeatLabels() != null ? b.getSeatLabels() : java.util.List.of()));
        String showTime = b.getStartTime() != null ? esc(b.getStartTime().format(SHOW_TIME)) : "—";
        String bookedAt = b.getCreatedAt() != null ? esc(b.getCreatedAt().format(BOOKED_AT)) : "—";
        String total = b.getTotalAmount() != null ? esc(b.getTotalAmount().toPlainString()) : "—";

        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family:Segoe UI,Roboto,sans-serif;line-height:1.5;color:#222;">
                <h2 style="margin:0 0 12px;">Booking confirmed</h2>
                <p>Hi %s,</p>
                <p>Your tickets are booked. Here are the details:</p>
                <table cellpadding="8" style="border-collapse:collapse;background:#f8f8fa;border-radius:8px;">
                <tr><td><strong>Booking ID</strong></td><td>#%s</td></tr>
                <tr><td><strong>Movie</strong></td><td>%s</td></tr>
                <tr><td><strong>Theatre</strong></td><td>%s</td></tr>
                <tr><td><strong>Screen</strong></td><td>%s</td></tr>
                <tr><td><strong>Show time</strong></td><td>%s</td></tr>
                <tr><td><strong>Seats</strong></td><td>%s</td></tr>
                <tr><td><strong>Total</strong></td><td>₹%s</td></tr>
                <tr><td><strong>Booked at</strong></td><td>%s</td></tr>
                </table>
                <p style="margin-top:20px;font-size:13px;color:#666;">Thank you for choosing Cinebuzz.</p>
                <p style="font-size:12px;color:#999;">This is an automated message. Please do not reply.</p>
                </body>
                </html>
                """
                .formatted(
                        userName,
                        b.getBookingId(),
                        movie,
                        theatre,
                        screen,
                        showTime,
                        seats,
                        total,
                        bookedAt
                );
    }

    private static String esc(String s) {
        return s == null ? "" : HtmlUtils.htmlEscape(s);
    }
}
