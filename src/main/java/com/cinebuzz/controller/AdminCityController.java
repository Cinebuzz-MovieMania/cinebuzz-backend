package com.cinebuzz.controller;

import com.cinebuzz.dto.request.CityRequestDto;
import com.cinebuzz.dto.response.CityResponseDto;
import com.cinebuzz.service.CityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/cities")
public class AdminCityController {

    @Autowired
    private CityService cityService;

    @PostMapping
    public ResponseEntity<CityResponseDto> createCity(@RequestBody CityRequestDto dto) {
        return null;
    }

    @GetMapping
    public ResponseEntity<List<CityResponseDto>> getAllCities() {
        return null;
    }

    @PutMapping("/{id}")
    public ResponseEntity<CityResponseDto> updateCity(@PathVariable Long id,
                                                      @RequestBody CityRequestDto dto) {
        return null;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCity(@PathVariable Long id) {
        return null;
    }
}