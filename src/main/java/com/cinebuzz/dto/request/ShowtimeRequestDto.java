package com.cinebuzz.dto.request;

import jakarta.validation.constraints.DecimalMin;
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
    @Min(value = 1, message = "Movie ID must be positive")
    private Long movieId;

    @NotNull(message = "Screen ID is required")
    @Min(value = 1, message = "Screen ID must be positive")
    private Long screenId;

    /** Show start; end time is computed as start + movie duration (minutes). */
    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
    private BigDecimal price;
}
