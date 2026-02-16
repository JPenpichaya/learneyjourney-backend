package com.ying.learneyjourney.entity;

import com.ying.learneyjourney.untils.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "course_video")
public class CourseVideo extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "lesson_id")
    private CourseLesson courseLesson;
    @Column(name = "title")
    private String title;
    @Column(name = "url")
    private String url;
    @Column(name = "duration")
    private Integer duration; // duration in seconds
    @Column(name = "position")
    private Integer position;
    @Column(name="worksheet")
    private String worksheet;

}