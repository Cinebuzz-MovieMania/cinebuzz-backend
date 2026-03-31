package com.cinebuzz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ScreenResponseDto {

    private Long id;
    private String name;
    private Integer totalRows;
    private Integer seatsPerRow;
    private Integer totalSeats;
    private Long theatreId;
    private String theatreName;
}