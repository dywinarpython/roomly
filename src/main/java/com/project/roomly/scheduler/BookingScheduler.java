package com.project.roomly.scheduler;

import com.project.roomly.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class BookingScheduler {

    private final BookingRepository bookingRepository;


    @Transactional
    @Scheduled(fixedRate = 10 * 60 * 1000)
    public void updateStatusForBooking(){
        int countUpdate = bookingRepository.cancelExpiredBookings(LocalDateTime.now().minusMinutes(15));
        log.info("CANCELLED booking -> {}", countUpdate);

    }

    @Transactional
    @Scheduled(fixedRate = 30 * 60 * 1000)
    public void deleteStatusCancelled(){
        int countDelete = bookingRepository.deleteCancelledBooking();
        log.info("DELETE  CANCELLED booking -> {}", countDelete);
    }
}
