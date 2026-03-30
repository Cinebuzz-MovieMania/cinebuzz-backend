package com.cinebuzz.entity;

import com.cinebuzz.enums.MoviePersonRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "movie_persons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MoviePerson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MoviePersonRole role;

    private String characterName;

    private Integer billingOrder;
}