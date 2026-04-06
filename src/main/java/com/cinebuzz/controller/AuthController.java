package com.cinebuzz.controller;

import com.cinebuzz.dto.request.CompleteRegistrationRequestDto;
import com.cinebuzz.dto.request.EmailRequestDto;
import com.cinebuzz.dto.request.LoginRequestDto;
import com.cinebuzz.dto.request.ResetPasswordRequestDto;
import com.cinebuzz.dto.request.VerifyRegistrationOtpRequestDto;
import com.cinebuzz.dto.response.ApiResponse;
import com.cinebuzz.dto.response.AuthResponseDto;
import com.cinebuzz.dto.response.SendOtpResponseDto;
import com.cinebuzz.dto.response.VerifyOtpResponseDto;
import com.cinebuzz.service.AuthService;
import com.cinebuzz.service.PasswordResetService;
import com.cinebuzz.service.RegistrationOtpService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private RegistrationOtpService registrationOtpService;

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/forgot-password/request")
    public ResponseEntity<ApiResponse<SendOtpResponseDto>> forgotPasswordRequest(@Valid @RequestBody EmailRequestDto dto) {
        SendOtpResponseDto data = passwordResetService.requestSendCode(dto.getEmail());
        return ResponseEntity.ok(new ApiResponse<>(true,
                "Check your email for an 8-character reset code.", data));
    }

    @PostMapping("/forgot-password/resend")
    public ResponseEntity<ApiResponse<SendOtpResponseDto>> forgotPasswordResend(@Valid @RequestBody EmailRequestDto dto) {
        SendOtpResponseDto data = passwordResetService.resendCode(dto.getEmail());
        return ResponseEntity.ok(new ApiResponse<>(true,
                "A new reset code was sent to your email.", data));
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<ApiResponse<Void>> forgotPasswordReset(@Valid @RequestBody ResetPasswordRequestDto dto) {
        passwordResetService.resetPassword(dto.getEmail(), dto.getCode(), dto.getNewPassword());
        return ResponseEntity.ok(new ApiResponse<>(true, "Password updated. You can sign in with your new password.", null));
    }

    @PostMapping("/register/send-otp")
    public ResponseEntity<ApiResponse<SendOtpResponseDto>> sendRegistrationOtp(@Valid @RequestBody EmailRequestDto dto) {
        SendOtpResponseDto data = registrationOtpService.sendOtp(dto.getEmail());
        return ResponseEntity.ok(new ApiResponse<>(true, "Verification code sent to your email.", data));
    }

    @PostMapping("/register/resend-otp")
    public ResponseEntity<ApiResponse<SendOtpResponseDto>> resendRegistrationOtp(@Valid @RequestBody EmailRequestDto dto) {
        SendOtpResponseDto data = registrationOtpService.resendOtp(dto.getEmail());
        return ResponseEntity.ok(new ApiResponse<>(true, "A new verification code was sent.", data));
    }

    @PostMapping("/register/verify-otp")
    public ResponseEntity<ApiResponse<VerifyOtpResponseDto>> verifyRegistrationOtp(
            @Valid @RequestBody VerifyRegistrationOtpRequestDto dto) {
        VerifyOtpResponseDto data = registrationOtpService.verifyOtp(dto.getEmail(), dto.getOtp());
        return ResponseEntity.ok(new ApiResponse<>(true, "Email verified. Continue to set your name and password.", data));
    }

    @PostMapping("/register/complete")
    public ResponseEntity<ApiResponse<AuthResponseDto>> completeRegistration(
            @Valid @RequestBody CompleteRegistrationRequestDto dto) {
        AuthResponseDto data = authService.completeRegistration(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Account created successfully.", data));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(
            @Valid @RequestBody LoginRequestDto dto) {
        AuthResponseDto data = authService.login(dto);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Login successful", data));
    }
}
