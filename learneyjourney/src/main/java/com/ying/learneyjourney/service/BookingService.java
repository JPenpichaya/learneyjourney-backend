package com.ying.learneyjourney.service;

import com.ying.learneyjourney.constaint.EnumBookingStatus;
import com.ying.learneyjourney.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;

    public Long bookingComingSoonByTutorId(UUID tutorId){
        return bookingRepository.countByTutorIdAndStatus(tutorId, EnumBookingStatus.CONFIRMED);
    }
}
