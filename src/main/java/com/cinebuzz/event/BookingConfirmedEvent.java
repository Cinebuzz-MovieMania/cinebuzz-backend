package com.cinebuzz.event;

import com.cinebuzz.dto.response.BookingResponseDto;

/** Published after a booking is committed; used to send confirmation email without blocking the HTTP transaction. */
public record BookingConfirmedEvent(String recipientEmail, BookingResponseDto booking) {}
