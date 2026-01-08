package com.ying.learneyjourney.entity;

import com.ying.learneyjourney.untils.Auditable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "social_media_post")
public class SocialMediaPost extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "tutor_profile_id")
    private TutorProfile tutorProfile;
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
    @Column(name = "image_url")
    private String imageUrl;
    @Column(name = "video_url")
    private String videoUrl;
}