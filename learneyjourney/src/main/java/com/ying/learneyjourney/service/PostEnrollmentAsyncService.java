package com.ying.learneyjourney.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostEnrollmentAsyncService {
    private final EmailService emailService;

    @Async
    public void sendingEmailAfterEnrolled(String userId, UUID courseId, String sessionId) {
        try {
            emailService.sendEnrollmentConfirmation(userId, courseId, sessionId);
        } catch (Exception e) {
            // Do NOT throw — async failures should not affect enrollment
            log.error("Failed to send enrollment email: userId={}, courseId={}, sessionId={}",
                    userId, courseId, sessionId, e);
        }
    }
}
