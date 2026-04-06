package com.cinebuzz.service;

import com.cinebuzz.dto.request.BookingRequestDto;
import com.cinebuzz.dto.request.SeatHoldRequestDto;
import com.cinebuzz.dto.response.BookingResponseDto;
import com.cinebuzz.dto.response.SeatHoldResponseDto;
import com.cinebuzz.dto.response.SeatMapItemDto;
import com.cinebuzz.dto.response.SeatMapResponseDto;
import com.cinebuzz.entity.Booking;
import com.cinebuzz.entity.Showtime;
import com.cinebuzz.entity.ShowtimeSeat;
import com.cinebuzz.entity.User;
import com.cinebuzz.enums.SeatStatus;
import com.cinebuzz.event.BookingConfirmedEvent;
import com.cinebuzz.exception.ResourceNotFoundException;
import com.cinebuzz.exception.ValidationException;
import com.cinebuzz.repository.BookingRepository;
import com.cinebuzz.repository.ShowtimeRepository;
import com.cinebuzz.repository.ShowtimeSeatRepository;
import com.cinebuzz.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
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

    /** Shown when hold TTL passed (seats return to AVAILABLE) or lock no longer valid at confirm time. */
    public static final String MSG_HOLD_EXPIRED =
            "Transaction failed. Please select your seats again.";

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private ShowtimeSeatRepository showtimeSeatRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SeatHoldExpiryService seatHoldExpiryService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Value("${booking.hold-ttl-seconds:20}")
    private int holdTtlSeconds;

    @Transactional
    public SeatMapResponseDto getSeatMap(Long showtimeId) {
        seatHoldExpiryService.expireStaleLocks();
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
                        st.getStatus(),
                        st.getLockedBy() != null ? st.getLockedBy().getId() : null
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

    /**
     * After login, when user reaches the payment page: lock seats for this user until {@link #holdTtlSeconds} elapses.
     * Idempotent for the same user (refreshes TTL).
     */
    @Transactional
    public SeatHoldResponseDto createHold(Long userId, SeatHoldRequestDto dto) {
        List<Long> ids = dto.getShowtimeSeatIds();
        log.info("[seat-hold] create userId={} seatIds={}", userId, ids);
        validateSeatIdList(ids);

        seatHoldExpiryService.expireStaleLocks();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        List<ShowtimeSeat> seats = showtimeSeatRepository.findByIdInForUpdate(ids);
        if (seats.size() != ids.size()) {
            throw new ValidationException(MSG_BOOKING_FAILED);
        }

        Long showtimeId = seats.get(0).getShowtime().getId();
        for (ShowtimeSeat st : seats) {
            if (!Objects.equals(st.getShowtime().getId(), showtimeId)) {
                throw new ValidationException(MSG_BOOKING_FAILED);
            }
            validateSeatForHold(st, userId);
        }

        LocalDateTime until = LocalDateTime.now().plusSeconds(holdTtlSeconds);
        for (ShowtimeSeat st : seats) {
            st.setStatus(SeatStatus.LOCKED);
            st.setLockedBy(user);
            st.setLockedUntil(until);
        }

        log.info("[seat-hold] success userId={} until={} count={}", userId, until, seats.size());
        return new SeatHoldResponseDto(ids, until);
    }

    private void validateSeatForHold(ShowtimeSeat st, Long userId) {
        switch (st.getStatus()) {
            case AVAILABLE:
                return;
            case LOCKED:
                if (st.getLockedBy() == null || !st.getLockedBy().getId().equals(userId)) {
                    throw new ValidationException("These seats are being held by another user.");
                }
                return;
            case BOOKED:
            default:
                throw new ValidationException(MSG_BOOKING_FAILED);
        }
    }

    /** Release holds owned by this user (cancel checkout or leave payment page). */
    @Transactional
    public void releaseHold(Long userId, SeatHoldRequestDto dto) {
        List<Long> ids = dto.getShowtimeSeatIds();
        if (ids == null || ids.isEmpty()) {
            return;
        }
        log.debug("[seat-hold] release userId={} seatIds={}", userId, ids);

        seatHoldExpiryService.expireStaleLocks();

        List<ShowtimeSeat> seats = showtimeSeatRepository.findByIdInForUpdate(ids);
        for (ShowtimeSeat st : seats) {
            if (st.getStatus() != SeatStatus.LOCKED) {
                continue;
            }
            if (st.getLockedBy() != null && st.getLockedBy().getId().equals(userId)) {
                st.setStatus(SeatStatus.AVAILABLE);
                st.setLockedUntil(null);
                st.setLockedBy(null);
            }
        }
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

        seatHoldExpiryService.expireStaleLocks();

        List<ShowtimeSeat> seats = showtimeSeatRepository.findByIdInForUpdate(ids);
        if (seats.size() != ids.size()) {
            rejectBooking("Fewer seats than requested requestedIds=%s found=%s".formatted(ids, seats.size()));
        }

        Long showtimeId = seats.get(0).getShowtime().getId();
        LocalDateTime now = LocalDateTime.now();
        for (ShowtimeSeat st : seats) {
            Long stId = st.getId();
            if (!Objects.equals(st.getShowtime().getId(), showtimeId)) {
                rejectBooking("showtimeSeatId=%s wrong showtime".formatted(stId));
            }
            // After expiry (committed in its own transaction), lapsed holds are AVAILABLE — long wait on payment page.
            if (st.getStatus() == SeatStatus.AVAILABLE) {
                throw new ValidationException(MSG_HOLD_EXPIRED);
            }
            if (st.getStatus() == SeatStatus.BOOKED) {
                rejectBooking("showtimeSeatId=%s already booked".formatted(stId));
            }
            if (st.getStatus() != SeatStatus.LOCKED) {
                rejectBooking("showtimeSeatId=%s expected LOCKED got %s".formatted(stId, st.getStatus()));
            }
            if (st.getLockedBy() == null || !st.getLockedBy().getId().equals(userId)) {
                rejectBooking("showtimeSeatId=%s not locked by this user".formatted(stId));
            }
            if (st.getLockedUntil() == null || !st.getLockedUntil().isAfter(now)) {
                throw new ValidationException(MSG_HOLD_EXPIRED);
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
            st.setLockedUntil(null);
            st.setLockedBy(null);
        }

        BookingResponseDto response = toDto(booking, user);
        eventPublisher.publishEvent(new BookingConfirmedEvent(user.getEmail(), response));
        log.info("[booking-create] Success bookingId={} userId={} showtimeId={} seatCount={} total={}",
                booking.getId(), userId, showtimeId, seats.size(), booking.getTotalAmount());
        return response;
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
        User u = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("[bookings-list] User not found id={}", userId);
                    return new ResourceNotFoundException("User not found.");
                });
        List<Booking> bookings = bookingRepository.findAllByUserIdWithDetailsOrderByCreatedAtDesc(u.getId());
        log.info("[bookings-list] userId={} bookingCount={}", userId, bookings.size());
        return bookings.stream().map(b -> toDto(b, u)).collect(Collectors.toList());
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
