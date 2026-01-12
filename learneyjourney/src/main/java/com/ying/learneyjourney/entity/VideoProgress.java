package com.ying.learneyjourney.entity;

import com.ying.learneyjourney.constaint.EnumVideoProgressStatus;
import com.ying.learneyjourney.untils.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "video_progress")
public class VideoProgress extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @OneToOne
    @JoinColumn(name = "course_video_id")
    private CourseVideo courseVideo;
    @Column(name = "watched_seconds")
    private Integer watchedSeconds;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EnumVideoProgressStatus status;
    @Column(name = "last_watched_at")
    private LocalDateTime lastWatchedAt;

}