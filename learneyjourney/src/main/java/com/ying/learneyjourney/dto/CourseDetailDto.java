package com.ying.learneyjourney.dto;

import lombok.Data;

import java.util.UUID;
@Data
public class CourseDetailDto {
    private UUID id;
    private Long totalLessons;
    private Long totalStudents;
    private Double rating;
    private String totalDuration;
}
