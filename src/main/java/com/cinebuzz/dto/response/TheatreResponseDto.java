package com.cinebuzz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class TheatreResponseDto {

    private Long id;
    private String name;
    private String address;
    private Long cityId;
    private String cityName;
}