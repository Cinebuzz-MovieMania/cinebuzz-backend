package com.cinebuzz.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "persons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Person {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String bio;

    private LocalDate dateOfBirth;

    private String nationality;

    private String profilePictureUrl;

    private String profilePictureKey;

    @OneToMany(mappedBy = "person", cascade = CascadeType.ALL)
    private List<MoviePerson> movies;
}