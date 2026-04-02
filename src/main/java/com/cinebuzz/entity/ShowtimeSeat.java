package com.cinebuzz.entity;

import com.cinebuzz.enums.SeatStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "showtime_seats",
        uniqueConstraints = @UniqueConstraint(columnNames = {"showtime_id", "seat_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShowtimeSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus status;

    @Column
    private LocalDateTime lockedUntil;

    /** When status is LOCKED, which user holds this seat until {@link #lockedUntil}. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "held_by_user_id")
    private User heldBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;
}
