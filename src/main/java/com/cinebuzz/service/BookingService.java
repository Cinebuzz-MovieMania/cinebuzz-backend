package com.cinebuzz.service;

import com.cinebuzz.config.BookingProperties;
import com.cinebuzz.dto.request.BookingRequestDto;
import com.cinebuzz.dto.response.BookingPricingResponseDto;
import com.cinebuzz.dto.response.BookingResponseDto;
import com.cinebuzz.dto.response.SeatHoldResponseDto;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class BookingService {

    public static final int MIN_SEATS = 1;
    public static final int MAX_SEATS = 10;

    @Autowired
    private ShowtimeRepository showtimeRepository;

    @Autowired
    private ShowtimeSeatRepository showtimeSeatRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingProperties bookingProperties;

    @Transactional(readOnly = true)
    public BookingPricingResponseDto getBookingPricingConfig() {
        return new BookingPricingResponseDto(bookingProperties.getConvenienceFeeRate());
    }

    @Transactional
    public SeatMapResponseDto getSeatMap(Long showtimeId) {
        releaseExpiredHolds();
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found with id: " + showtimeId));

        List<ShowtimeSeat> rows = showtimeSeatRepository.findByShowtimeIdWithSeatOrdered(showtimeId);
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
    public SeatHoldResponseDto holdSeats(String userEmail, BookingRequestDto dto) {
        List<Long> ids = dto.getShowtimeSeatIds();
        validateSeatIdList(ids);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        releaseExpiredHolds();

        List<ShowtimeSeat> rows = showtimeSeatRepository.findByIdInForUpdate(ids);
        if (rows.size() != ids.size()) {
            throw new ValidationException("One or more seats are invalid.");
        }

        Long showtimeId = rows.get(0).getShowtime().getId();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime until = now.plusMinutes(bookingProperties.getSeatHoldMinutes());

        for (ShowtimeSeat st : rows) {
            if (!Objects.equals(st.getShowtime().getId(), showtimeId)) {
                throw new ValidationException("All seats must belong to the same show.");
            }
            if (st.getStatus() == SeatStatus.BOOKED) {
                throw new ValidationException("Seat " + st.getSeat().getSeatLabel() + " is already booked.");
            }
            if (st.getStatus() == SeatStatus.LOCKED) {
                boolean expired = st.getLockedUntil() == null || st.getLockedUntil().isBefore(now);
                boolean mine = st.getHeldBy() != null && st.getHeldBy().getId().equals(user.getId());
                if (!expired && !mine) {
                    throw new ValidationException("Seat " + st.getSeat().getSeatLabel() + " is no longer available.");
                }
            } else if (st.getStatus() != SeatStatus.AVAILABLE) {
                throw new ValidationException("Seat " + st.getSeat().getSeatLabel() + " is no longer available.");
            }
        }

        for (ShowtimeSeat st : rows) {
            st.setStatus(SeatStatus.LOCKED);
            st.setHeldBy(user);
            st.setLockedUntil(until);
        }

        return new SeatHoldResponseDto(until, rows.size());
    }

    /**
     * Clears LOCKED seats held by this user (e.g. user left checkout without paying).
     */
    @Transactional
    public void releaseSeatHolds(String userEmail, BookingRequestDto dto) {
        if (dto == null || dto.getShowtimeSeatIds() == null || dto.getShowtimeSeatIds().isEmpty()) {
            return;
        }
        List<Long> ids = dto.getShowtimeSeatIds();
        validateSeatIdList(ids);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        releaseExpiredHolds();

        List<ShowtimeSeat> rows = showtimeSeatRepository.findByIdInForUpdate(ids);
        if (rows.size() != ids.size()) {
            return;
        }

        for (ShowtimeSeat st : rows) {
            if (st.getStatus() != SeatStatus.LOCKED) {
                continue;
            }
            if (st.getHeldBy() == null || !st.getHeldBy().getId().equals(user.getId())) {
                continue;
            }
            st.setStatus(SeatStatus.AVAILABLE);
            st.setHeldBy(null);
            st.setLockedUntil(null);
        }
    }

    @Transactional
    public BookingResponseDto createBooking(String userEmail, BookingRequestDto dto) {
        List<Long> ids = dto.getShowtimeSeatIds();
        validateSeatIdList(ids);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        releaseExpiredHolds();

        List<ShowtimeSeat> locked = showtimeSeatRepository.findByIdInForUpdate(ids);
        if (locked.size() != ids.size()) {
            throw new ValidationException("One or more seats are invalid.");
        }

        Long showtimeId = locked.get(0).getShowtime().getId();
        LocalDateTime now = LocalDateTime.now();
        for (ShowtimeSeat st : locked) {
            if (!Objects.equals(st.getShowtime().getId(), showtimeId)) {
                throw new ValidationException("All seats must belong to the same show.");
            }
            if (st.getStatus() == SeatStatus.BOOKED) {
                throw new ValidationException("Seat " + st.getSeat().getSeatLabel() + " is no longer available.");
            }
            if (st.getStatus() != SeatStatus.LOCKED || st.getHeldBy() == null
                    || !st.getHeldBy().getId().equals(user.getId())) {
                throw new ValidationException(
                        "Seats must be reserved from the seat map first. Go back and complete Pay again.");
            }
            if (st.getLockedUntil() == null || st.getLockedUntil().isBefore(now)) {
                throw new ValidationException("Your seat hold expired. Go back and select seats again.");
            }
        }

        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found."));

        BigDecimal unit = showtime.getPrice();
        BigDecimal ticketSubtotal = unit.multiply(BigDecimal.valueOf(locked.size())).setScale(2, RoundingMode.HALF_UP);
        BigDecimal fee = ticketSubtotal
                .multiply(bookingProperties.getConvenienceFeeRate())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = ticketSubtotal.add(fee);

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setShowtime(showtime);
        booking.setTicketSubtotal(ticketSubtotal);
        booking.setConvenienceFee(fee);
        booking.setTotalAmount(total);
        booking.setCreatedAt(LocalDateTime.now());
        booking = bookingRepository.save(booking);

        for (ShowtimeSeat st : locked) {
            st.setStatus(SeatStatus.BOOKED);
            st.setBooking(booking);
            st.setHeldBy(null);
            st.setLockedUntil(null);
        }

        return toDto(booking, user);
    }

    private void validateSeatIdList(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new ValidationException("Select at least one seat.");
        }
        if (ids.size() < MIN_SEATS || ids.size() > MAX_SEATS) {
            throw new ValidationException("You can book between " + MIN_SEATS + " and " + MAX_SEATS + " seats.");
        }
        long distinct = ids.stream().distinct().count();
        if (distinct != ids.size()) {
            throw new ValidationException("Duplicate seats are not allowed.");
        }
    }

    private void releaseExpiredHolds() {
        showtimeSeatRepository.releaseExpiredLocksBefore(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDto> listBookingsForUser(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));
        List<Booking> bookings = bookingRepository.findAllByUserIdWithDetailsOrderByCreatedAtDesc(user.getId());
        return bookings.stream().map(b -> toDto(b, user)).collect(Collectors.toList());
    }

    private BookingResponseDto toDto(Booking booking, User user) {
        Showtime showtime = booking.getShowtime();
        List<ShowtimeSeat> seats = showtimeSeatRepository.findByBookingIdWithSeat(booking.getId());
        List<String> labels = seats.stream()
                .map(st -> st.getSeat().getSeatLabel())
                .sorted()
                .collect(Collectors.toList());
        BigDecimal subtotal = booking.getTicketSubtotal();
        BigDecimal convenienceFee = booking.getConvenienceFee();
        if (subtotal == null) {
            subtotal = booking.getTotalAmount();
            convenienceFee = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return new BookingResponseDto(
                booking.getId(),
                showtime.getId(),
                subtotal,
                convenienceFee,
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
