package com.ying.learneyjourney.dto;

import com.ying.learneyjourney.entity.SocialMediaPost;
import lombok.Data;

import java.util.UUID;

@Data
public class SocialMediaPostDto {
    private UUID id;
    private UUID tutorProfileId;
    private String content;
    private String imageUrl;
    private String videoUrl;

    public static SocialMediaPostDto fromEntity(com.ying.learneyjourney.entity.SocialMediaPost post) {
        SocialMediaPostDto dto = new SocialMediaPostDto();
        dto.setId(post.getId());
        dto.setTutorProfileId(post.getTutorProfile() != null ? post.getTutorProfile().getId() : null);
        dto.setContent(post.getContent());
        dto.setImageUrl(post.getImageUrl());
        dto.setVideoUrl(post.getVideoUrl());
        return dto;
    }

    public static SocialMediaPost toEntity(SocialMediaPostDto dto) {
        SocialMediaPost post = new SocialMediaPost();
        post.setId(dto.getId());
        // Note: Setting tutorProfile requires fetching the TutorProfile entity by ID
        post.setContent(dto.getContent());
        post.setImageUrl(dto.getImageUrl());
        post.setVideoUrl(dto.getVideoUrl());
        return post;
    }
}
