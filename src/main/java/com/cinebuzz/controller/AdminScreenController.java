package com.cinebuzz.controller;

import com.cinebuzz.dto.request.ScreenRequestDto;
import com.cinebuzz.dto.response.ApiResponse;
import com.cinebuzz.dto.response.ScreenResponseDto;
import com.cinebuzz.service.ScreenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/screens")
public class AdminScreenController {

    @Autowired
    private ScreenService screenService;

    @PostMapping
    public ResponseEntity<ApiResponse<ScreenResponseDto>> createScreen(
            @Valid @RequestBody ScreenRequestDto dto) {
        ScreenResponseDto data = screenService.createScreen(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Screen created successfully", data));
    }

    @GetMapping("/theatre/{theatreId}")
    public ResponseEntity<ApiResponse<List<ScreenResponseDto>>> getScreensByTheatre(
            @PathVariable Long theatreId) {
        List<ScreenResponseDto> data = screenService.getScreensByTheatre(theatreId);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Screens fetched successfully", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteScreen(@PathVariable Long id) {
        screenService.deleteScreen(id);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Screen deleted successfully", null));
    }
}