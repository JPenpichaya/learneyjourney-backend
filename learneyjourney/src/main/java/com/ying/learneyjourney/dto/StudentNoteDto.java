package com.ying.learneyjourney.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class StudentNoteDto implements Serializable {
    private UUID id;
    private String userId;
    private UUID lessonId;
    private String content;
    private String imageUrl;
    private LocalDateTime videoAt;

    public static StudentNoteDto from(com.ying.learneyjourney.entity.StudentNote note) {
        StudentNoteDto dto = new StudentNoteDto();
        dto.setId(note.getId());
        dto.setUserId(note.getUser().getId());
        dto.setLessonId(note.getCourseVideo().getId());
        dto.setContent(note.getContent());
        dto.setImageUrl(note.getImageUrl());
        dto.setVideoAt(note.getVideoAt());
        return dto;
    }

    public static com.ying.learneyjourney.entity.StudentNote toEntity(StudentNoteDto dto) {
        com.ying.learneyjourney.entity.StudentNote note = new com.ying.learneyjourney.entity.StudentNote();
        note.setContent(dto.getContent());
        note.setImageUrl(dto.getImageUrl());
        note.setVideoAt(dto.getVideoAt());
        return note;
    }
}
