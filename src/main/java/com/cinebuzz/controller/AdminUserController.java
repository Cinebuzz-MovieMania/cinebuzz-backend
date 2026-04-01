package com.cinebuzz.controller;

import com.cinebuzz.dto.response.ApiResponse;
import com.cinebuzz.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    @Autowired
    private AuthService authService;

    @PutMapping("/{userId}/promote")
    public ResponseEntity<ApiResponse<Void>> promoteToAdmin(@PathVariable Long userId) {
        authService.promoteToAdmin(userId);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "User promoted to admin successfully", null));
    }
}
