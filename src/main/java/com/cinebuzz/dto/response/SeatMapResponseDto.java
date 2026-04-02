package com.cinebuzz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeatMapResponseDto {

    private Long showtimeId;
    private BigDecimal price;
    private String movieTitle;
    private String screenName;
    private String theatreName;
    private Integer totalRows;
    private Integer seatsPerRow;
    private List<SeatMapItemDto> seats;
}
