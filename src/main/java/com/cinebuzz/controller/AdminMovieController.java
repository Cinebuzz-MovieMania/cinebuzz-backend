package com.cinebuzz.controller;

import com.cinebuzz.dto.request.MovieRequestDto;
import com.cinebuzz.dto.response.ApiResponse;
import com.cinebuzz.dto.response.MovieResponseDto;
import com.cinebuzz.service.MovieService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/movies")
public class AdminMovieController {

    @Autowired
    private MovieService movieService;

    @PostMapping
    public ResponseEntity<ApiResponse<MovieResponseDto>> createMovie(
            @Valid @RequestBody MovieRequestDto dto) {
        MovieResponseDto data = movieService.createMovie(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Movie created successfully", data));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MovieResponseDto>>> getAllMovies() {
        List<MovieResponseDto> data = movieService.getAllMovies();
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Movies fetched successfully", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MovieResponseDto>> getMovieById(@PathVariable Long id) {
        MovieResponseDto data = movieService.getMovieById(id);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Movie fetched successfully", data));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MovieResponseDto>> updateMovie(
            @PathVariable Long id,
            @Valid @RequestBody MovieRequestDto dto) {
        MovieResponseDto data = movieService.updateMovie(id, dto);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Movie updated successfully", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMovie(@PathVariable Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Movie deleted successfully", null));
    }
}