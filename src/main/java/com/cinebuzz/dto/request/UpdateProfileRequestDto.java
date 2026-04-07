package com.cinebuzz.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequestDto {

    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 120, message = "Name must be between 1 and 120 characters")
    private String name;
}
