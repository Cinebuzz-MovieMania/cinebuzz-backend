package com.cinebuzz.controller;

import com.cinebuzz.dto.request.BookingRequestDto;
import com.cinebuzz.dto.request.SeatHoldRequestDto;
import com.cinebuzz.dto.response.ApiResponse;
import com.cinebuzz.dto.response.BookingResponseDto;
import com.cinebuzz.dto.response.SeatHoldResponseDto;
import com.cinebuzz.dto.response.SeatMapResponseDto;
import com.cinebuzz.entity.User;
import com.cinebuzz.service.BookingService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    /** ShowtimeSeat rows for this show (status per seat). Prefer path {@code showtime-seats} over {@code seats}. */
    @GetMapping({"/showtimes/{showtimeId}/showtime-seats", "/showtimes/{showtimeId}/seats"})
    public ResponseEntity<ApiResponse<SeatMapResponseDto>> getShowtimeSeatMap(@PathVariable Long showtimeId) {
        log.debug("[api] GET /showtimes/{}/showtime-seats", showtimeId);
        SeatMapResponseDto data = bookingService.getSeatMap(showtimeId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Showtime seat map loaded", data));
    }

    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<List<BookingResponseDto>>> listMyBookings(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = requireCurrentUser(userDetails);
        log.info("[api] GET /bookings principalUserId={} email={}", user.getId(), user.getEmail());
        List<BookingResponseDto> data = bookingService.listBookingsForUser(user.getId());
        return ResponseEntity.ok(new ApiResponse<>(true, "Bookings loaded", data));
    }

    /** After login: user reaches payment page — lock seats for this account until TTL (see {@code booking.hold-ttl-seconds}). */
    @PostMapping("/bookings/hold")
    public ResponseEntity<ApiResponse<SeatHoldResponseDto>> holdSeats(
            @Valid @RequestBody SeatHoldRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = requireCurrentUser(userDetails);
        log.info("[api] POST /bookings/hold userId={} seats={}", user.getId(), dto.getShowtimeSeatIds());
        SeatHoldResponseDto data = bookingService.createHold(user.getId(), dto);
        return ResponseEntity.ok(new ApiResponse<>(true, "Seats held for checkout", data));
    }

    /** Cancel checkout or leave payment page — release seats held by this user. */
    @PostMapping("/bookings/release-hold")
    public ResponseEntity<ApiResponse<Void>> releaseSeats(
            @Valid @RequestBody SeatHoldRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = requireCurrentUser(userDetails);
        log.info("[api] POST /bookings/release-hold userId={} seats={}", user.getId(), dto.getShowtimeSeatIds());
        bookingService.releaseHold(user.getId(), dto);
        return ResponseEntity.ok(new ApiResponse<>(true, "Hold released", null));
    }

    @PostMapping("/bookings")
    public ResponseEntity<ApiResponse<BookingResponseDto>> createBooking(
            @Valid @RequestBody BookingRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = requireCurrentUser(userDetails);
        log.info("[api] POST /bookings principalUserId={} email={} seatIds={}",
                user.getId(), user.getEmail(), dto.getShowtimeSeatIds());
        BookingResponseDto data = bookingService.createBooking(user.getId(), dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Booking confirmed", data));
    }

    private static User requireCurrentUser(UserDetails   details) {
        if (!(details instanceof User u)) {
            log.error("[api] Wrong principal type: {}", details != null ? details.getClass().getName() : "null");
            throw new IllegalStateException("Expected domain User principal");
        }
        return u;
    }
}
