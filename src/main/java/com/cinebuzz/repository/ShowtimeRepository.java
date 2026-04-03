package com.cinebuzz.repository;

import com.cinebuzz.entity.Movie;
import com.cinebuzz.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    /**
     * Counts conflicting shows on the same screen. A 5-minute gap is required after each show ends
     * before the next can start. Conflict when: existing.start &lt; newEnd+gap AND newStart &lt; existing.end+gap.
     */
    @Query(value = """
            SELECT COUNT(*) FROM showtimes s
            WHERE s.screen_id = :screenId
            AND s.start_time < :newEndPlusGap
            AND :newStart < DATE_ADD(s.end_time, INTERVAL :gapMinutes MINUTE)
            """, nativeQuery = true)
    long countConflictingOnScreenWithGap(
            @Param("screenId") Long screenId,
            @Param("newStart") LocalDateTime newStart,
            @Param("newEndPlusGap") LocalDateTime newEndPlusGap,
            @Param("gapMinutes") int gapMinutes);

    @Query("SELECT DISTINCT m FROM Showtime st JOIN st.movie m JOIN st.screen sc JOIN sc.theatre t WHERE t.city.id = :cityId ORDER BY m.title")
    List<Movie> findDistinctMoviesPlayingInCity(@Param("cityId") Long cityId);

    @Query("select st.movie.id, count(st) from Showtime st join st.screen sc join sc.theatre t where t.city.id = :cityId group by st.movie.id")
    List<Object[]> countShowtimesPerMovieInCity(@Param("cityId") Long cityId);

    @Query("SELECT s FROM Showtime s JOIN FETCH s.movie JOIN FETCH s.screen sc JOIN FETCH sc.theatre th JOIN FETCH th.city c WHERE c.id = :cityId AND s.movie.id = :movieId AND s.startTime >= :dayStart AND s.startTime < :dayEnd ORDER BY s.startTime")
    List<Showtime> findByCityMovieAndDateRange(
            @Param("cityId") Long cityId,
            @Param("movieId") Long movieId,
            @Param("dayStart") LocalDateTime dayStart,
            @Param("dayEnd") LocalDateTime dayEnd);

    @Query("SELECT s FROM Showtime s JOIN FETCH s.movie JOIN FETCH s.screen sc JOIN FETCH sc.theatre th JOIN FETCH th.city WHERE s.id = :id")
    Optional<Showtime> findByIdWithDetails(@Param("id") Long id);
}