package com.ying.learneyjourney.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ying.learneyjourney.constaint.EnumUserRoles;
import com.ying.learneyjourney.entity.User;
import lombok.Data;

import java.util.UUID;

@Data
public class UserDto {
    private String id;
    private String email;
    private String displayName;
    private String photoUrl;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String stripConnect;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private EnumUserRoles role;
    private String bio;

    public static UserDto from(User e){
        UserDto dto = new UserDto();
        dto.setId(e.getId());
        dto.setEmail(e.getEmail());
        dto.setDisplayName(e.getDisplayName());
        dto.setPhotoUrl(e.getPhotoUrl());
        dto.setStripConnect(e.getStripConnect());
        dto.setRole(e.getRole());
        dto.setBio(e.getBio());
        return dto;
    }

    public static User toEntity(UserDto dto){
        User e = new User();
        e.setId(dto.getId());
        e.setEmail(dto.getEmail());
        e.setDisplayName(dto.getDisplayName());
        e.setPhotoUrl(dto.getPhotoUrl());
        e.setStripConnect(dto.getStripConnect());
        e.setRole(dto.getRole());
        e.setBio(dto.getBio());
        return e;
    }
}
