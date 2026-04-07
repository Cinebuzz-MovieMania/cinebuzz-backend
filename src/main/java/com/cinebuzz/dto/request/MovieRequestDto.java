package com.cinebuzz.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class MovieRequestDto {

    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 300, message = "Title must be between 1 and 300 characters")
    private String title;

    @Size(max = 10000, message = "Description must be at most 10000 characters")
    private String description;

    @NotBlank(message = "Genre is required")
    @Size(min = 1, max = 100, message = "Genre must be between 1 and 100 characters")
    private String genre;

    @NotBlank(message = "Language is required")
    @Size(min = 1, max = 80, message = "Language must be between 1 and 80 characters")
    private String language;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 600, message = "Duration must be at most 600 minutes")
    private Integer durationMinutes;

    @NotNull(message = "Release date is required")
    private LocalDate releaseDate;

    @Size(max = 2048, message = "Poster URL must be at most 2048 characters")
    private String posterUrl;

    @Size(max = 512, message = "Poster key must be at most 512 characters")
    private String posterKey;
}
