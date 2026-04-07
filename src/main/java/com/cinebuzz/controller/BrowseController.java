package com.cinebuzz.controller;

import com.cinebuzz.dto.response.ApiResponse;
import com.cinebuzz.dto.response.BrowseMovieRowDto;
import com.cinebuzz.dto.response.PersonResponseDto;
import com.cinebuzz.dto.response.ShowtimeResponseDto;
import com.cinebuzz.service.BrowseService;
import com.cinebuzz.service.PersonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/browse")
public class BrowseController {

    @Autowired
    private BrowseService browseService;

    @Autowired
    private PersonService personService;

    /** Public person profile (cast/crew detail on the home movie panel). */
    @GetMapping("/persons/{id}")
    public ResponseEntity<ApiResponse<PersonResponseDto>> personById(@PathVariable Long id) {
        PersonResponseDto data = personService.getPersonById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Person", data));
    }

    @GetMapping("/movies")
    public ResponseEntity<ApiResponse<List<BrowseMovieRowDto>>> moviesForCity(@RequestParam Long cityId) {
        List<BrowseMovieRowDto> data = browseService.listMoviesForCity(cityId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Movies for city", data));
    }

    @GetMapping("/showtimes")
    public ResponseEntity<ApiResponse<List<ShowtimeResponseDto>>> showtimesForCityMovieDate(
            @RequestParam Long cityId,
            @RequestParam Long movieId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ShowtimeResponseDto> data = browseService.listShowtimesForCityMovieAndDate(cityId, movieId, date);
        return ResponseEntity.ok(new ApiResponse<>(true, "Showtimes", data));
    }

    @GetMapping("/showtimes/{id}")
    public ResponseEntity<ApiResponse<ShowtimeResponseDto>> showtimeById(@PathVariable Long id) {
        ShowtimeResponseDto data = browseService.getShowtimeById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Showtime", data));
    }
}
