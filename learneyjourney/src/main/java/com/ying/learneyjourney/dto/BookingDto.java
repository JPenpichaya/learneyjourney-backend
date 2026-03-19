package com.ying.learneyjourney.dto;

import com.ying.learneyjourney.constaint.EnumBookingStatus;
import com.ying.learneyjourney.entity.Booking;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class BookingDto {
    private UUID id;
    private String studentId;
    private UUID tutorId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String note;
    private BigDecimal price;
    private EnumBookingStatus status;
    private String meetingUrl;

    public static  BookingDto fromEntity(Booking entity) {
        BookingDto dto = new BookingDto();
        dto.setId(entity.getId());
        dto.setStudentId(entity.getStudent().getId());
        dto.setTutorId(entity.getTutor().getId());
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        dto.setNote(entity.getNote());
        dto.setPrice(entity.getPrice());
        dto.setStatus(entity.getStatus());
        dto.setMeetingUrl(entity.getMeetingUrl());
        return dto;
    }
}
