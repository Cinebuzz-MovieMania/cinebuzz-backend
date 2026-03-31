package com.cinebuzz.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class PersonRequestDto {

    @NotBlank(message = "Name is required")
    private String name;

    private String bio;
    private LocalDate dateOfBirth;
    private String nationality;
    private String profilePictureUrl;
    private String profilePictureKey;
}