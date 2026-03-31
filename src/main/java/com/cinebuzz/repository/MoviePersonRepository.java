package com.cinebuzz.repository;

import com.cinebuzz.entity.MoviePerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MoviePersonRepository extends JpaRepository<MoviePerson, Long> {
    List<MoviePerson> findByMovieIdOrderByBillingOrder(Long movieId);
    boolean existsByMovieIdAndPersonId(Long movieId, Long personId);
    Optional<MoviePerson> findByMovieIdAndPersonId(Long movieId, Long personId);

}