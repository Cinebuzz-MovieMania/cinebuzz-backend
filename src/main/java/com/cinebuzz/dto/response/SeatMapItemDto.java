package com.cinebuzz.dto.response;

import com.cinebuzz.enums.SeatStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SeatMapItemDto {

    private Long showtimeSeatId;
    private Long seatId;
    private String seatLabel;
    private String rowLabel;
    private Integer seatNumber;
    private SeatStatus status;
    /** Present when status is LOCKED; identifies who holds the seat for checkout. */
    private Long lockedByUserId;
}
