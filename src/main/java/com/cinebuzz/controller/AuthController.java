package com.cinebuzz.controller;

import com.cinebuzz.dto.request.LoginRequestDto;
import com.cinebuzz.dto.request.RegisterRequestDto;
import com.cinebuzz.dto.response.ApiResponse;
import com.cinebuzz.dto.response.AuthResponseDto;
import com.cinebuzz.service.AuthService;
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

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponseDto>> registerUser(
            @Valid @RequestBody RegisterRequestDto dto) {
        AuthResponseDto data = authService.registerUser(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "User registered successfully", data));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(
            @Valid @RequestBody LoginRequestDto dto) {
        AuthResponseDto data = authService.login(dto);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Login successful", data));
    }
}