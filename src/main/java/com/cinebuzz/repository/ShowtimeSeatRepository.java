package com.cinebuzz.repository;

import com.cinebuzz.entity.ShowtimeSeat;
import com.cinebuzz.enums.SeatStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowtimeSeatRepository extends JpaRepository<ShowtimeSeat, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE ShowtimeSeat s SET s.status = 'AVAILABLE', s.lockedUntil = null, s.heldBy = null "
            + "WHERE s.status = 'LOCKED' AND s.lockedUntil IS NOT NULL AND s.lockedUntil < :now")
    int releaseExpiredLocksBefore(@Param("now") LocalDateTime now);

    List<ShowtimeSeat> findByShowtimeId(Long showtimeId);

    long countByShowtimeIdAndStatus(Long showtimeId, SeatStatus status);

    @Query("SELECT s FROM ShowtimeSeat s JOIN FETCH s.seat seat WHERE s.showtime.id = :showtimeId ORDER BY seat.rowLabel, seat.seatNumber")
    List<ShowtimeSeat> findByShowtimeIdWithSeatOrdered(@Param("showtimeId") Long showtimeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ShowtimeSeat s JOIN FETCH s.seat seat LEFT JOIN FETCH s.heldBy WHERE s.id IN :ids")
    List<ShowtimeSeat> findByIdInForUpdate(@Param("ids") List<Long> ids);

    @Query("SELECT s FROM ShowtimeSeat s JOIN FETCH s.seat WHERE s.booking.id = :bookingId ORDER BY s.seat.seatLabel")
    List<ShowtimeSeat> findByBookingIdWithSeat(@Param("bookingId") Long bookingId);
}