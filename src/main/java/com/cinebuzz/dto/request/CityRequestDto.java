package com.cinebuzz.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CityRequestDto {

    @NotBlank(message = "City name is required")
    @Size(min = 1, max = 120, message = "City name must be between 1 and 120 characters")
    private String name;

    @NotBlank(message = "State is required")
    @Size(min = 1, max = 120, message = "State must be between 1 and 120 characters")
    private String state;
}
