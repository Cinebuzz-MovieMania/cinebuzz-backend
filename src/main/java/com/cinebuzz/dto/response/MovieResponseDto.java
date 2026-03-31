package com.cinebuzz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class MovieResponseDto {

    private Long id;
    private String title;
    private String description;
    private String genre;
    private String language;
    private Integer durationMinutes;
    private LocalDate releaseDate;
    private String posterUrl;
}