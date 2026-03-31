package com.cinebuzz.dto.request;

import com.cinebuzz.enums.MoviePersonRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MoviePersonRequestDto {

    @NotNull(message = "Person ID is required")
    private Long personId;

    @NotNull(message = "Role is required")
    private MoviePersonRole role;

    private String characterName;
    private Integer billingOrder;
}