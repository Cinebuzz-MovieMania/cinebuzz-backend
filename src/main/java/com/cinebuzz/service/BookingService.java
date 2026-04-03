package com.cinebuzz.service;

import com.cinebuzz.dto.request.BookingRequestDto;
import com.cinebuzz.dto.response.BookingResponseDto;
import com.cinebuzz.dto.response.SeatMapItemDto;
import com.cinebuzz.dto.response.SeatMapResponseDto;
import com.cinebuzz.entity.Booking;
import com.cinebuzz.entity.Showtime;
import com.cinebuzz.entity.ShowtimeSeat;
import com.cinebuzz.entity.User;
import com.cinebuzz.enums.SeatStatus;
import com.cinebuzz.exception.ResourceNotFoundException;
import com.cinebuzz.exception.ValidationException;
import com.cinebuzz.repository.BookingRepository;
import com.cinebuzz.repository.ShowtimeRepository;
import com.cinebuzz.repository.ShowtimeSeatRepository;
import com.cinebuzz.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookingService {

    public static final int MIN_SEATS = 1;
    public static final int MAX_SEATS = 10;

    public static final String MSG_BOOKING_FAILED =
            "Sorry! These seats are no more available.";

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private ShowtimeSeatRepository showtimeSeatRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public SeatMapResponseDto getSeatMap(Long showtimeId) {
        log.debug("[seat-map] Loading seat map for showtimeId={}", showtimeId);
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found with id: " + showtimeId));

        List<ShowtimeSeat> rows = showtimeSeatRepository.findByShowtimeIdWithSeatOrdered(showtimeId);
        log.debug("[seat-map] showtimeId={} seatRows={}", showtimeId, rows.size());
        List<SeatMapItemDto> items = rows.stream()
                .map(st -> new SeatMapItemDto(
                        st.getId(),
                        st.getSeat().getId(),
                        st.getSeat().getSeatLabel(),
                        st.getSeat().getRowLabel(),
                        st.getSeat().getSeatNumber(),
                        st.getStatus()
                ))
                .collect(Collectors.toList());

        var screen = showtime.getScreen();
        return new SeatMapResponseDto(
                showtime.getId(),
                showtime.getPrice(),
                showtime.getMovie().getTitle(),
                screen.getName(),
                screen.getTheatre().getName(),
                screen.getTotalRows(),
                screen.getSeatsPerRow(),
                items
        );
    }

    @Transactional
    public BookingResponseDto createBooking(Long userId, BookingRequestDto dto) {
        List<Long> ids = dto.getShowtimeSeatIds();
        log.info("[booking-create] Request userId={} showtimeSeatIds={}", userId, ids);
        validateSeatIdList(ids);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[booking-create] User not found id={}", userId);
                    return new ResourceNotFoundException("User not found.");
                });

        List<ShowtimeSeat> seats = showtimeSeatRepository.findByIdInForUpdate(ids);
        if (seats.size() != ids.size()) {
            rejectBooking("Fewer seats than requested requestedIds=%s found=%s".formatted(ids, seats.size()));
        }

        Long showtimeId = seats.get(0).getShowtime().getId();
        for (ShowtimeSeat st : seats) {
            Long stId = st.getId();
            if (!Objects.equals(st.getShowtime().getId(), showtimeId)) {
                rejectBooking("showtimeSeatId=%s wrong showtime".formatted(stId));
            }
            if (st.getStatus() != SeatStatus.AVAILABLE) {
                rejectBooking("showtimeSeatId=%s not available status=%s".formatted(stId, st.getStatus()));
            }
        }

        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found."));

        BigDecimal unit = showtime.getPrice();
        BigDecimal ticketSubtotal = unit.multiply(BigDecimal.valueOf(seats.size())).setScale(2, RoundingMode.HALF_UP);

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setShowtime(showtime);
        booking.setTicketSubtotal(ticketSubtotal);
        booking.setTotalAmount(ticketSubtotal);
        booking.setCreatedAt(LocalDateTime.now());
        booking = bookingRepository.save(booking);

        for (ShowtimeSeat st : seats) {
            st.setStatus(SeatStatus.BOOKED);
            st.setBooking(booking);
        }

        log.info("[booking-create] Success bookingId={} userId={} showtimeId={} seatCount={} total={}",
                booking.getId(), userId, showtimeId, seats.size(), booking.getTotalAmount());
        return toDto(booking, user);
    }

    private void rejectBooking(String internalReason) {
        log.warn("[booking-create] Rejected: {}", internalReason);
        throw new ValidationException(MSG_BOOKING_FAILED);
    }

    private void validateSeatIdList(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            log.warn("[booking] Validation failed: empty seat id list");
            throw new ValidationException("Select at least one seat.");
        }
        if (ids.size() < MIN_SEATS || ids.size() > MAX_SEATS) {
            log.warn("[booking] Validation failed: seat count {} not in [{}, {}]", ids.size(), MIN_SEATS, MAX_SEATS);
            throw new ValidationException("You can book between " + MIN_SEATS + " and " + MAX_SEATS + " seats.");
        }
        long distinct = ids.stream().distinct().count();
        if (distinct != ids.size()) {
            log.warn("[booking] Validation failed: duplicate seat ids in {}", ids);
            throw new ValidationException("Duplicate seats are not allowed.");
        }
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDto> listBookingsForUser(Long userId) {
        log.debug("[bookings-list] userId={}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[bookings-list] User not found id={}", userId);
                    return new ResourceNotFoundException("User not found.");
                });
        List<Booking> bookings = bookingRepository.findAllByUserIdWithDetailsOrderByCreatedAtDesc(user.getId());
        log.info("[bookings-list] userId={} bookingCount={}", userId, bookings.size());
        return bookings.stream().map(b -> toDto(b, user)).collect(Collectors.toList());
    }

    private BookingResponseDto toDto(Booking booking, User user) {
        Showtime showtime = booking.getShowtime();
        List<ShowtimeSeat> seatRows = showtimeSeatRepository.findByBookingIdWithSeat(booking.getId());
        List<String> labels = seatRows.stream()
                .map(st -> st.getSeat().getSeatLabel())
                .sorted()
                .collect(Collectors.toList());
        BigDecimal subtotal = booking.getTicketSubtotal();
        if (subtotal == null) {
            subtotal = booking.getTotalAmount();
        }

        return new BookingResponseDto(
                booking.getId(),
                showtime.getId(),
                subtotal,
                booking.getTotalAmount(),
                labels,
                booking.getCreatedAt(),
                user.getName(),
                showtime.getMovie().getTitle(),
                showtime.getScreen().getTheatre().getName(),
                showtime.getScreen().getName(),
                showtime.getStartTime()
        );
    }
}
