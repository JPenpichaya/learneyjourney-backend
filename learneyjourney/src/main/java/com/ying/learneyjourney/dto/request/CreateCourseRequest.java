package com.ying.learneyjourney.dto.request;

import com.google.firebase.database.annotations.NotNull;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class CreateCourseRequest {

    @Valid
    @NotNull
    private CourseInfo courseInfo;

    @Valid
    @NotNull
    private List<LessonInfo> lessonInfo;

    // =============================
    // Inner Classes
    // =============================

    @Data
    public static class CourseInfo {

        private UUID id;

        @NotBlank
        private String title;

        private String subtitle;

        @NotBlank
        private String description;

        @NotNull
        private UUID tutorProfileId;

        @NotBlank
        private String category;

        @NotBlank
        private String level;

        private String imageUrl;

        private Boolean isLive = false;

        private String badge;

        @NotBlank
        private String access;

        @NotNull
        @DecimalMin(value = "0.0", inclusive = true)
        private Double priceThb;

        private String outcomes;
    }

    @Data
    public static class LessonInfo {

        @NotBlank
        private String title;

        @NotBlank
        private String description;

        @NotNull
        private Integer position;

        @Valid
        private List<VideoInfo> videos;
    }

    @Data
    public static class VideoInfo {

        @NotBlank
        private String title;

        @NotBlank
        private String url;

        @NotNull
        private Integer position;
        @NotBlank
        private Integer duration;
        private String contact;

    }
}