package com.cinebuzz.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingPricingResponseDto {

    /** Same as configured rate (e.g. 0.10 for 10%). */
    private BigDecimal convenienceFeeRate;
}
