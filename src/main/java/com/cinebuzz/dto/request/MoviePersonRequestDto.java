package com.cinebuzz.dto.request;

import com.cinebuzz.enums.MoviePersonRole;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MoviePersonRequestDto {

    @NotNull(message = "Person ID is required")
    @Min(value = 1, message = "Person ID must be a positive number")
    private Long personId;

    @NotNull(message = "Role is required")
    private MoviePersonRole role;

    @Size(max = 200, message = "Character name must be at most 200 characters")
    private String characterName;

    @Min(value = 0, message = "Billing order must be at least 0")
    @Max(value = 999, message = "Billing order must be at most 999")
    private Integer billingOrder;
}
