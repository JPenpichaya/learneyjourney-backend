package com.ying.learneyjourney.entity;

import com.ying.learneyjourney.constaint.EnumCourseBadge;
import com.ying.learneyjourney.untils.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "course")
public class Course extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;
    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    @ManyToOne
    @JoinColumn(name = "tutor_profile_id")
    private TutorProfile tutorProfile;
    @Column(name = "image_url")
    private String imageUrl;
    @Column(name = "is_live")
    private Boolean isLive;
    @Enumerated(EnumType.STRING)
    @Column(name = "badge")
    private EnumCourseBadge badge;
    @Column(name = "price_thb")
    private Double priceThb;
}