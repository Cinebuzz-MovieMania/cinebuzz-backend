package com.cinebuzz.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CityRequestDto {

    @NotBlank(message = "City name is required")
    private String name;

    @NotBlank(message = "State is required")
    private String state;
}