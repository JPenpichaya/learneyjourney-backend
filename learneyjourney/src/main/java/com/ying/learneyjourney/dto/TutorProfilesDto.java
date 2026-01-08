package com.ying.learneyjourney.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.UUID;
@Data
public class TutorProfilesDto {
    private UUID id;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String userId;
    private String bio;

    public static TutorProfilesDto from(com.ying.learneyjourney.entity.TutorProfile tutorProfile) {
        TutorProfilesDto dto = new TutorProfilesDto();
        dto.setId(tutorProfile.getId());
        dto.setUserId(tutorProfile.getUser().getId());
        dto.setBio(tutorProfile.getBio());
        return dto;
    }

    public static com.ying.learneyjourney.entity.TutorProfile toEntity(TutorProfilesDto dto, com.ying.learneyjourney.entity.User user) {
        com.ying.learneyjourney.entity.TutorProfile tutorProfile = new com.ying.learneyjourney.entity.TutorProfile();
        tutorProfile.setId(dto.getId());
        tutorProfile.setUser(user);
        tutorProfile.setBio(dto.getBio());
        return tutorProfile;
    }
}
