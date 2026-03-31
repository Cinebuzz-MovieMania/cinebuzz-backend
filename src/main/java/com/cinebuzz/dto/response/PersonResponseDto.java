package com.cinebuzz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class PersonResponseDto {

    private Long id;
    private String name;
    private String bio;
    private LocalDate dateOfBirth;
    private String nationality;
    private String profilePictureUrl;
}