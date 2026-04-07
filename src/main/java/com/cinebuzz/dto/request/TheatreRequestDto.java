package com.cinebuzz.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TheatreRequestDto {

    @NotBlank(message = "Theatre name is required")
    @Size(min = 1, max = 200, message = "Theatre name must be between 1 and 200 characters")
    private String name;

    @NotBlank(message = "Address is required")
    @Size(min = 1, max = 500, message = "Address must be between 1 and 500 characters")
    private String address;

    @NotNull(message = "City ID is required")
    @Min(value = 1, message = "City ID must be a positive number")
    private Long cityId;
}
