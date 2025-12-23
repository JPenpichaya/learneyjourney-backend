package com.ying.learneyjourney.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ying.learneyjourney.constaint.EnumLessonProgressStatus;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class LessonProgressDto implements Serializable {
    private UUID id;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String userId;
    private EnumLessonProgressStatus status;
    private LocalDateTime completedAt;
    private UUID courseLessonId;

    public static LessonProgressDto from(com.ying.learneyjourney.entity.LessonProgress lp) {
        LessonProgressDto dto = new LessonProgressDto();
        dto.setId(lp.getCourseLesson().getId());
        dto.setUserId(lp.getUser().getId());
        dto.setStatus(lp.getStatus());
        dto.setCompletedAt(lp.getCompletedAt());
        return dto;
    }

    public static com.ying.learneyjourney.entity.LessonProgress toEntity(LessonProgressDto dto) {
        com.ying.learneyjourney.entity.LessonProgress lp = new com.ying.learneyjourney.entity.LessonProgress();
        lp.setStatus(dto.getStatus());
        lp.setCompletedAt(dto.getCompletedAt());
        return lp;
    }
}
