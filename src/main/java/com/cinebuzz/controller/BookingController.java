package com.cinebuzz.controller;

import com.cinebuzz.dto.request.BookingRequestDto;
import com.cinebuzz.dto.response.ApiResponse;
import com.cinebuzz.dto.response.BookingPricingResponseDto;
import com.cinebuzz.dto.response.BookingResponseDto;
import com.cinebuzz.dto.response.SeatHoldResponseDto;
import com.cinebuzz.dto.response.SeatMapResponseDto;
import com.cinebuzz.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @GetMapping("/showtimes/{showtimeId}/seats")
    public ResponseEntity<ApiResponse<SeatMapResponseDto>> getSeatMap(@PathVariable Long showtimeId) {
        SeatMapResponseDto data = bookingService.getSeatMap(showtimeId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Seat map loaded", data));
    }

    @GetMapping("/booking-pricing")
    public ResponseEntity<ApiResponse<BookingPricingResponseDto>> getBookingPricing() {
        BookingPricingResponseDto data = bookingService.getBookingPricingConfig();
        return ResponseEntity.ok(new ApiResponse<>(true, "Booking pricing", data));
    }

    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<List<BookingResponseDto>>> listMyBookings(
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        List<BookingResponseDto> data = bookingService.listBookingsForUser(email);
        return ResponseEntity.ok(new ApiResponse<>(true, "Bookings loaded", data));
    }

    @PostMapping("/bookings/seat-hold")
    public ResponseEntity<ApiResponse<SeatHoldResponseDto>> holdSeats(
            @Valid @RequestBody BookingRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        SeatHoldResponseDto data = bookingService.holdSeats(email, dto);
        return ResponseEntity.ok(new ApiResponse<>(true, "Seats reserved", data));
    }

    @PostMapping("/bookings/seat-hold/release")
    public ResponseEntity<ApiResponse<Void>> releaseSeatHolds(
            @RequestBody(required = false) BookingRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        bookingService.releaseSeatHolds(email, dto);
        return ResponseEntity.ok(new ApiResponse<>(true, "Seats released", null));
    }

    @PostMapping("/bookings")
    public ResponseEntity<ApiResponse<BookingResponseDto>> createBooking(
            @Valid @RequestBody BookingRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        BookingResponseDto data = bookingService.createBooking(email, dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Booking confirmed", data));
    }
}
