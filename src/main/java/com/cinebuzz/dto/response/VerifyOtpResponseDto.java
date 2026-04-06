package com.cinebuzz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VerifyOtpResponseDto {

    /** Short-lived JWT; send with complete-registration (not as Bearer for API access). */
    private final String registrationToken;
}
