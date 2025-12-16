package com.ying.learneyjourney.dto;

import com.ying.learneyjourney.constaint.EnumCourseBadge;
import jakarta.persistence.Column;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;
@Data
public class CourseDto implements Serializable {
    private UUID id;
    private String title;
    private String description;
    private UUID tutorProfileId;
    private String imageUrl;
    private Boolean isLive;
    private EnumCourseBadge badge;
    private Double priceThb;
    private String priceId;

    public static CourseDto from(com.ying.learneyjourney.entity.Course course) {
        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());
        dto.setTutorProfileId(course.getTutorProfile().getId());
        dto.setImageUrl(course.getImageUrl());
        dto.setIsLive(course.getIsLive());
        dto.setBadge(course.getBadge());
        dto.setPriceThb(course.getPriceThb());
        dto.setPriceId(course.getPriceId());
        return dto;
    }

    public static com.ying.learneyjourney.entity.Course toEntity(CourseDto dto, com.ying.learneyjourney.entity.TutorProfile tutorProfile) {
        com.ying.learneyjourney.entity.Course course = new com.ying.learneyjourney.entity.Course();
        course.setId(dto.getId());
        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        course.setTutorProfile(tutorProfile);
        course.setImageUrl(dto.getImageUrl());
        course.setIsLive(dto.getIsLive());
        course.setBadge(dto.getBadge());
        course.setPriceThb(dto.getPriceThb());
        course.setPriceId(dto.getPriceId());
        return course;
    }
}
