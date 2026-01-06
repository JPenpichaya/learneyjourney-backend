package com.ying.learneyjourney.service;
import com.ying.learneyjourney.dto.CourseVideoDto;
import com.ying.learneyjourney.entity.CourseLesson;
import com.ying.learneyjourney.entity.CourseVideo;
import com.ying.learneyjourney.repository.CourseLessonRepository;
import com.ying.learneyjourney.repository.CourseVideoRepository;
import com.ying.learneyjourney.service.CourseVideoService;
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
class CourseVideoServiceTest {

    @Mock private CourseVideoRepository courseVideoRepository;
    @Mock private CourseLessonRepository courseLessonRepository;

    @InjectMocks private CourseVideoService service;

    private UUID videoId;
    private UUID lessonId;

    @BeforeEach
    void setUp() {
        videoId = UUID.randomUUID();
        lessonId = UUID.randomUUID();
    }

    // -------------------- create --------------------

    @Test
    void create_shouldSetLessonAndSave_whenLessonExists() {
        // arrange
        CourseVideoDto dto = new CourseVideoDto();
        dto.setLessonId(lessonId);
        dto.setTitle("Video 1");
        dto.setUrl("http://x");
        dto.setDuration(120);
        dto.setPosition(1);

        CourseLesson lesson = new CourseLesson();
        lesson.setId(lessonId);

        when(courseLessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));

        when(courseVideoRepository.save(any(CourseVideo.class)))
                .thenAnswer(inv -> {
                    CourseVideo entity = inv.getArgument(0);
                    entity.setId(videoId); // simulate generated id
                    return entity;
                });

        // act
        CourseVideoDto out = service.create(dto);

        // assert
        assertNotNull(out);
        assertEquals(videoId, out.getId());

        ArgumentCaptor<CourseVideo> captor = ArgumentCaptor.forClass(CourseVideo.class);
        verify(courseVideoRepository).save(captor.capture());
        CourseVideo saved = captor.getValue();

