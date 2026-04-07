package com.cinebuzz.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ShowtimeRequestDto {

    @NotNull(message = "Movie ID is required")
    @Min(value = 1, message = "Movie ID must be a positive number")
    private Long movieId;

    @NotNull(message = "Screen ID is required")
    @Min(value = 1, message = "Screen ID must be a positive number")
    private Long screenId;

    /** Show start; end time is computed as start + movie duration (minutes). */
    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
    @DecimalMax(value = "99999999.99", inclusive = true, message = "Price exceeds maximum allowed")
    @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 digits before decimal and 2 after")
    private BigDecimal price;
}
