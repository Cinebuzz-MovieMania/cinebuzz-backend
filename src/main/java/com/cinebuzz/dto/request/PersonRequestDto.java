package com.cinebuzz.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
public class PersonRequestDto {

    @NotBlank(message = "Name is required")
    @Size(min = 1, max = 200, message = "Name must be between 1 and 200 characters")
    private String name;

    @Size(max = 10000, message = "Bio must be at most 10000 characters")
    private String bio;
    private LocalDate dateOfBirth;

    @Size(max = 80, message = "Nationality must be at most 80 characters")
    private String nationality;

    @Size(max = 2048, message = "Profile picture URL must be at most 2048 characters")
    private String profilePictureUrl;

    @Size(max = 512, message = "Profile picture key must be at most 512 characters")
    private String profilePictureKey;
}
