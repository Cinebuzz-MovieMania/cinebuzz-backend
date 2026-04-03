package com.cinebuzz.repository;

import com.cinebuzz.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT DISTINCT b FROM Booking b JOIN FETCH b.showtime sh JOIN FETCH sh.movie JOIN FETCH sh.screen sc JOIN FETCH sc.theatre WHERE b.user.id = :userId ORDER BY b.createdAt DESC")
    List<Booking> findAllByUserIdWithDetailsOrderByCreatedAtDesc(@Param("userId") Long userId);
}
