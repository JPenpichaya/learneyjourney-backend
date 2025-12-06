package com.ying.learneyjourney.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;
@Data
public class CourseDto implements Serializable {
    private UUID id;
    private String title;
    private String description;
    private UUID tutorProfileId;

    public static CourseDto from(com.ying.learneyjourney.entity.Course course) {
        CourseDto dto = new CourseDto();
        dto.setId(course.getId());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());
        dto.setTutorProfileId(course.getTutorProfile().getId());
        return dto;
    }

    public static com.ying.learneyjourney.entity.Course toEntity(CourseDto dto, com.ying.learneyjourney.entity.TutorProfile tutorProfile) {
        com.ying.learneyjourney.entity.Course course = new com.ying.learneyjourney.entity.Course();
        course.setId(dto.getId());
        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        course.setTutorProfile(tutorProfile);
        return course;
    }
}
