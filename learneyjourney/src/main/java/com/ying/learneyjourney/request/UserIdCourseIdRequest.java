package com.ying.learneyjourney.request;

import lombok.Data;

import java.util.UUID;

@Data
public class UserIdCourseIdRequest {
    private String userId;
    private UUID courseId;
    private UUID lessonId;
}
