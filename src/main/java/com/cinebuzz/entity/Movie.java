package com.cinebuzz.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String genre;

    @Column(nullable = false)
    private String language;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false)
    private LocalDate releaseDate;

    private String posterUrl;

    private String posterKey;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    private List<MoviePerson> cast;
}