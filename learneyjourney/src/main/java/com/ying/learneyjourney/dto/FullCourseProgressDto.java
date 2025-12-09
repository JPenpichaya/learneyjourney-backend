package com.ying.learneyjourney.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class FullCourseProgressDto {
    private String userId;
    private UUID courseId;
    private int completedPercentage;
    private long totalLessons;
    private long completedLessons;
}
