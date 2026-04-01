package com.cinebuzz.repository;

import com.cinebuzz.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

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
}