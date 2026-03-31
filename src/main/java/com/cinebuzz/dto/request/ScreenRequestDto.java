package com.cinebuzz.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScreenRequestDto {

    @NotBlank(message = "Screen name is required")
    private String name;

    @NotNull(message = "Total rows is required")
    @Min(value = 1, message = "Total rows must be at least 1")
    private Integer totalRows;

    @NotNull(message = "Seats per row is required")
    @Min(value = 1, message = "Seats per row must be at least 1")
    private Integer seatsPerRow;

    @NotNull(message = "Theatre ID is required")
    private Long theatreId;
}