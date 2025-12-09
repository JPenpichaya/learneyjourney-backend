package com.ying.learneyjourney.dto;

import com.ying.learneyjourney.entity.Course;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class CourseLessonDto implements Serializable {
    private UUID id;

    private UUID courseId;

    private String title;

    private String description;

    private Integer position;
    public static CourseLessonDto from(com.ying.learneyjourney.entity.CourseLesson lesson){
        CourseLessonDto dto = new CourseLessonDto();
        dto.setId(lesson.getId());
        dto.setCourseId(lesson.getCourse().getId());
        dto.setTitle(lesson.getTitle());
        dto.setDescription(lesson.getDescription());
        dto.setPosition(lesson.getPosition());
        return dto;
    }
    public static com.ying.learneyjourney.entity.CourseLesson toEntity(CourseLessonDto dto){
        com.ying.learneyjourney.entity.CourseLesson lesson = new com.ying.learneyjourney.entity.CourseLesson();
        lesson.setId(dto.getId());
        lesson.setTitle(dto.getTitle());
        lesson.setDescription(dto.getDescription());
        lesson.setPosition(dto.getPosition());
        return lesson;
    }
}
