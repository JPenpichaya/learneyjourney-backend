package com.ying.learneyjourney.service;
import com.ying.learneyjourney.Util.FirebaseAuthUtil;
import com.ying.learneyjourney.dto.EnrollmentDto;
import com.ying.learneyjourney.entity.Course;
import com.ying.learneyjourney.entity.Enrollment;
import com.ying.learneyjourney.entity.User;
import com.ying.learneyjourney.repository.CourseRepository;
import com.ying.learneyjourney.repository.EnrollmentRepository;
import com.ying.learneyjourney.repository.UserRepository;
import com.ying.learneyjourney.service.EnrollmentService;
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
class EnrollmentServiceTest {

    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private UserRepository userRepository;
    @Mock private FirebaseAuthUtil firebaseAuthUtil;

    @InjectMocks private EnrollmentService service;

    private UUID enrollmentId;
    private UUID courseId;
    private String userId;

    @BeforeEach
    void setUp() {
        enrollmentId = UUID.randomUUID();
        courseId = UUID.randomUUID();
        userId = "user-1";
    }

    // -------------------- create --------------------

    @Test
    void create_shouldSetUserAndCourseAndSave_andReturnDtoWithIds() {
        // arrange
        EnrollmentDto dto = new EnrollmentDto();
        dto.setUserId(userId);
        dto.setCourseId(courseId);

        User user = new User();
        user.setId(userId);

        Course course = new Course();
        course.setId(courseId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        when(enrollmentRepository.save(any(Enrollment.class)))
                .thenAnswer(inv -> {
                    Enrollment e = inv.getArgument(0);
                    e.setId(enrollmentId); // simulate generated id
                    return e;
                });

        // act
        EnrollmentDto out = service.create(dto);

        // assert
        assertNotNull(out);
        assertEquals(enrollmentId, out.getId());
        assertEquals(userId, out.getUserId());
        assertEquals(courseId, out.getCourseId());

        ArgumentCaptor<Enrollment> captor = ArgumentCaptor.forClass(Enrollment.class);
        verify(enrollmentRepository).save(captor.capture());
        Enrollment saved = captor.getValue();

        assertNotNull(saved.getUser());
        assertEquals(userId, saved.getUser().getId());
        assertNotNull(saved.getCourse());
        assertEquals(courseId, saved.getCourse().getId());
    }

    @Test
    void create_shouldThrow_whenUserNotFound() {
        EnrollmentDto dto = new EnrollmentDto();
        dto.setUserId(userId);
        dto.setCourseId(courseId);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.create(dto)
        );
        assertEquals("User not found", ex.getMessage());

        verify(enrollmentRepository, never()).save(any());
        verify(courseRepository, never()).findById(any());
    }

    @Test
    void create_shouldThrow_whenCourseNotFound() {
        EnrollmentDto dto = new EnrollmentDto();
        dto.setUserId(userId);
        dto.setCourseId(courseId);

        User user = new User();
        user.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.create(dto)
        );
        assertEquals("Course not found", ex.getMessage());

