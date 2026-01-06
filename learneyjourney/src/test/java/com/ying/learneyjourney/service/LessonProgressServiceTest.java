package com.ying.learneyjourney.service;
import com.ying.learneyjourney.Util.TimeManagement;
import com.ying.learneyjourney.constaint.EnumLessonProgressStatus;
import com.ying.learneyjourney.constaint.EnumVideoProgressStatus;
import com.ying.learneyjourney.dto.*;
import com.ying.learneyjourney.entity.CourseLesson;
import com.ying.learneyjourney.entity.LessonProgress;
import com.ying.learneyjourney.entity.User;
import com.ying.learneyjourney.repository.*;
import com.ying.learneyjourney.service.LessonProgressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LessonProgressServiceTest {

    @Mock private LessonProgressRepository lessonProgressRepository;
    @Mock private CourseLessonRepository courseLessonRepository;
    @Mock private UserRepository userRepository;
    @Mock private CourseVideoRepository courseVideoRepository;
    @Mock private VideoProgressRepository videoProgressRepository;

    @InjectMocks
    private LessonProgressService service;

    private UUID lessonId1;
    private UUID lessonId2;
    private UUID courseId;
    private UUID progressId;
    private String userId;

    @BeforeEach
    void setUp() {
        lessonId1 = UUID.randomUUID();
        lessonId2 = UUID.randomUUID();
        courseId = UUID.randomUUID();
        progressId = UUID.randomUUID();
        userId = "user-1";
    }

    // -------------------- create --------------------

    @Test
    void create_shouldSetUserAndLessonAndSave_whenFound() {
        // arrange
        LessonProgressDto dto = new LessonProgressDto();
        dto.setUserId(userId);
        dto.setCourseLessonId(lessonId1);

        User user = new User();
        user.setId(userId);

        CourseLesson lesson = new CourseLesson();
        lesson.setId(lessonId1);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(courseLessonRepository.findById(lessonId1)).thenReturn(Optional.of(lesson));

        when(lessonProgressRepository.save(any(LessonProgress.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // act
        LessonProgressDto out = service.create(dto);

        // assert
        assertNotNull(out);

        ArgumentCaptor<LessonProgress> captor = ArgumentCaptor.forClass(LessonProgress.class);
        verify(lessonProgressRepository).save(captor.capture());
        LessonProgress saved = captor.getValue();

        assertNotNull(saved.getUser());
        assertEquals(userId, saved.getUser().getId());
        assertNotNull(saved.getCourseLesson());
        assertEquals(lessonId1, saved.getCourseLesson().getId());
    }

    // -------------------- update --------------------

    @Test
    void update_shouldUpdateStatusAndCompletedAt_andSave() {
        LocalDateTime completedAt = LocalDateTime.now();
        LocalDateTime completedAt2 = LocalDateTime.now().plusHours(1);
        // arrange
        LessonProgress existing = new LessonProgress();
        existing.setId(progressId);
        existing.setStatus(EnumLessonProgressStatus.NOT_START);
        existing.setCompletedAt(completedAt);
        existing.setUser(new User());
        existing.setCourseLesson(new CourseLesson());

        when(lessonProgressRepository.findById(progressId)).thenReturn(Optional.of(existing));
        when(lessonProgressRepository.save(any(LessonProgress.class))).thenAnswer(inv -> inv.getArgument(0));

        LessonProgressDto dto = new LessonProgressDto();
        dto.setStatus(EnumLessonProgressStatus.COMPLETED);
        dto.setCompletedAt(completedAt2);

        // act
        LessonProgressDto out = service.update(progressId, dto);

        // assert
        verify(lessonProgressRepository).save(existing);
        assertNotNull(out);
        assertEquals(EnumLessonProgressStatus.COMPLETED, existing.getStatus());
        assertEquals(completedAt2, existing.getCompletedAt());
    }

    @Test
    void update_shouldThrow_whenNotFound() {
        when(lessonProgressRepository.findById(progressId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.update(progressId, new LessonProgressDto())
        );

        assertEquals("Lesson progress not found", ex.getMessage());
        verify(lessonProgressRepository, never()).save(any());
    }

    // -------------------- getById --------------------

    @Test
    void getById_shouldReturnDto_whenFound() {
        LessonProgress lp = new LessonProgress();
        lp.setId(progressId);
        lp.setUser(new User());
        lp.setCourseLesson(new CourseLesson());



        when(lessonProgressRepository.findById(progressId)).thenReturn(Optional.of(lp));

        LessonProgressDto out = service.getById(progressId);

        assertNotNull(out);
        assertEquals(progressId, out.getId());
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        when(lessonProgressRepository.findById(progressId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.getById(progressId)
        );
        assertEquals("Lesson progress not found", ex.getMessage());
    }

    // -------------------- deleteById --------------------

    @Test
    void deleteById_shouldDelete_whenExists() {
        when(lessonProgressRepository.existsById(progressId)).thenReturn(true);

        service.deleteById(progressId);

        verify(lessonProgressRepository).deleteById(progressId);
    }

    @Test
    void deleteById_shouldThrow_whenNotExists() {
        when(lessonProgressRepository.existsById(progressId)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.deleteById(progressId)
        );
        assertEquals("Lesson progress not found", ex.getMessage());
        verify(lessonProgressRepository, never()).deleteById(any());
    }

    // -------------------- getByCourseId --------------------

    @Test
    void getByCourseId_shouldReturnProgressDtos_andSetCourseLessonId() {
        // arrange lessons in course
        CourseLesson l1 = new CourseLesson(); l1.setId(lessonId1);
        CourseLesson l2 = new CourseLesson(); l2.setId(lessonId2);
        when(courseLessonRepository.findByCourse(courseId)).thenReturn(List.of(l1, l2));

        // arrange progresses (must contain courseLesson to avoid NPE in convertToDto)
        LessonProgress p1 = new LessonProgress();
        p1.setId(UUID.randomUUID());
        p1.setCourseLesson(l1);
        p1.setUser(new User());

        LessonProgress p2 = new LessonProgress();
        p2.setId(UUID.randomUUID());
        p2.setCourseLesson(l2);
        p2.setUser(new User());

        when(lessonProgressRepository.findByCourseLessonIdInAndUserId(
                List.of(lessonId1, lessonId2), userId
        )).thenReturn(List.of(p1, p2));

        // act
        List<LessonProgressDto> out = service.getByCourseId(userId, courseId);

        // assert
        assertEquals(2, out.size());
        assertEquals(lessonId1, out.get(0).getCourseLessonId());
        assertEquals(lessonId2, out.get(1).getCourseLessonId());
    }

    // -------------------- getFullCourseProgress --------------------

    @Test
    void getFullCourseProgress_shouldComputePercentageCorrectly() {
        // lessons
        CourseLesson l1 = new CourseLesson(); l1.setId(lessonId1);
        CourseLesson l2 = new CourseLesson(); l2.setId(lessonId2);
        when(courseLessonRepository.findByCourse(courseId)).thenReturn(List.of(l1, l2));

        // progresses: 1 completed, 1 not
        LessonProgress p1 = new LessonProgress();
        p1.setCourseLesson(l1);
        p1.setStatus(EnumLessonProgressStatus.COMPLETED);

        LessonProgress p2 = new LessonProgress();
        p2.setCourseLesson(l2);
        p2.setStatus(EnumLessonProgressStatus.PROGRESS);

        when(lessonProgressRepository.findByCourseLessonIdInAndUserId(
                List.of(lessonId1, lessonId2), userId
        )).thenReturn(List.of(p1, p2));

        FullCourseProgressDto out = service.getFullCourseProgress(courseId, userId);

        assertEquals(courseId, out.getCourseId());
        assertEquals(userId, out.getUserId());
        assertEquals(2L, out.getTotalLessons());
        assertEquals(1L, out.getCompletedLessons());
        assertEquals(50, out.getCompletedPercentage());
    }

    @Test
    void getFullCourseProgress_shouldReturnZero_whenNoLessons() {
        when(courseLessonRepository.findByCourse(courseId)).thenReturn(List.of());
        when(lessonProgressRepository.findByCourseLessonIdInAndUserId(List.of(), userId)).thenReturn(List.of());

        FullCourseProgressDto out = service.getFullCourseProgress(courseId, userId);

        assertEquals(0, out.getCompletedPercentage());
        assertEquals(0L, out.getTotalLessons());
        assertEquals(0L, out.getCompletedLessons());
    }

    // -------------------- getStudentLessonProgress --------------------

    @Test
    void getStudentLessonProgress_shouldBuildLessonDtosWithVideos_andSetLessonStatusWhenProgressExists() {
        // arrange lessons
        CourseLesson lesson = new CourseLesson();
        lesson.setId(lessonId1);
        lesson.setTitle("Lesson 1");
        lesson.setPosition(1);

        when(courseLessonRepository.findByCourse(courseId)).thenReturn(List.of(lesson));

        // progress exists for lesson -> status should be set
        LessonProgress lp = new LessonProgress();
        lp.setCourseLesson(lesson);
        lp.setStatus(EnumLessonProgressStatus.COMPLETED);

        when(lessonProgressRepository.findByCourseLessonIdInAndUserId(
                List.of(lessonId1), userId
        )).thenReturn(List.of(lp));

        // video progress rows
        VideoProgressRepository.VideoProgressRow row = mock(VideoProgressRepository.VideoProgressRow.class);
        UUID rowId = UUID.randomUUID();

        when(row.getId()).thenReturn(rowId);
        when(row.getDescription()).thenReturn("Desc");
        when(row.getStatus()).thenReturn("COMPLETED"); // must match EnumVideoProgressStatus
        when(row.getCompletedAt()).thenReturn(40);
        when(row.getTitle()).thenReturn("Video 1");
        when(row.getUrl()).thenReturn("http://v");
        when(row.getDuration()).thenReturn(3661); // 1:01:01
        when(row.getPosition()).thenReturn(1);

        when(videoProgressRepository.findVideoProgressRows(userId, lessonId1))
                .thenReturn(List.of(row));

        // act
        List<StudentLessonProgressDto> out = service.getStudentLessonProgress(courseId, userId);

        // assert
        assertEquals(1, out.size());

        StudentLessonProgressDto dto = out.get(0);
        assertEquals(lessonId1, dto.getLessonId());
        assertEquals("Lesson 1", dto.getTitle());
        assertEquals(1, dto.getPosition());
        assertEquals(EnumLessonProgressStatus.COMPLETED, dto.getStatus());

        assertNotNull(dto.getVideos());
        assertEquals(1, dto.getVideos().size());

        VideoProgressRowDto v = dto.getVideos().get(0);
        assertEquals(rowId, v.getId());
        assertEquals("Desc", v.getDescription());
        assertEquals(EnumVideoProgressStatus.COMPLETED, v.getStatus());
        assertEquals(40, v.getCompletedAt());
        assertEquals("Video 1", v.getTitle());
        assertEquals("http://v", v.getUrl());
        assertEquals(1, v.getPosition());

        // duration formatted via TimeManagement.formatSecondsToHMS
        assertEquals(TimeManagement.formatSecondsToHMS(3661), v.getDuration());
    }

    @Test
    void getStudentLessonProgress_shouldNotSetStatus_whenNoProgressForLesson() {
        CourseLesson lesson = new CourseLesson();
        lesson.setId(lessonId1);
        lesson.setTitle("Lesson 1");
        lesson.setPosition(1);

        when(courseLessonRepository.findByCourse(courseId)).thenReturn(List.of(lesson));
        when(lessonProgressRepository.findByCourseLessonIdInAndUserId(List.of(lessonId1), userId))
                .thenReturn(List.of()); // no progress

        when(videoProgressRepository.findVideoProgressRows(userId, lessonId1))
                .thenReturn(List.of());

        List<StudentLessonProgressDto> out = service.getStudentLessonProgress(courseId, userId);

        assertEquals(1, out.size());
        assertNull(out.get(0).getStatus(), "Status should be null when no LessonProgress exists");
        assertEquals(0, out.get(0).getVideos().size());
    }
}

