package com.cinebuzz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeatHoldResponseDto {

    private LocalDateTime holdExpiresAt;
    private int seatCount;
}
