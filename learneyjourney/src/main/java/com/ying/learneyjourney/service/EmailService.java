package com.ying.learneyjourney.service;

import com.ying.learneyjourney.entity.Course;
import com.ying.learneyjourney.entity.User;
import com.ying.learneyjourney.repository.CourseRepository;
import com.ying.learneyjourney.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;


    public void sendEnrollmentConfirmation(String userId, UUID courseId, String sessionId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("You’re enrolled in " + course.getTitle());
        message.setText(
                "Hi " + user.getDisplayName() + ",\n\n" +
                        "Your enrollment was successful!\n\n" +
                        "Course: " + course.getTitle() + "\n" +
                        "Session ID: " + sessionId + "\n\n" +
                        "You can now access the course from your dashboard.\n\n" +
                        "Happy learning!\n" +
                        "Learney Journey Team"
        );

        mailSender.send(message);
    }
}