        assertNotNull(saved.getCourseLesson(), "courseLesson should be set when lesson exists");
        assertEquals(lessonId, saved.getCourseLesson().getId());
        verify(courseLessonRepository).findById(lessonId);
    }

    @Test
    void create_shouldSaveWithoutLesson_whenLessonNotFound() {
        // arrange
        CourseVideoDto dto = new CourseVideoDto();
        dto.setLessonId(lessonId);

        when(courseLessonRepository.findById(lessonId)).thenReturn(Optional.empty());

        when(courseVideoRepository.save(any(CourseVideo.class)))
                .thenAnswer(inv -> {
                    CourseVideo entity = inv.getArgument(0);
                    entity.setId(videoId);
                    return entity;
                });

        // act
        CourseVideoDto out = service.create(dto);

        // assert
        assertEquals(videoId, out.getId());

        ArgumentCaptor<CourseVideo> captor = ArgumentCaptor.forClass(CourseVideo.class);
        verify(courseVideoRepository).save(captor.capture());
        CourseVideo saved = captor.getValue();

        assertNull(saved.getCourseLesson(), "courseLesson should remain null when lesson not found");
        verify(courseLessonRepository).findById(lessonId);
    }

    // -------------------- update --------------------

    @Test
    void update_shouldUpdateFieldsAndSave_whenVideoExists() {
        // arrange
        CourseVideo existing = new CourseVideo();
        existing.setId(videoId);
        existing.setTitle("Old");
        existing.setUrl("old");
        existing.setDuration(1);
        existing.setPosition(9);

        CourseVideoDto dto = new CourseVideoDto();
        dto.setTitle("New");
        dto.setUrl("new-url");
        dto.setDuration(300);
        dto.setPosition(2);

        when(courseVideoRepository.findById(videoId)).thenReturn(Optional.of(existing));

        // act
        CourseVideoDto out = service.update(videoId, dto);

        // assert
        assertSame(dto, out);

        assertEquals("New", existing.getTitle());
        assertEquals("new-url", existing.getUrl());
        assertEquals(300, existing.getDuration());
        assertEquals(2, existing.getPosition());

        verify(courseVideoRepository).save(existing);
    }

    @Test
    void update_shouldThrow_whenVideoNotFound() {
        // arrange
        CourseVideoDto dto = new CourseVideoDto();
        when(courseVideoRepository.findById(videoId)).thenReturn(Optional.empty());

        // act + assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.update(videoId, dto)
        );
        assertEquals("Course video not found", ex.getMessage());

        verify(courseVideoRepository, never()).save(any());
    }

    // -------------------- getById --------------------

    @Test
    void getById_shouldReturnDto_whenFound() {
        // arrange
        CourseVideo v = new CourseVideo();
        v.setId(videoId);
        v.setTitle("T");
        v.setUrl("U");
        v.setDuration(100);
        v.setPosition(1);

        when(courseVideoRepository.findById(videoId)).thenReturn(Optional.of(v));

        // act
        CourseVideoDto out = service.getById(videoId);

        // assert
        assertNotNull(out);
        assertEquals(videoId, out.getId());
        assertEquals("T", out.getTitle());
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        when(courseVideoRepository.findById(videoId)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.getById(videoId)
        );
        assertEquals("Course video not found", ex.getMessage());
    }

    // -------------------- getAll --------------------

    @Test
    void getAll_shouldMapEntitiesToDtos() {
        CourseVideo a = new CourseVideo();
        a.setId(UUID.randomUUID());
        a.setTitle("A");
        a.setPosition(1);

        CourseVideo b = new CourseVideo();
        b.setId(UUID.randomUUID());
        b.setTitle("B");
        b.setPosition(2);

        when(courseVideoRepository.findAll()).thenReturn(List.of(a, b));

        List<CourseVideoDto> out = service.getAll();

        assertEquals(2, out.size());
        assertEquals("A", out.get(0).getTitle());
        assertEquals("B", out.get(1).getTitle());
    }

    @Test
    void getAllPageable_shouldMapPageEntitiesToDtos() {
        Pageable pageable = PageRequest.of(0, 2);

        CourseVideo a = new CourseVideo();
        a.setId(UUID.randomUUID());
        a.setTitle("A");

        CourseVideo b = new CourseVideo();
        b.setId(UUID.randomUUID());
        b.setTitle("B");

        when(courseVideoRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(a, b), pageable, 2));

        Page<CourseVideoDto> out = service.getAll(pageable);

        assertEquals(2, out.getTotalElements());
        assertEquals(2, out.getContent().size());
        assertEquals("A", out.getContent().get(0).getTitle());
        assertEquals("B", out.getContent().get(1).getTitle());
    }

    // -------------------- deleteById --------------------

    @Test
    void deleteById_shouldDelete_whenExists() {
        when(courseVideoRepository.existsById(videoId)).thenReturn(true);

        service.deleteById(videoId);

        verify(courseVideoRepository).deleteById(videoId);
    }

    @Test
    void deleteById_shouldThrow_whenNotExists() {
        when(courseVideoRepository.existsById(videoId)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.deleteById(videoId)
        );
        assertEquals("Course video not found", ex.getMessage());

        verify(courseVideoRepository, never()).deleteById(any());
    }

    // -------------------- getByCourseLessonId --------------------

    @Test
    void getByCourseLessonId_shouldReturnDtosAndSetLessonIdOnEach() {
        // arrange
        CourseVideo a = new CourseVideo();
        a.setId(UUID.randomUUID());
        a.setTitle("A");

        CourseVideo b = new CourseVideo();
        b.setId(UUID.randomUUID());
        b.setTitle("B");

        when(courseVideoRepository.findByLessonId(lessonId)).thenReturn(List.of(a, b));

        // act
        List<CourseVideoDto> out = service.getByCourseLessonId(lessonId);

        // assert
        assertEquals(2, out.size());
        assertTrue(out.stream().allMatch(d -> lessonId.equals(d.getLessonId())),
                "Each DTO should have lessonId set by service.addCourseLessonId()");
        verify(courseVideoRepository).findByLessonId(lessonId);
    }
}
