package com.ying.learneyjourney.dto.response;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class LatestLessonUpdateResponse {
    private List<Course> course;
    private List<Live> live;
    private List<Worksheet> worksheet;

    @Data
    public static class Course {
        private UUID id;
        private String title;
        private Integer lastAtLesson;
        private Integer progress;
        private String duration;
    }

    @Data
    public static class Live {
        private UUID id;
        private String title;
        private String tutorName;
        private Double startAt;
        private String status;
    }

    @Data
    public static class Worksheet {
        private UUID id;
        private String title;
        private String description;
        private String duration;
    }
}
