package com.cinebuzz.controller;

import com.cinebuzz.dto.request.ShowtimeRequestDto;
import com.cinebuzz.dto.response.ApiResponse;
import com.cinebuzz.dto.response.ShowtimeResponseDto;
import com.cinebuzz.service.ShowtimeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/showtimes")
public class AdminShowtimeController {

    @Autowired
    private ShowtimeService showtimeService;

    @PostMapping
    public ResponseEntity<ApiResponse<ShowtimeResponseDto>> createShowtime(
            @Valid @RequestBody ShowtimeRequestDto dto) {
        ShowtimeResponseDto data = showtimeService.createShowtime(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Showtime created successfully", data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ShowtimeResponseDto>>> getAllShowtimes() {
        List<ShowtimeResponseDto> data = showtimeService.getAllShowtimes();
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Showtimes fetched successfully", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ShowtimeResponseDto>> getShowtimeById(@PathVariable Long id) {
        ShowtimeResponseDto data = showtimeService.getShowtimeById(id);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Showtime fetched successfully", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteShowtime(@PathVariable Long id) {
        showtimeService.deleteShowtime(id);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Showtime deleted successfully", null));
    }
}