package com.cinebuzz.service;

import com.cinebuzz.dto.response.SendOtpResponseDto;
import com.cinebuzz.entity.PasswordResetCode;
import com.cinebuzz.entity.User;
import com.cinebuzz.exception.RateLimitException;
import com.cinebuzz.exception.ValidationException;
import com.cinebuzz.repository.PasswordResetCodeRepository;
import com.cinebuzz.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

@Slf4j
@Service
public class PasswordResetService {

    /** Same UX as registration OTP resend. */
    public static final int RESEND_COOLDOWN_SECONDS = 25;

    /** Single-use reset code lifetime. */
    public static final int CODE_EXPIRATION_MINUTES = 15;
    private static final int CODE_EXPIRATION_SECONDS = CODE_EXPIRATION_MINUTES * 60;

    public static final int CODE_LENGTH = 8;

    /**
     * Unambiguous alphanumeric (uppercase + digits, no 0/O/I/1).
     */
    private static final String CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final SecureRandom random = new SecureRandom();

    @Autowired
    private PasswordResetCodeRepository passwordResetCodeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthMailService authMailService;

    public SendOtpResponseDto requestSendCode(String emailRaw) {
        return sendOrResend(RegistrationOtpService.normalizeEmail(emailRaw), false);
    }

    public SendOtpResponseDto resendCode(String emailRaw) {
        return sendOrResend(RegistrationOtpService.normalizeEmail(emailRaw), true);
    }

    private SendOtpResponseDto sendOrResend(String email, boolean isResend) {
        if (!userRepository.existsByEmail(email)) {
            log.info("[auth] Password reset requested for unregistered email: {}", email);
            throw new ValidationException(
                    "No account is registered with this email address. Check the spelling or create an account.");
        }

        Instant now = Instant.now();
        PasswordResetCode row = passwordResetCodeRepository.findByEmail(email).orElse(null);

        if (row != null) {
            long secondsSinceSend = Duration.between(row.getLastSentAt(), now).getSeconds();
            if (secondsSinceSend < RESEND_COOLDOWN_SECONDS) {
                int wait = RESEND_COOLDOWN_SECONDS - (int) secondsSinceSend;
                throw new RateLimitException(
                        "Please wait before requesting another code.", Math.max(1, wait));
            }
        }

        String codePlain = generateCode();
        String hash = passwordEncoder.encode(codePlain);
        Instant expiresAt = now.plusSeconds(CODE_EXPIRATION_SECONDS);

        if (row == null) {
            row = PasswordResetCode.builder()
                    .email(email)
                    .codeHash(hash)
                    .expiresAt(expiresAt)
                    .lastSentAt(now)
                    .build();
        } else {
            row.setCodeHash(hash);
            row.setExpiresAt(expiresAt);
            row.setLastSentAt(now);
        }
        passwordResetCodeRepository.save(row);

        try {
            authMailService.sendPasswordResetCode(email, codePlain, CODE_EXPIRATION_SECONDS);
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.error("[auth] Failed to send password reset email to {}", email, e);
            passwordResetCodeRepository.deleteByEmail(email);
            throw new ValidationException(
                    e.getMessage() != null ? e.getMessage() : "Could not send email. Try again later.");
        }

        log.info("[auth] Password reset code {} for email={}", isResend ? "resent" : "sent", email);
        return new SendOtpResponseDto(RESEND_COOLDOWN_SECONDS, CODE_EXPIRATION_SECONDS);
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_ALPHABET.charAt(random.nextInt(CODE_ALPHABET.length())));
        }
        return sb.toString();
    }

    @Transactional
    public void resetPassword(String emailRaw, String codeRaw, String newPassword) {
        String email = RegistrationOtpService.normalizeEmail(emailRaw);
        String code = normalizeCode(codeRaw);

        if (code.length() != CODE_LENGTH) {
            throw new ValidationException("Invalid or expired reset code.");
        }

        PasswordResetCode row = passwordResetCodeRepository
                .findByEmail(email)
                .orElseThrow(() -> new ValidationException("Invalid or expired reset code."));

        if (Instant.now().isAfter(row.getExpiresAt())) {
            passwordResetCodeRepository.deleteByEmail(email);
            throw new ValidationException("Invalid or expired reset code.");
        }

        if (!passwordEncoder.matches(code, row.getCodeHash())) {
            throw new ValidationException("Invalid or expired reset code.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ValidationException("Invalid or expired reset code."));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        passwordResetCodeRepository.deleteByEmail(email);
        log.info("[auth] Password reset completed for userId={} email={}", user.getId(), email);
    }

    private static String normalizeCode(String code) {
        if (code == null) {
            return "";
        }
        String alnum = code.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
        return alnum;
    }
}
