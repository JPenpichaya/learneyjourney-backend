package com.ying.learneyjourney.service;
import com.ying.learneyjourney.dto.CourseReviewDto;
import com.ying.learneyjourney.entity.Course;
import com.ying.learneyjourney.entity.CourseReview;
import com.ying.learneyjourney.entity.User;
import com.ying.learneyjourney.repository.CourseRepository;
import com.ying.learneyjourney.repository.CourseReviewRepository;
import com.ying.learneyjourney.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseReviewServiceTest {

    @Mock private CourseReviewRepository courseReviewRepository;
    @Mock private UserRepository userRepository;
    @Mock private CourseRepository courseRepository;

    @InjectMocks private CourseReviewService service;

    private UUID reviewId;
    private String userId;
    private UUID courseId;

    @BeforeEach
    void setUp() {
        reviewId = UUID.randomUUID();
        userId = "random-user-id";
        courseId = UUID.randomUUID();
    }

    @Test
    void create_shouldSaveAndSetUserAndCourse_whenTheyExist() {
        // arrange
        CourseReviewDto dto = new CourseReviewDto();
        dto.setUserId(userId);
        dto.setCourseId(courseId);

        User user = new User();
        user.setId(userId);

        Course course = new Course();
        course.setId(courseId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        when(courseReviewRepository.save(any(CourseReview.class)))
                .thenAnswer(inv -> {
                    CourseReview entity = inv.getArgument(0);
                    // simulate JPA-generated id
                    entity.setId(reviewId);
                    return entity;
                });

        // act
        CourseReviewDto out = service.create(dto);

        // assert
        assertNotNull(out);
        assertEquals(reviewId, out.getId());

        ArgumentCaptor<CourseReview> captor = ArgumentCaptor.forClass(CourseReview.class);
        verify(courseReviewRepository).save(captor.capture());
        CourseReview saved = captor.getValue();

        assertNotNull(saved.getUser());
        assertEquals(userId, saved.getUser().getId());

        assertNotNull(saved.getCourse());
        assertEquals(courseId, saved.getCourse().getId());

        verify(userRepository).findById(userId);
        verify(courseRepository).findById(courseId);
    }

    @Test
    void create_shouldSaveWithoutUser_whenUserNotFound() {
        // arrange
        CourseReviewDto dto = new CourseReviewDto();
        dto.setUserId(userId);
        dto.setCourseId(courseId);

        Course course = new Course();
        course.setId(courseId);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        when(courseReviewRepository.save(any(CourseReview.class)))
                .thenAnswer(inv -> {
                    CourseReview entity = inv.getArgument(0);
                    entity.setId(reviewId);
                    return entity;
                });

        // act
        CourseReviewDto out = service.create(dto);

        // assert
        assertEquals(reviewId, out.getId());

        ArgumentCaptor<CourseReview> captor = ArgumentCaptor.forClass(CourseReview.class);
        verify(courseReviewRepository).save(captor.capture());
        CourseReview saved = captor.getValue();

        assertNull(saved.getUser(), "User should be null when userId not found");
        assertNotNull(saved.getCourse(), "Course should be set when course exists");

        verify(userRepository).findById(userId);
        verify(courseRepository).findById(courseId);
    }

    @Test
    void create_shouldSaveWithoutCourse_whenCourseNotFound() {
        // arrange
        CourseReviewDto dto = new CourseReviewDto();
        dto.setUserId(userId);
        dto.setCourseId(courseId);

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        when(courseReviewRepository.save(any(CourseReview.class)))
                .thenAnswer(inv -> {
                    CourseReview entity = inv.getArgument(0);
                    entity.setId(reviewId);
                    return entity;
                });

        // act
        CourseReviewDto out = service.create(dto);

        // assert
        assertEquals(reviewId, out.getId());

        ArgumentCaptor<CourseReview> captor = ArgumentCaptor.forClass(CourseReview.class);
        verify(courseReviewRepository).save(captor.capture());
        CourseReview saved = captor.getValue();

        assertNotNull(saved.getUser(), "User should be set when user exists");
        assertNull(saved.getCourse(), "Course should be null when courseId not found");

        verify(userRepository).findById(userId);
        verify(courseRepository).findById(courseId);
    }

    @Test
    void create_shouldSaveWithoutUserAndCourse_whenBothNotFound() {
        // arrange
        CourseReviewDto dto = new CourseReviewDto();
        dto.setUserId(userId);
        dto.setCourseId(courseId);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        when(courseReviewRepository.save(any(CourseReview.class)))
                .thenAnswer(inv -> {
                    CourseReview entity = inv.getArgument(0);
                    entity.setId(reviewId);
                    return entity;
                });

        // act
        CourseReviewDto out = service.create(dto);

        // assert
        assertEquals(reviewId, out.getId());

        ArgumentCaptor<CourseReview> captor = ArgumentCaptor.forClass(CourseReview.class);
        verify(courseReviewRepository).save(captor.capture());
        CourseReview saved = captor.getValue();

        assertNull(saved.getUser());
        assertNull(saved.getCourse());
    }
}
