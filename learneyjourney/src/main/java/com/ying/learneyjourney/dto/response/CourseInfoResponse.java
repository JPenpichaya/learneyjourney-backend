package com.ying.learneyjourney.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class CourseInfoResponse {
    private UUID id;
    private String title;
    private String subtitle;
    private String description;
    private String category;
    private String level;
    private String duration;
    private Boolean haveCertificate;
    private Long lessons;
    private String outcomes;
    private Double rate;
    private String students;
    private TutorProfile tutorProfile;

    @Data
    public static class TutorProfile {
        private String name;
        private String title;
    }
}
