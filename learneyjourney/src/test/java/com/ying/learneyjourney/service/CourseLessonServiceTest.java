package com.ying.learneyjourney.service;

import com.ying.learneyjourney.dto.CourseLessonDto;
import com.ying.learneyjourney.entity.Course;
import com.ying.learneyjourney.entity.CourseLesson;
import com.ying.learneyjourney.repository.CourseLessonRepository;
import com.ying.learneyjourney.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseLessonServiceTest {

    @Mock private CourseLessonRepository courseLessonRepository;
    @Mock private CourseRepository courseRepository;

    @InjectMocks private CourseLessonService service;

    private UUID lessonId;
    private UUID courseId;

    @BeforeEach
    void setup() {
        lessonId = UUID.randomUUID();
        courseId = UUID.randomUUID();
    }

    @Test
    void create_shouldSaveAndSetCourse_whenCourseExists() {
        // arrange
        CourseLessonDto dto = new CourseLessonDto();
        dto.setCourseId(courseId);
        dto.setTitle("L1");
        dto.setDescription("D1");
        dto.setPosition(1);

        Course course = new Course();
        course.setId(courseId);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        // Capture the entity passed to save, and simulate JPA setting an ID
        when(courseLessonRepository.save(any(CourseLesson.class)))
                .thenAnswer(inv -> {
                    CourseLesson e = inv.getArgument(0);
                    e.setId(lessonId);
                    return e;
                });

        // act
        CourseLessonDto out = service.create(dto);

        // assert
        assertNotNull(out);
        assertEquals(lessonId, out.getId());

        ArgumentCaptor<CourseLesson> captor = ArgumentCaptor.forClass(CourseLesson.class);
        verify(courseLessonRepository).save(captor.capture());
        CourseLesson saved = captor.getValue();

        assertNotNull(saved);
        assertNotNull(saved.getCourse());
        assertEquals(courseId, saved.getCourse().getId());
        verify(courseRepository).findById(courseId);
    }

    @Test
    void create_shouldSaveWithoutCourse_whenCourseNotFound() {
        // arrange
        CourseLessonDto dto = new CourseLessonDto();
        dto.setCourseId(courseId);
        dto.setTitle("L1");
        dto.setDescription("D1");
        dto.setPosition(1);

        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        when(courseLessonRepository.save(any(CourseLesson.class)))
                .thenAnswer(inv -> {
                    CourseLesson e = inv.getArgument(0);
                    e.setId(lessonId);
                    return e;
                });

        // act
        CourseLessonDto out = service.create(dto);

        // assert
        assertEquals(lessonId, out.getId());

        ArgumentCaptor<CourseLesson> captor = ArgumentCaptor.forClass(CourseLesson.class);
        verify(courseLessonRepository).save(captor.capture());
        CourseLesson saved = captor.getValue();

        assertNull(saved.getCourse(), "Course should stay null when courseId not found");
        verify(courseRepository).findById(courseId);
    }

    @Test
    void update_shouldUpdateFieldsAndSave_whenLessonExists() {
        // arrange
        CourseLesson existing = new CourseLesson();
        existing.setId(lessonId);
        existing.setTitle("Old");
        existing.setDescription("OldD");
        existing.setPosition(9);

        CourseLessonDto dto = new CourseLessonDto();
        dto.setTitle("New");
        dto.setDescription("NewD");
        dto.setPosition(1);

        when(courseLessonRepository.findById(lessonId)).thenReturn(Optional.of(existing));

        // act
        CourseLessonDto out = service.update(lessonId, dto);

        // assert
        assertSame(dto, out);

        verify(courseLessonRepository).save(existing);
        assertEquals("New", existing.getTitle());
        assertEquals("NewD", existing.getDescription());
        assertEquals(1, existing.getPosition());
    }

    @Test
    void update_shouldThrow_whenLessonNotFound() {
        // arrange
        CourseLessonDto dto = new CourseLessonDto();
        when(courseLessonRepository.findById(lessonId)).thenReturn(Optional.empty());

        // act + assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.update(lessonId, dto)
        );
        assertEquals("Course lesson not found", ex.getMessage());

        verify(courseLessonRepository, never()).save(any());
    }

    @Test
    void getById_shouldReturnDto_whenLessonExists() {
        // arrange
        CourseLesson existing = new CourseLesson();
        existing.setId(lessonId);
        existing.setTitle("T");
        existing.setDescription("D");
        existing.setPosition(1);
        existing.setCourse(new Course());

        when(courseLessonRepository.findById(lessonId)).thenReturn(Optional.of(existing));

        // act
        CourseLessonDto out = service.getById(lessonId);

        // assert
        assertNotNull(out);
        assertEquals(lessonId, out.getId());
        assertEquals("T", out.getTitle());
        assertEquals("D", out.getDescription());
        assertEquals(1, out.getPosition());
    }

    @Test
    void getById_shouldThrow_whenLessonNotFound() {
        // arrange
        when(courseLessonRepository.findById(lessonId)).thenReturn(Optional.empty());

        // act + assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.getById(lessonId)
        );
        assertEquals("Course lesson not found", ex.getMessage());
    }

    @Test
    void getAll_shouldMapEntitiesToDtos() {
        // arrange
        CourseLesson a = new CourseLesson();
        a.setId(UUID.randomUUID());
        a.setTitle("A");
        a.setDescription("DA");
        a.setPosition(1);
        a.setCourse(new Course());

        CourseLesson b = new CourseLesson();
        b.setId(UUID.randomUUID());
        b.setTitle("B");
        b.setDescription("DB");
        b.setPosition(2);
        b.setCourse(new Course());

        when(courseLessonRepository.findAll()).thenReturn(List.of(a, b));

        // act
        List<CourseLessonDto> out = service.getAll();

        // assert
        assertEquals(2, out.size());
        assertEquals("A", out.get(0).getTitle());
        assertEquals("B", out.get(1).getTitle());
    }

    @Test
    void getAllPageable_shouldMapPageEntitiesToDtos() {
        // arrange
        Pageable pageable = PageRequest.of(0, 2);

        CourseLesson a = new CourseLesson();
        a.setId(UUID.randomUUID());
        a.setTitle("A");
        a.setDescription("DA");
        a.setPosition(1);
        a.setCourse(new Course());

        CourseLesson b = new CourseLesson();
        b.setId(UUID.randomUUID());
        b.setTitle("B");
        b.setDescription("DB");
        b.setPosition(2);
        b.setCourse(new Course());

        Page<CourseLesson> entityPage = new PageImpl<>(List.of(a, b), pageable, 2);
        when(courseLessonRepository.findAll(pageable)).thenReturn(entityPage);

        // act
        Page<CourseLessonDto> out = service.getAll(pageable);

        // assert
        assertEquals(2, out.getContent().size());
        assertEquals(2, out.getTotalElements());
        assertEquals("A", out.getContent().get(0).getTitle());
        assertEquals("B", out.getContent().get(1).getTitle());
    }

    @Test
    void deleteById_shouldDelete_whenExists() {
        // arrange
        when(courseLessonRepository.existsById(lessonId)).thenReturn(true);

        // act
        service.deleteById(lessonId);

        // assert
        verify(courseLessonRepository).deleteById(lessonId);
    }

    @Test
    void deleteById_shouldThrow_whenNotExists() {
        // arrange
        when(courseLessonRepository.existsById(lessonId)).thenReturn(false);

        // act + assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.deleteById(lessonId)
        );
        assertEquals("Course lesson not found", ex.getMessage());

        verify(courseLessonRepository, never()).deleteById(any());
    }

    @Test
    void getByCourse_shouldReturnDtosWithCourseIdSet() {
        // arrange
        CourseLesson a = new CourseLesson();
        a.setId(UUID.randomUUID());
        a.setTitle("A");
        a.setDescription("DA");
        a.setPosition(1);
        a.setCourse(new Course());

        CourseLesson b = new CourseLesson();
        b.setId(UUID.randomUUID());
        b.setTitle("B");
        b.setDescription("DB");
        b.setPosition(2);
        b.setCourse(new Course());

        when(courseLessonRepository.findByCourse(courseId)).thenReturn(List.of(a, b));

        // act
        List<CourseLessonDto> out = service.getByCourse(courseId);

        // assert
        assertEquals(2, out.size());
        assertTrue(out.stream().allMatch(d -> courseId.equals(d.getCourseId())),
                "Every returned DTO should have courseId set by service");
        verify(courseLessonRepository).findByCourse(courseId);
    }
}
