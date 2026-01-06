package com.ying.learneyjourney.service;

import com.ying.learneyjourney.dto.LoginAttemptsDto;
import com.ying.learneyjourney.entity.LoginAttempts;
import com.ying.learneyjourney.repository.LoginAttemptsRepository;
import com.ying.learneyjourney.service.LoginAttemptsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginAttemptsServiceTest {

    @Mock
    private LoginAttemptsRepository loginAttemptsRepository;

    @InjectMocks
    private LoginAttemptsService service;

    @Test
    void recordLoginAttempt_shouldConvertDtoToEntity_andSave() {
        // arrange
        LoginAttemptsDto dto = new LoginAttemptsDto();
        dto.setUserId("user-1");
        dto.setIpAddress("1.2.3.4");
        dto.setUserAgent("JUnit");
        dto.setSuccess(true);
        dto.setAttemptTime(LocalDateTime.now());

        ArgumentCaptor<LoginAttempts> captor = ArgumentCaptor.forClass(LoginAttempts.class);

        // act
        service.recordLoginAttempt(dto);

        // assert
        verify(loginAttemptsRepository).save(captor.capture());

        LoginAttempts saved = captor.getValue();
        assertNotNull(saved, "Saved entity should not be null");

        // These asserts assume your LoginAttemptsDto.toEntity maps these fields.
        // If any of these fail, it means your mapper doesn't set them.
        assertEquals(dto.getUserId(), saved.getUser());
        assertEquals(dto.getIpAddress(), saved.getIpAddress());
        assertEquals(dto.getUserAgent(), saved.getUserAgent());
        assertEquals(dto.isSuccess(), saved.isSuccess());
        assertEquals(dto.getAttemptTime(), saved.getAttemptTime());
    }
}

