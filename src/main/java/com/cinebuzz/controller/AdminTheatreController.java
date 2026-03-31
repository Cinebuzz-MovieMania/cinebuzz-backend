package com.cinebuzz.controller;

import com.cinebuzz.dto.request.TheatreRequestDto;
import com.cinebuzz.dto.response.ApiResponse;
import com.cinebuzz.dto.response.TheatreResponseDto;
import com.cinebuzz.service.TheatreService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/theatres")
public class AdminTheatreController {

    @Autowired
    private TheatreService theatreService;

    @PostMapping
    public ResponseEntity<ApiResponse<TheatreResponseDto>> createTheatre(
            @Valid @RequestBody TheatreRequestDto dto) {
        TheatreResponseDto data = theatreService.createTheatre(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Theatre created successfully", data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TheatreResponseDto>>> getAllTheatres() {
        List<TheatreResponseDto> data = theatreService.getAllTheatres();
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Theatres fetched successfully", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TheatreResponseDto>> getTheatreById(@PathVariable Long id) {
        TheatreResponseDto data = theatreService.getTheatreById(id);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Theatre fetched successfully", data));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TheatreResponseDto>> updateTheatre(
            @PathVariable Long id,
            @Valid @RequestBody TheatreRequestDto dto) {
        TheatreResponseDto data = theatreService.updateTheatre(id, dto);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Theatre updated successfully", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTheatre(@PathVariable Long id) {
        theatreService.deleteTheatre(id);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Theatre deleted successfully", null));
    }
}