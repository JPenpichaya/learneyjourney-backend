package com.ying.learneyjourney.repository;

import com.ying.learneyjourney.constaint.EnumBookingStatus;
import com.ying.learneyjourney.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID>, JpaSpecificationExecutor<Booking> {
    long countByTutorId(UUID tutorId);
    
    long countByTutorIdAndStatus(UUID tutorId, EnumBookingStatus status);
}
