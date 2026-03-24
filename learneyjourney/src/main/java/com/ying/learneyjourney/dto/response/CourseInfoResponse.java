package com.ying.learneyjourney.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
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
    private String priceId;

    @Data
    public static class TutorProfile {
        private String name;
        private String title;
        private String photoUrl;
    }
    public record WorksheetSummaryResponse(
            UUID id,
            String title,
            String subject,
            String language,
            String activeVersionLabel,
            int versionCount,
            int exportCount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
    }
}
