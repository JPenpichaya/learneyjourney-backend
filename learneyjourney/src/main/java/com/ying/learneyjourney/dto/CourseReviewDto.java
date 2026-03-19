package com.ying.learneyjourney.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ying.learneyjourney.entity.CourseReview;
import lombok.Data;

import java.util.UUID;
@Data
public class CourseReviewDto {
    private UUID id;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private UUID courseId;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String userId;
    private Integer rating;
    private String review;
    private Boolean isVisible;

    public static CourseReviewDto fromEntity(com.ying.learneyjourney.entity.CourseReview courseReview) {
        CourseReviewDto dto = new CourseReviewDto();
        dto.setId(courseReview.getId());
        dto.setCourseId(courseReview.getCourse().getId());
        dto.setUserId(courseReview.getUser().getId().toString());
        dto.setRating(courseReview.getRating());
        dto.setReview(courseReview.getReview());
        dto.setIsVisible(courseReview.getIsVisible());
        return dto;
    }

    public static CourseReview toEntity(CourseReviewDto dto) {
        CourseReview courseReview = new CourseReview();
        // Note: Setting course and user entities would require fetching them from the database
        courseReview.setRating(dto.getRating());
        courseReview.setReview(dto.getReview());
        courseReview.setIsVisible(dto.getIsVisible());
        return courseReview;
    }
}
