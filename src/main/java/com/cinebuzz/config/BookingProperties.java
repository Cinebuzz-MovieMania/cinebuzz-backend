package com.cinebuzz.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@Getter
@Setter
@ConfigurationProperties(prefix = "cinebuzz.booking")
public class BookingProperties {

    /**
     * Fraction of ticket subtotal charged as convenience fee (e.g. 0.10 for 10%).
     */
    private BigDecimal convenienceFeeRate = new BigDecimal("0.10");

    /**
     * How long seats stay reserved after Pay (review page) before the hold expires.
     */
    private int seatHoldMinutes = 10;
}
