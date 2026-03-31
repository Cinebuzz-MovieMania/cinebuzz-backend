package com.cinebuzz.controller;

import com.cinebuzz.dto.request.CityRequestDto;
import com.cinebuzz.dto.response.ApiResponse;
import com.cinebuzz.dto.response.CityResponseDto;
import com.cinebuzz.service.CityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/cities")
public class AdminCityController {

    @Autowired
    private CityService cityService;

    @PostMapping
    public ResponseEntity<ApiResponse<CityResponseDto>> createCity(
            @Valid @RequestBody CityRequestDto dto) {
        CityResponseDto data = cityService.createCity(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "City created successfully", data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CityResponseDto>>> getAllCities() {
        List<CityResponseDto> data = cityService.getAllCities();
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Cities fetched successfully", data));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CityResponseDto>> updateCity(
            @PathVariable Long id,
            @Valid @RequestBody CityRequestDto dto) {
        CityResponseDto data = cityService.updateCity(id, dto);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "City updated successfully", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCity(@PathVariable Long id) {
        cityService.deleteCity(id);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "City deleted successfully", null));
    }
}