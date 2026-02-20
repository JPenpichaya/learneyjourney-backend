package com.ying.learneyjourney.entity;

import com.ying.learneyjourney.constaint.EnumLessonProgressStatus;
import com.ying.learneyjourney.untils.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "lesson_progress")
public class LessonProgress extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @OneToOne
    @JoinColumn(name = "course_lesson_id")
    private CourseLesson courseLesson;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EnumLessonProgressStatus status;
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

}