package com.cinebuzz.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "seats",
        uniqueConstraints = @UniqueConstraint(columnNames = {"screen_id", "seat_label"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String rowLabel;

    @Column(nullable = false)
    private Integer seatNumber;

    @Column(nullable = false)
    private String seatLabel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;
}