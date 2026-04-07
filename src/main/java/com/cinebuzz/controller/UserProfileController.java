package com.cinebuzz.controller;

import com.cinebuzz.dto.request.UpdateProfileRequestDto;
import com.cinebuzz.dto.response.ApiResponse;
import com.cinebuzz.dto.response.UserProfileDto;
import com.cinebuzz.entity.User;
import com.cinebuzz.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
public class UserProfileController {

    @Autowired
    private AuthService authService;

    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = requireCurrentUser(userDetails);
        UserProfileDto data = authService.updateProfileName(user.getId(), dto.getName());
        log.info("[api] PATCH /users/me userId={}", user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Profile updated", data));
    }

    private static User requireCurrentUser(UserDetails details) {
        if (!(details instanceof User u)) {
            throw new IllegalStateException("Expected domain User principal");
        }
        return u;
    }
}
