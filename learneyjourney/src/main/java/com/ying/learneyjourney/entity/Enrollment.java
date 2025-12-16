package com.ying.learneyjourney.entity;

import com.ying.learneyjourney.constaint.EnumEnrollmentStatus;
import com.ying.learneyjourney.untils.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "enrollment")
public class Enrollment extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EnumEnrollmentStatus status;
    @Column(name = "progress")
    private int progress;
    @Column(name = "completion_at")
    private LocalDateTime completionAt;
    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

}