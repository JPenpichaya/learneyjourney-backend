package com.ying.learneyjourney.dto;

import com.ying.learneyjourney.constaint.EnumEnrollmentStatus;
import com.ying.learneyjourney.entity.Course;
import com.ying.learneyjourney.entity.Enrollment;
import jakarta.persistence.Column;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
@Data
public class EnrollmentDto implements Serializable {
    private UUID id;
    private String userId;
    private UUID courseId;
    private EnumEnrollmentStatus status;
    private int progress;
    private LocalDateTime completionAt;
    private LocalDateTime lastAccessedAt;

    public static EnrollmentDto from(Enrollment e){
        EnrollmentDto dto = new EnrollmentDto();
        dto.setId(e.getId());
        dto.setUserId(e.getUser().getId());
        dto.setCourseId(e.getCourse().getId());
        dto.setStatus(e.getStatus());
        dto.setProgress(e.getProgress());
        dto.setCompletionAt(e.getCompletionAt());
        dto.setLastAccessedAt(e.getLastAccessedAt());
        return dto;
    }

    public static Enrollment toEntity(EnrollmentDto dto){
        Enrollment e = new Enrollment();
        e.setId(dto.getId());
        e.setStatus(dto.getStatus());
        e.setProgress(dto.getProgress());
        e.setCompletionAt(dto.getCompletionAt());
        e.setLastAccessedAt(dto.getLastAccessedAt());
        return e;
    }
}
