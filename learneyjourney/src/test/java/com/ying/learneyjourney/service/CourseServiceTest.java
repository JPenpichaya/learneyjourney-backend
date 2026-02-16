package com.ying.learneyjourney.service;

import com.ying.learneyjourney.dto.CourseDetailDto;
import com.ying.learneyjourney.dto.CourseDto;
import com.ying.learneyjourney.dto.response.CourseInfoResponse;
import com.ying.learneyjourney.entity.Course;
import com.ying.learneyjourney.entity.Enrollment;
import com.ying.learneyjourney.entity.TutorProfile;
import com.ying.learneyjourney.master.BusinessException;
import com.ying.learneyjourney.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock private CourseRepository courseRepository;
    @Mock private TutorProfileRepository tutorProfileRepository;
    @Mock private CourseReviewRepository courseReviewRepository;
    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private CourseLessonRepository courseLessonRepository;
    @Mock private CourseVideoRepository courseVideoRepository;

    @InjectMocks private CourseService service;

    private UUID courseId;
    private UUID tutorProfileId;

    @BeforeEach
    void setUp() {
        courseId = UUID.randomUUID();
        tutorProfileId = UUID.randomUUID();
    }

    // -------------------- create --------------------

    @Test
    void create_shouldSaveAndSetId_whenTutorProfileExists() {
        // arrange
        CourseDto dto = new CourseDto();
        dto.setTutorProfileId(tutorProfileId);
        dto.setTitle("Java");
        dto.setDescription("Basics");

        TutorProfile profile = new TutorProfile();
        profile.setId(tutorProfileId);

        when(tutorProfileRepository.findById(tutorProfileId)).thenReturn(Optional.of(profile));

        when(courseRepository.save(any(Course.class)))
                .thenAnswer(inv -> {
                    Course e = inv.getArgument(0);
                    e.setId(courseId); // simulate JPA generated id
                    return e;
                });

        // act
        CourseDto out = service.create(dto);

        // assert
        assertNotNull(out);
        assertEquals(courseId, out.getId());
        verify(tutorProfileRepository).findById(tutorProfileId);
        verify(courseRepository).save(any(Course.class));
    }

    @Test
    void create_shouldThrow_whenTutorProfileNotFound() {
        CourseDto dto = new CourseDto();
        dto.setTutorProfileId(tutorProfileId);

        when(tutorProfileRepository.findById(tutorProfileId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.create(dto)
        );
        assertEquals("Tutor Profile not found", ex.getMessage());
        verify(courseRepository, never()).save(any());
    }

    // -------------------- update --------------------

    @Test
    void update_shouldUpdateTitleAndDescriptionAndSave_whenCourseExists() {
        // arrange
        Course existing = new Course();
        existing.setId(courseId);
        existing.setTitle("Old");
        existing.setDescription("OldD");

        CourseDto dto = new CourseDto();
        dto.setTitle("New");
        dto.setDescription("NewD");

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(existing));

        // act
        CourseDto out = service.update(courseId, dto);

        // assert
        assertSame(dto, out);
        assertEquals("New", existing.getTitle());
        assertEquals("NewD", existing.getDescription());
        verify(courseRepository).save(existing);
    }

    @Test
    void update_shouldThrow_whenCourseNotFound() {
        CourseDto dto = new CourseDto();
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.update(courseId, dto)
        );
        assertEquals("Course not found", ex.getMessage());
        verify(courseRepository, never()).save(any());
    }

    // -------------------- getById --------------------

    @Test
    void getById_shouldReturnDto_whenCourseExists() {
        Course course = new Course();
        course.setId(courseId);
        course.setTitle("T");
        course.setDescription("D");
        course.setTutorProfile(new TutorProfile());

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        CourseDto out = service.getById(courseId);

        assertNotNull(out);
        assertEquals(courseId, out.getId());
    }

    @Test
    void getById_shouldThrow_whenCourseNotFound() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.getById(courseId)
        );
        assertEquals("Course not found", ex.getMessage());
    }

    // -------------------- getAll --------------------

    @Test
    void getAll_shouldMapEntitiesToDtos() {
        Course a = new Course(); a.setId(UUID.randomUUID()); a.setTitle("A");a.setTutorProfile(new TutorProfile());
        Course b = new Course(); b.setId(UUID.randomUUID()); b.setTitle("B");b.setTutorProfile(new TutorProfile());

        when(courseRepository.findAll()).thenReturn(List.of(a, b));

        List<CourseDto> out = service.getAll();

        assertEquals(2, out.size());
        assertEquals("A", out.get(0).getTitle());
        assertEquals("B", out.get(1).getTitle());
    }

    @Test
    void getAllPageable_shouldMapPageEntitiesToDtos() {
        Pageable pageable = PageRequest.of(0, 2);

        Course a = new Course(); a.setId(UUID.randomUUID()); a.setTitle("A"); a.setTutorProfile(new TutorProfile());
        Course b = new Course(); b.setId(UUID.randomUUID()); b.setTitle("B"); b.setTutorProfile(new TutorProfile());

        when(courseRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(a, b), pageable, 2));

        Page<CourseDto> out = service.getAll(pageable);

        assertEquals(2, out.getTotalElements());
        assertEquals(2, out.getContent().size());
        assertEquals("A", out.getContent().get(0).getTitle());
        assertEquals("B", out.getContent().get(1).getTitle());
    }

    // -------------------- deleteById --------------------

    @Test
    void deleteById_shouldDelete_whenExists() {
        when(courseRepository.existsById(courseId)).thenReturn(true);

        service.deleteById(courseId);

        verify(courseRepository).deleteById(courseId);
    }

    @Test
    void deleteById_shouldThrowBusinessException_whenNotExists() {
        when(courseRepository.existsById(courseId)).thenReturn(false);

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> service.deleteById(courseId)
        );

        // message/code based on your constructor call
        assertEquals("Course not found", ex.getMessage());
        // if your BusinessException exposes code getter, assert it too:
        // assertEquals("COURSE_NOT_FOUND", ex.getCode());

        verify(courseRepository, never()).deleteById(any());
    }

    // -------------------- getCourseDetailById --------------------

    @Test
    @Disabled
    void getCourseDetailById_shouldReturnDetail_withDurationRow() {
        // arrange
        Course course = new Course();
        course.setId(courseId);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseReviewRepository.getAverageRatingByCourseId(courseId)).thenReturn(4.5);
        when(enrollmentRepository.countByCourseId(courseId)).thenReturn(12L);
        when(courseLessonRepository.countByCourseId(courseId)).thenReturn(7L);

        CourseVideoRepository.DurationDisplayRow row = mock(CourseVideoRepository.DurationDisplayRow.class);
        when(row.getDisplayValue()).thenReturn(2.0);
        when(row.getDisplayUnit()).thenReturn("hrs");
        when(courseVideoRepository.getCourseDurationDisplay(courseId)).thenReturn(row);

        // act
        CourseInfoResponse out = service.getCourseDetailById(courseId);

        // assert
        assertNotNull(out);
        assertEquals(courseId, out.getId());
        assertEquals(12L, out.getStudents());
        assertEquals(7L, out.getLessons());
        assertEquals(4.5, out.getRate());
        assertEquals("2.0 hrs", out.getDuration());
    }

    @Test
    void getCourseDetailById_shouldReturnZeroMins_whenDurationRowNull() {
        Course course = new Course();
        course.setId(courseId);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
        when(courseReviewRepository.getAverageRatingByCourseId(courseId)).thenReturn(null);
        when(enrollmentRepository.countByCourseId(courseId)).thenReturn(0L);
        when(courseLessonRepository.countByCourseId(courseId)).thenReturn(0L);
        when(courseVideoRepository.getCourseDurationDisplay(courseId)).thenReturn(null);

        CourseInfoResponse out = service.getCourseDetailById(courseId);

        assertEquals("0 mins", out.getDuration());
    }

    @Test
    void getCourseDetailById_shouldThrow_whenCourseNotFound() {
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.getCourseDetailById(courseId)
        );
        assertEquals("Course not found", ex.getMessage());
    }

    // -------------------- getEnrolledCouresByUserId --------------------

    @Test
    void getEnrolledCouresByUserId_shouldReturnCoursesMappedFromEnrollmentCourseIds() {
        // arrange
        String userId = "u1";

        UUID c1 = UUID.randomUUID();
        UUID c2 = UUID.randomUUID();

        Course course1 = new Course(); course1.setId(c1); course1.setTitle("C1");course1.setTutorProfile(new TutorProfile());
        Course course2 = new Course(); course2.setId(c2); course2.setTitle("C2");course2.setTutorProfile(new TutorProfile());

        Enrollment e1 = new Enrollment();
        e1.setCourse(course1);
        Enrollment e2 = new Enrollment();
        e2.setCourse(course2);

        when(enrollmentRepository.findBy_UserId(userId)).thenReturn(List.of(e1, e2));
        when(courseRepository.findByIn_courseId(List.of(c1, c2))).thenReturn(List.of(course1, course2));

        // act
        Page<CourseDto> out = service.getEnrolledCouresByUserId(any(), userId);

        // assert
        assertEquals(2, out.getTotalElements());
        assertEquals(Set.of("C1", "C2"), Set.copyOf(out.stream().map(CourseDto::getTitle).toList()));
        verify(courseRepository).findByIn_courseId(List.of(c1, c2));
    }
}
