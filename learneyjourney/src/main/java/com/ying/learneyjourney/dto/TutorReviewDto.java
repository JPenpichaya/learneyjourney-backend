package com.ying.learneyjourney.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class TutorReviewDto {
    private UUID id;
    private UUID tutorProfileId;
    private String studentId;
    private UUID bookingId;
    private Integer rating;
    private String reviewText;
    private Boolean isVisible;

    public static TutorReviewDto fromEntity(com.ying.learneyjourney.entity.TutorReview tutorReview) {
        TutorReviewDto dto = new TutorReviewDto();
        dto.setId(tutorReview.getId());
        dto.setTutorProfileId(tutorReview.getTutor().getId());
        dto.setStudentId(tutorReview.getStudent().getId());
        dto.setBookingId(tutorReview.getBooking().getId());
        dto.setRating(tutorReview.getRating());
        dto.setReviewText(tutorReview.getReviewText());
        dto.setIsVisible(tutorReview.getIsVisible());
        return dto;
    }
}
