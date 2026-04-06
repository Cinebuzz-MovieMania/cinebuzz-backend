package com.cinebuzz.service;

import com.cinebuzz.dto.response.SendOtpResponseDto;
import com.cinebuzz.dto.response.VerifyOtpResponseDto;
import com.cinebuzz.entity.RegistrationOtp;
import com.cinebuzz.exception.AlreadyExistsException;
import com.cinebuzz.exception.RateLimitException;
import com.cinebuzz.exception.ValidationException;
import com.cinebuzz.repository.RegistrationOtpRepository;
import com.cinebuzz.repository.UserRepository;
import com.cinebuzz.security.JwtUtil;
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
public class RegistrationOtpService {

    public static final int RESEND_COOLDOWN_SECONDS = 25;
    public static final int OTP_EXPIRATION_MINUTES = 2;
    private static final int OTP_EXPIRATION_SECONDS = OTP_EXPIRATION_MINUTES * 60;

    private final SecureRandom random = new SecureRandom();

    @Autowired
    private RegistrationOtpRepository registrationOtpRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthMailService authMailService;

    public SendOtpResponseDto sendOtp(String emailRaw) {
        return sendOrResendOtp(normalizeEmail(emailRaw), false);
    }

    public SendOtpResponseDto resendOtp(String emailRaw) {
        return sendOrResendOtp(normalizeEmail(emailRaw), true);
    }

    private SendOtpResponseDto sendOrResendOtp(String email, boolean isResend) {
        if (userRepository.existsByEmail(email)) {
            throw new AlreadyExistsException("This email is already registered. Sign in instead.");
        }

        Instant now = Instant.now();
        RegistrationOtp row = registrationOtpRepository.findByEmail(email).orElse(null);

        if (row != null) {
            long secondsSinceSend = Duration.between(row.getLastSentAt(), now).getSeconds();
            if (secondsSinceSend < RESEND_COOLDOWN_SECONDS) {
                int wait = RESEND_COOLDOWN_SECONDS - (int) secondsSinceSend;
                throw new RateLimitException(
                        "Please wait before requesting another code.", Math.max(1, wait));
            }
        }

        String otpPlain = String.format("%06d", random.nextInt(1_000_000));
        String hash = passwordEncoder.encode(otpPlain);
        Instant expiresAt = now.plusSeconds(OTP_EXPIRATION_SECONDS);

        if (row == null) {
            row = RegistrationOtp.builder()
                    .email(email)
                    .otpHash(hash)
                    .expiresAt(expiresAt)
                    .lastSentAt(now)
                    .build();
        } else {
            row.setOtpHash(hash);
            row.setExpiresAt(expiresAt);
            row.setLastSentAt(now);
        }
        registrationOtpRepository.save(row);

        try {
            authMailService.sendRegistrationOtp(email, otpPlain, OTP_EXPIRATION_SECONDS);
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.error("[auth] Failed to send OTP email to {}", email, e);
            registrationOtpRepository.deleteByEmail(email);
            throw new ValidationException(
                    e.getMessage() != null ? e.getMessage() : "Could not send verification email. Try again later.");
        }

        log.info("[auth] OTP {} for email={}", isResend ? "resent" : "sent", email);
        return new SendOtpResponseDto(RESEND_COOLDOWN_SECONDS, OTP_EXPIRATION_SECONDS);
    }

    @Transactional
    public VerifyOtpResponseDto verifyOtp(String emailRaw, String otpPlain) {
        String email = normalizeEmail(emailRaw);
        RegistrationOtp row = registrationOtpRepository
                .findByEmail(email)
                .orElseThrow(() -> new ValidationException("No verification code found for this email. Request a new code."));

        if (Instant.now().isAfter(row.getExpiresAt())) {
            registrationOtpRepository.deleteByEmail(email);
            throw new ValidationException("Verification code expired. Request a new code.");
        }

        if (!passwordEncoder.matches(otpPlain, row.getOtpHash())) {
            throw new ValidationException("Invalid verification code.");
        }

        registrationOtpRepository.deleteByEmail(email);
        String token = jwtUtil.generateRegistrationProofToken(email);
        log.info("[auth] Email verified for registration email={}", email);
        return new VerifyOtpResponseDto(token);
    }

    public static String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