        verify(enrollmentRepository, never()).save(any());
    }

    // -------------------- update --------------------

    @Test
    void update_shouldUpdateFieldsAndSave_andCopyValuesBackToDto() {
        // arrange
        Enrollment existing = new Enrollment();
        existing.setId(enrollmentId);
        LocalDateTime completedAt = LocalDateTime.now();
        LocalDateTime lastAccessedAt = LocalDateTime.now();

        EnrollmentDto dto = new EnrollmentDto();
        // Use values matching your DTO types (enums/dates/etc.) in your project
        dto.setStatus(existing.getStatus()); // if enum exists, you can set a real value
        dto.setProgress(50);
        dto.setCompletionAt(completedAt);
        dto.setLastAccessedAt(lastAccessedAt);

        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(existing));

        // act
        EnrollmentDto out = service.update(enrollmentId, dto);

        // assert
        assertNotNull(out);
        verify(enrollmentRepository).save(existing);

        assertEquals(dto.getStatus(), out.getStatus());
        assertEquals(50, out.getProgress());
        assertEquals(completedAt, out.getCompletionAt());
        assertEquals(lastAccessedAt, out.getLastAccessedAt());
    }

    @Test
    void update_shouldThrow_whenEnrollmentNotFound() {
        EnrollmentDto dto = new EnrollmentDto();
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.update(enrollmentId, dto)
        );
        assertEquals("Enrollment not found", ex.getMessage());

        verify(enrollmentRepository, never()).save(any());
    }

    // -------------------- getById --------------------

    @Test
    void getById_shouldReturnDto_whenFound() {
        Enrollment existing = new Enrollment();
        existing.setId(enrollmentId);
        existing.setUser(new User());
        existing.setCourse(new Course());

        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(existing));

        EnrollmentDto out = service.getById(enrollmentId);

        assertNotNull(out);
        assertEquals(enrollmentId, out.getId());
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.getById(enrollmentId)
        );
        assertEquals("Enrollment not found", ex.getMessage());
    }

    // -------------------- getAll --------------------

    @Test
    void getAll_shouldMapEntitiesToDtos() {
        Enrollment a = new Enrollment(); a.setId(UUID.randomUUID()); a.setUser(new User()); a.setCourse(new Course());
        Enrollment b = new Enrollment(); b.setId(UUID.randomUUID()); b.setUser(new User()); b.setCourse(new Course());

        when(enrollmentRepository.findAll()).thenReturn(List.of(a, b));

        List<EnrollmentDto> out = service.getAll();

        assertEquals(2, out.size());
        assertNotNull(out.get(0).getId());
        assertNotNull(out.get(1).getId());
    }

    @Test
    void getAllPageable_shouldMapPageEntitiesToDtos() {
        Pageable pageable = PageRequest.of(0, 2);

        Enrollment a = new Enrollment(); a.setId(UUID.randomUUID()); a.setUser(new User()); a.setCourse(new Course());
        Enrollment b = new Enrollment(); b.setId(UUID.randomUUID()); b.setUser(new User()); b.setCourse(new Course());

        when(enrollmentRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(a, b), pageable, 2));

        Page<EnrollmentDto> out = service.getAll(pageable);

        assertEquals(2, out.getTotalElements());
        assertEquals(2, out.getContent().size());
    }

    // -------------------- deleteById --------------------

    @Test
    void deleteById_shouldDelete_whenExists() {
        when(enrollmentRepository.existsById(enrollmentId)).thenReturn(true);

        service.deleteById(enrollmentId);

        verify(enrollmentRepository).deleteById(enrollmentId);
    }

    @Test
    void deleteById_shouldThrow_whenNotExists() {
        when(enrollmentRepository.existsById(enrollmentId)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.deleteById(enrollmentId)
        );
        assertEquals("Enrollment not found", ex.getMessage());

        verify(enrollmentRepository, never()).deleteById(any());
    }

    // -------------------- getEnrollmentsByUserId --------------------

    @Test
    void getEnrollmentsByUserId_shouldUseFirebaseToGetUserId_andReturnEnrollments() throws Exception {
        // arrange
        String token = "token123";
        when(firebaseAuthUtil.getUserIdFromToken(token)).thenReturn(userId);

        Enrollment e1 = new Enrollment(); e1.setId(UUID.randomUUID()); e1.setUser(new User()); e1.setCourse(new Course());
        Enrollment e2 = new Enrollment(); e2.setId(UUID.randomUUID()); e2.setUser(new User()); e2.setCourse(new Course());

        when(enrollmentRepository.findBy_UserId(userId)).thenReturn(List.of(e1, e2));

        // act
        List<EnrollmentDto> out = service.getEnrollmentsByUserId(token);

        // assert
        assertEquals(2, out.size());
        verify(firebaseAuthUtil).getUserIdFromToken(token);
        verify(enrollmentRepository).findBy_UserId(userId);
    }

    @Test
    void getEnrollmentsByUserId_shouldPropagateException_whenFirebaseFails() throws Exception {
        String token = "bad";
        when(firebaseAuthUtil.getUserIdFromToken(token)).thenThrow(new Exception("Invalid token"));

        Exception ex = assertThrows(Exception.class, () -> service.getEnrollmentsByUserId(token));
        assertEquals("Invalid token", ex.getMessage());

        verify(enrollmentRepository, never()).findBy_UserId(any());
    }
}
