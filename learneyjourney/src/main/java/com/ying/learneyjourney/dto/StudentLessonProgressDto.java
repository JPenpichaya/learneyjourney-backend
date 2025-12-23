package com.ying.learneyjourney.dto;

import com.ying.learneyjourney.constaint.EnumLessonProgressStatus;
import com.ying.learneyjourney.entity.VideoProgress;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class StudentLessonProgressDto implements Serializable {
    private UUID lessonId;
    private String title;
    private List<VideoProgressRowDto> videos;
    private Integer position;
    private EnumLessonProgressStatus status;
}
