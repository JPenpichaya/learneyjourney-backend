package com.ying.learneyjourney.dto;

import com.ying.learneyjourney.constaint.EnumLessonProgressStatus;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class StudentLessonProgressDto implements Serializable {
    private UUID lessonId;
    private String description;
    private EnumLessonProgressStatus status;
    private LocalDateTime completedAt;
    private String title;
    private String url;
    private Integer duration;
    private Integer position;
}
