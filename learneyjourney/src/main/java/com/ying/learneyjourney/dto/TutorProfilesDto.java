package com.ying.learneyjourney.dto;

import lombok.Data;

import java.util.UUID;
@Data
public class TutorProfilesDto {
    private UUID id;
    private String userId;
    private String bio;

    public static TutorProfilesDto from(com.ying.learneyjourney.entity.TutorProfile tutorProfile) {
        TutorProfilesDto dto = new TutorProfilesDto();
        dto.setId(tutorProfile.getId());
        dto.setUserId(tutorProfile.getUser_id().getId());
        dto.setBio(tutorProfile.getBio());
        return dto;
    }

    public static com.ying.learneyjourney.entity.TutorProfile toEntity(TutorProfilesDto dto, com.ying.learneyjourney.entity.User user) {
        com.ying.learneyjourney.entity.TutorProfile tutorProfile = new com.ying.learneyjourney.entity.TutorProfile();
        tutorProfile.setId(dto.getId());
        tutorProfile.setUser_id(user);
        tutorProfile.setBio(dto.getBio());
        return tutorProfile;
    }
}
