package com.ying.learneyjourney.service;

import com.ying.learneyjourney.service.EmailService;
import com.ying.learneyjourney.service.PostEnrollmentAsyncService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostEnrollmentAsyncServiceTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PostEnrollmentAsyncService service;

    @Test
    void sendingEmailAfterEnrolled_shouldCallEmailService() {
        String userId = "user-1";
        UUID courseId = UUID.randomUUID();
        String sessionId = "cs_test_123";

        service.sendingEmailAfterEnrolled(userId, courseId, sessionId);

        verify(emailService).sendEnrollmentConfirmation(userId, courseId, sessionId);
    }

    @Test
    void sendingEmailAfterEnrolled_shouldNotThrow_whenEmailServiceThrows() {
        String userId = "user-1";
        UUID courseId = UUID.randomUUID();
        String sessionId = "cs_test_123";

        doThrow(new RuntimeException("SMTP down"))
                .when(emailService)
                .sendEnrollmentConfirmation(userId, courseId, sessionId);

        assertDoesNotThrow(() ->
                service.sendingEmailAfterEnrolled(userId, courseId, sessionId)
        );

        verify(emailService).sendEnrollmentConfirmation(userId, courseId, sessionId);
    }
}
