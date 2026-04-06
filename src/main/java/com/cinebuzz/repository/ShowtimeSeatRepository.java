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

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowtimeSeatRepository extends JpaRepository<ShowtimeSeat, Long> {

    List<ShowtimeSeat> findByShowtimeId(Long showtimeId);

    long countByShowtimeIdAndStatus(Long showtimeId, SeatStatus status);

    @Query("SELECT s FROM ShowtimeSeat s JOIN FETCH s.seat seat LEFT JOIN FETCH s.lockedBy WHERE s.showtime.id = :showtimeId ORDER BY seat.rowLabel, seat.seatNumber")
    List<ShowtimeSeat> findByShowtimeIdWithSeatOrdered(@Param("showtimeId") Long showtimeId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ShowtimeSeat s JOIN FETCH s.seat seat LEFT JOIN FETCH s.lockedBy WHERE s.id IN :ids")
    List<ShowtimeSeat> findByIdInForUpdate(@Param("ids") List<Long> ids);

    /**
     * Any row still {@link SeatStatus#LOCKED} but past its hold window (or with missing {@code lockedUntil})
     * becomes {@link SeatStatus#AVAILABLE} and lock columns are cleared.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ShowtimeSeat s SET s.status = :available, s.lockedUntil = null, s.lockedBy = null "
            + "WHERE s.status = :locked AND (s.lockedUntil IS NULL OR s.lockedUntil <= :now)")
    int expireStaleLocks(@Param("now") LocalDateTime now,
                         @Param("available") SeatStatus available,
                         @Param("locked") SeatStatus locked);

    @Query("SELECT s FROM ShowtimeSeat s JOIN FETCH s.seat WHERE s.booking.id = :bookingId ORDER BY s.seat.seatLabel")
    List<ShowtimeSeat> findByBookingIdWithSeat(@Param("bookingId") Long bookingId);
}
