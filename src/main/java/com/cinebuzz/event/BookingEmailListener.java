package com.cinebuzz.event;

import com.cinebuzz.service.BookingMailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class BookingEmailListener {

    @Autowired
    private BookingMailService bookingMailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBookingConfirmed(BookingConfirmedEvent event) {
        try {
            bookingMailService.sendBookingConfirmation(event.recipientEmail(), event.booking());
        } catch (Exception e) {
            log.error("[mail] Listener failed for bookingId={}", event.booking().getBookingId(), e);
        }
    }
}
