package com.ying.learneyjourney.dto;

import com.ying.learneyjourney.entity.LoginAttempts;
import com.ying.learneyjourney.entity.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;
@Data
public class LoginAttemptsDto {
    private UUID id;
    private String userId;
    private LocalDateTime attemptTime;
    private boolean success;
    private String ipAddress;
    private String userAgent;

    public static LoginAttemptsDto from(LoginAttempts e) {
        LoginAttemptsDto dto = new LoginAttemptsDto();
        dto.setId(e.getId());
        dto.setUserId(e.getUser());
        dto.setAttemptTime(e.getAttemptTime());
        dto.setSuccess(e.isSuccess());
        dto.setIpAddress(e.getIpAddress());
        dto.setUserAgent(e.getUserAgent());
        return dto;
    }

    public static LoginAttempts toEntity(LoginAttemptsDto dto) {
        LoginAttempts e = new LoginAttempts();
        e.setId(dto.getId());
        e.setUser(dto.userId);
        e.setAttemptTime(dto.getAttemptTime());
        e.setSuccess(dto.isSuccess());
        e.setIpAddress(dto.getIpAddress());
        e.setUserAgent(dto.getUserAgent());
        return e;
    }
}
