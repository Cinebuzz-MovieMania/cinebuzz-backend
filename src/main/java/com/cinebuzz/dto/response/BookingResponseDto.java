package com.cinebuzz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponseDto {

    private Long bookingId;
    private Long showtimeId;
    private BigDecimal ticketSubtotal;
    private BigDecimal totalAmount;
    private List<String> seatLabels;
    private LocalDateTime createdAt;

    private String userName;
    private String movieTitle;
    private String theatreName;
    private String screenName;
    private LocalDateTime startTime;
}
