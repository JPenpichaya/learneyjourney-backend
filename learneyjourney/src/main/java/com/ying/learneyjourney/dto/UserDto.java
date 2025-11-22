package com.ying.learneyjourney.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UserDto {
    private UUID id;
    private String email;
    private String displayName;
    private String photoUrl;
}
