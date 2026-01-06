package com.ying.learneyjourney.service;
import com.ying.learneyjourney.entity.Course;
import com.ying.learneyjourney.entity.User;
import com.ying.learneyjourney.repository.CourseRepository;
import com.ying.learneyjourney.repository.UserRepository;
import com.ying.learneyjourney.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private EmailService emailService;

    // -------------------- sendEnrollmentConfirmation --------------------

    @Test
    void sendEnrollmentConfirmation_shouldSendEmail_whenUserAndCourseExist() {
        // arrange
        String userId = "user-1";
        UUID courseId = UUID.randomUUID();
        String sessionId = "sess_123";

        User user = new User();
        user.setId(userId);
        user.setEmail("user@test.com");
        user.setDisplayName("John");

        Course course = new Course();
        course.setId(courseId);
        course.setTitle("Java Basics");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        ArgumentCaptor<SimpleMailMessage> mailCaptor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        // act
        emailService.sendEnrollmentConfirmation(userId, courseId, sessionId);

        // assert
        verify(mailSender).send(mailCaptor.capture());

        SimpleMailMessage sent = mailCaptor.getValue();

        assertEquals("no-reply@learneyjourney.com", sent.getFrom());
        assertArrayEquals(new String[]{"user@test.com"}, sent.getTo());
        assertEquals("You’re enrolled in Java Basics", sent.getSubject());

        assertTrue(sent.getText().contains("John"));
        assertTrue(sent.getText().contains("Java Basics"));
        assertTrue(sent.getText().contains(sessionId));
    }

    @Test
    void sendEnrollmentConfirmation_shouldThrow_whenUserNotFound() {
        String userId = "missing";
        UUID courseId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> emailService.sendEnrollmentConfirmation(userId, courseId, "s")
        );

        assertEquals("User not found", ex.getMessage());
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEnrollmentConfirmation_shouldThrow_whenCourseNotFound() {
        String userId = "user";
        UUID courseId = UUID.randomUUID();

        User user = new User();
        user.setId(userId);
        user.setEmail("user@test.com");
        user.setDisplayName("John");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> emailService.sendEnrollmentConfirmation(userId, courseId, "s")
        );

        assertEquals("Course not found", ex.getMessage());
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    // -------------------- sendTestEmail --------------------

    @Test
    void sendTestEmail_shouldSendEmail() {
        // arrange
        String email = "test@example.com";

        ArgumentCaptor<SimpleMailMessage> mailCaptor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        // act
        emailService.sendTestEmail(email);

        // assert
        verify(mailSender).send(mailCaptor.capture());

        SimpleMailMessage sent = mailCaptor.getValue();

        assertEquals("no-reply@learneyjourney.com", sent.getFrom());
        assertArrayEquals(new String[]{email}, sent.getTo());
        assertEquals("Test email", sent.getSubject());
        assertEquals("Hi this is a test email", sent.getText());
    }
}
