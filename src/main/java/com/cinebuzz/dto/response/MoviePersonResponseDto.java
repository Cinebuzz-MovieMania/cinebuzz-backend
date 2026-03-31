package com.cinebuzz.dto.response;

import com.cinebuzz.enums.MoviePersonRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MoviePersonResponseDto {

    private Long id;
    private Long personId;
    private String personName;
    private String profilePictureUrl;
    private MoviePersonRole role;
    private String characterName;
    private Integer billingOrder;
}