package com.cinebuzz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SendOtpResponseDto {

    /** Seconds until the client may request another OTP (resend cooldown). */
    private final int nextResendAllowedInSeconds;

    /** OTP validity window from the moment this OTP was sent. */
    private final int otpExpiresInSeconds;
}
