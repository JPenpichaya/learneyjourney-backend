package com.ying.learneyjourney.service;

import com.ying.learneyjourney.criteria.StudentNoteCriteria;
import com.ying.learneyjourney.dto.StudentNoteDto;
import com.ying.learneyjourney.entity.CourseVideo;
import com.ying.learneyjourney.entity.StudentNote;
import com.ying.learneyjourney.entity.User;
import com.ying.learneyjourney.master.PageCriteria;
import com.ying.learneyjourney.repository.CourseVideoRepository;
import com.ying.learneyjourney.repository.StudentNoteRepository;
import com.ying.learneyjourney.repository.UserRepository;
import com.ying.learneyjourney.service.StudentNoteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentNoteServiceTest {

    @Mock private StudentNoteRepository studentNoteRepository;
    @Mock private UserRepository userRepository;
    @Mock private CourseVideoRepository courseVideoRepository;

    @InjectMocks
    private StudentNoteService service;

    private UUID noteId;
    private UUID videoId;
    private String userId;

    @BeforeEach
    void setUp() {
        noteId = UUID.randomUUID();
        videoId = UUID.randomUUID();
        userId = "user-1";
    }

    // -------------------- create --------------------

    @Test
    void create_shouldSetUserAndVideoAndSave_whenFound() {
        // arrange
        StudentNoteDto dto = new StudentNoteDto();
        dto.setUserId(userId);
        dto.setVideoId(videoId);
        dto.setContent("hello");

        User user = new User();
        user.setId(userId);

        CourseVideo video = new CourseVideo();
        video.setId(videoId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(courseVideoRepository.findById(videoId)).thenReturn(Optional.of(video));

        when(studentNoteRepository.save(any(StudentNote.class))).thenAnswer(inv -> {
            StudentNote n = inv.getArgument(0);
            // simulate JPA id generation
            n.setId(noteId);
            return n;
        });

        ArgumentCaptor<StudentNote> captor = ArgumentCaptor.forClass(StudentNote.class);

        // act
        StudentNoteDto out = service.create(dto);

        // assert
        verify(studentNoteRepository).save(captor.capture());
        StudentNote saved = captor.getValue();

        assertNotNull(saved.getUser());
        assertEquals(userId, saved.getUser().getId());

        assertNotNull(saved.getCourseVideo());
        assertEquals(videoId, saved.getCourseVideo().getId());

        assertEquals(noteId, out.getId()); // dto.setId(entity.getId())
    }

    // -------------------- update --------------------

    @Test
    void update_shouldUpdateFields_andSave() {
        LocalDateTime videoAt = LocalDateTime.now();
        LocalDateTime newVideoAt = videoAt.plusMinutes(5);
        // arrange
        StudentNote existing = new StudentNote();
        existing.setId(noteId);
        existing.setContent("old");
        existing.setImageUrl("old.png");
        existing.setVideoAt(videoAt);

        when(studentNoteRepository.findById(noteId)).thenReturn(Optional.of(existing));
        when(studentNoteRepository.save(any(StudentNote.class))).thenAnswer(inv -> inv.getArgument(0));

        StudentNoteDto dto = new StudentNoteDto();
        dto.setContent("new");
        dto.setImageUrl("new.png");
        dto.setVideoAt(newVideoAt);

        // act
        StudentNoteDto out = service.update(noteId, dto);

        // assert
        verify(studentNoteRepository).save(existing);
        assertEquals("new", existing.getContent());
        assertEquals("new.png", existing.getImageUrl());
        assertEquals(newVideoAt, existing.getVideoAt());

        // service returns dto directly
        assertSame(dto, out);
    }

    @Test
    void update_shouldThrow_whenNotFound() {
        when(studentNoteRepository.findById(noteId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.update(noteId, new StudentNoteDto())
        );

        assertEquals("Student Note not found", ex.getMessage());
        verify(studentNoteRepository, never()).save(any());
    }

    // -------------------- getById --------------------

    @Test
    void getById_shouldReturnDto_whenFound() {
        StudentNote note = new StudentNote();
        note.setId(noteId);
        note.setUser(new User());
        note.setCourseVideo(new CourseVideo());

        when(studentNoteRepository.findById(noteId)).thenReturn(Optional.of(note));

        StudentNoteDto out = service.getById(noteId);

        assertNotNull(out);
        assertEquals(noteId, out.getId());
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        when(studentNoteRepository.findById(noteId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.getById(noteId)
        );

        assertEquals("Student Note not found", ex.getMessage());
    }

    // -------------------- getAll --------------------

    @Test
    void getAll_shouldMapAllNotes() {
        StudentNote n1 = new StudentNote(); n1.setId(UUID.randomUUID()); n1.setCourseVideo(new CourseVideo()); n1.setUser(new User());
        StudentNote n2 = new StudentNote(); n2.setId(UUID.randomUUID()); n2.setCourseVideo(new CourseVideo()); n2.setUser(new User());

        when(studentNoteRepository.findAll()).thenReturn(List.of(n1, n2));

        List<StudentNoteDto> out = service.getAll();

        assertEquals(2, out.size());
        assertEquals(n1.getId(), out.get(0).getId());
        assertEquals(n2.getId(), out.get(1).getId());
    }

    // -------------------- getAll(Pageable) --------------------

    @Test
    void getAllPage_shouldMapPage() {
        Pageable pageable = PageRequest.of(0, 10);

        StudentNote n1 = new StudentNote(); n1.setId(UUID.randomUUID()); n1.setUser(new User()); n1.setCourseVideo(new CourseVideo());
        Page<StudentNote> page = new PageImpl<>(List.of(n1), pageable, 1);

        when(studentNoteRepository.findAll(pageable)).thenReturn(page);

        Page<StudentNoteDto> out = service.getAll(pageable);

        assertEquals(1, out.getTotalElements());
        assertEquals(n1.getId(), out.getContent().get(0).getId());
    }

    // -------------------- deleteById --------------------

    @Test
    void deleteById_shouldDelete_whenExists() {
        when(studentNoteRepository.existsById(noteId)).thenReturn(true);

        service.deleteById(noteId);

        verify(studentNoteRepository).deleteById(noteId);
    }

    @Test
    void deleteById_shouldThrow_whenNotExists() {
        when(studentNoteRepository.existsById(noteId)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.deleteById(noteId)
        );

        assertEquals("Student Note not found", ex.getMessage());
        verify(studentNoteRepository, never()).deleteById(any());
    }

    // -------------------- getAllList(PageCriteria) --------------------

    @Test
    void getAllList_shouldMapAndSetVideoId_fromCourseVideo() {
        // arrange: note must have courseVideo to avoid NPE in convertToDto
        CourseVideo video = new CourseVideo();
        video.setId(videoId);

        StudentNote note = new StudentNote();
        note.setId(noteId);
        note.setCourseVideo(video);
        note.setUser(new User());

        @SuppressWarnings("unchecked")
        PageCriteria<StudentNoteCriteria> conditions = mock(PageCriteria.class);

        // service calls: studentNoteRepository.findAll(conditions.getSpecification())
        when(conditions.getSpecification()).thenReturn(null);
        when(studentNoteRepository.findAll(
                ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<StudentNote>>any()
        )).thenReturn(List.of(note));

        // act
        List<StudentNoteDto> out = service.getAllList(conditions);

        // assert
        assertEquals(1, out.size());
        assertEquals(noteId, out.get(0).getId());
        assertEquals(videoId, out.get(0).getVideoId()); // set by convertToDto
    }
}
