package com.cinebuzz.controller;

import com.cinebuzz.dto.request.MoviePersonRequestDto;
import com.cinebuzz.dto.request.PersonRequestDto;
import com.cinebuzz.dto.response.ApiResponse;
import com.cinebuzz.dto.response.MoviePersonResponseDto;
import com.cinebuzz.dto.response.PersonResponseDto;
import com.cinebuzz.service.PersonService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminPersonController {

    @Autowired
    private PersonService personService;

    @PostMapping("/persons")
    public ResponseEntity<ApiResponse<PersonResponseDto>> createPerson(
            @Valid @RequestBody PersonRequestDto dto) {
        PersonResponseDto data = personService.createPerson(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Person created successfully", data));
    }

    @GetMapping("/persons")
    public ResponseEntity<ApiResponse<List<PersonResponseDto>>> getAllPersons() {
        List<PersonResponseDto> data = personService.getAllPersons();
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Persons fetched successfully", data));
    }

    @GetMapping("/persons/{id}")
    public ResponseEntity<ApiResponse<PersonResponseDto>> getPersonById(@PathVariable Long id) {
        PersonResponseDto data = personService.getPersonById(id);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Person fetched successfully", data));
    }

    @PutMapping("/persons/{id}")
    public ResponseEntity<ApiResponse<PersonResponseDto>> updatePerson(
            @PathVariable Long id,
            @Valid @RequestBody PersonRequestDto dto) {
        PersonResponseDto data = personService.updatePerson(id, dto);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Person updated successfully", data));
    }

    @DeleteMapping("/persons/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePerson(@PathVariable Long id) {
        personService.deletePerson(id);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Person deleted successfully", null));
    }

    @PostMapping("/movies/{movieId}/cast")
    public ResponseEntity<ApiResponse<MoviePersonResponseDto>> addPersonToMovie(
            @PathVariable Long movieId,
            @Valid @RequestBody MoviePersonRequestDto dto) {
        MoviePersonResponseDto data = personService.addPersonToMovie(movieId, dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Person added to movie successfully", data));
    }

    @GetMapping("/movies/{movieId}/cast")
    public ResponseEntity<ApiResponse<List<MoviePersonResponseDto>>> getCastByMovie(
            @PathVariable Long movieId) {
        List<MoviePersonResponseDto> data = personService.getCastByMovie(movieId);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Cast fetched successfully", data));
    }

    @DeleteMapping("/movies/{movieId}/cast/{personId}")
    public ResponseEntity<ApiResponse<Void>> removePersonFromMovie(
            @PathVariable Long movieId,
            @PathVariable Long personId) {
        personService.removePersonFromMovie(movieId, personId);
        return ResponseEntity
                .ok(new ApiResponse<>(true, "Person removed from movie successfully", null));
    }
}