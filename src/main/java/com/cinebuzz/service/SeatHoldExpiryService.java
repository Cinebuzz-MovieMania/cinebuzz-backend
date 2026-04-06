package com.cinebuzz.service;

import com.cinebuzz.enums.SeatStatus;
import com.cinebuzz.repository.ShowtimeSeatRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Runs stale-lock cleanup in its own committed transaction so a later failure (e.g. validation in
 * {@code createBooking}) does not roll back the expiry update.
 */
@Slf4j
@Service
public class SeatHoldExpiryService {

    @Autowired
    private ShowtimeSeatRepository showtimeSeatRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int expireStaleLocks() {
        int n = showtimeSeatRepository.expireStaleLocks(
                LocalDateTime.now(), SeatStatus.AVAILABLE, SeatStatus.LOCKED);
        if (n > 0) {
            log.debug("[seat-hold] Expired stale locks count={}", n);
        }
        return n;
    }
}
