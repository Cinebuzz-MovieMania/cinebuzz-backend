package com.cinebuzz.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScreenRequestDto {

    @NotBlank(message = "Screen name is required")
    @Size(min = 1, max = 120, message = "Screen name must be between 1 and 120 characters")
    private String name;

    @NotNull(message = "Total rows is required")
    @Min(value = 1, message = "Total rows must be at least 1")
    @Max(value = 100, message = "Total rows must be at most 100")
    private Integer totalRows;

    @NotNull(message = "Seats per row is required")
    @Min(value = 1, message = "Seats per row must be at least 1")
    @Max(value = 100, message = "Seats per row must be at most 100")
    private Integer seatsPerRow;

    @NotNull(message = "Theatre ID is required")
    @Min(value = 1, message = "Theatre ID must be a positive number")
    private Long theatreId;
}
