package com.ying.learneyjourney.service;
import com.ying.learneyjourney.dto.VideoProgressDto;
import com.ying.learneyjourney.entity.CourseVideo;
import com.ying.learneyjourney.entity.User;
import com.ying.learneyjourney.entity.VideoProgress;
import com.ying.learneyjourney.repository.CourseVideoRepository;
import com.ying.learneyjourney.repository.UserRepository;
import com.ying.learneyjourney.repository.VideoProgressRepository;
import com.ying.learneyjourney.service.VideoProgressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoProgressServiceTest {

    @Mock private VideoProgressRepository videoProgressRepository;
    @Mock private CourseVideoRepository courseVideoRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks
    private VideoProgressService service;

    private UUID progressId;
    private UUID videoId;
    private UUID lessonId;
    private String userId;

    @BeforeEach
    void setUp() {
        progressId = UUID.randomUUID();
        videoId = UUID.randomUUID();
        lessonId = UUID.randomUUID();
        userId = "user-1";
    }

    // -------------------- create --------------------

    @Test
    void create_shouldSetUserAndVideo_andSave_whenFound() {
        // arrange
        VideoProgressDto dto = new VideoProgressDto();
        dto.setUserId(userId);
        dto.setCourseVideoId(videoId);

        User user = new User();
        user.setId(userId);

        CourseVideo video = new CourseVideo();
        video.setId(videoId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(courseVideoRepository.findById(videoId)).thenReturn(Optional.of(video));

        when(videoProgressRepository.save(any(VideoProgress.class))).thenAnswer(inv -> {
            VideoProgress vp = inv.getArgument(0);
            vp.setId(progressId); // simulate generated id
            return vp;
        });

        ArgumentCaptor<VideoProgress> captor = ArgumentCaptor.forClass(VideoProgress.class);

        // act
        VideoProgressDto out = service.create(dto);

        // assert
        verify(videoProgressRepository).save(captor.capture());
        VideoProgress saved = captor.getValue();

        assertNotNull(saved.getUser());
        assertEquals(userId, saved.getUser().getId());

        assertNotNull(saved.getCourseVideo());
        assertEquals(videoId, saved.getCourseVideo().getId());

        assertEquals(progressId, out.getId());
    }

    // -------------------- update --------------------

    @Test
    void update_shouldUpdateFields_andSave() {
        // arrange
        VideoProgress existing = new VideoProgress();
        existing.setId(progressId);
        existing.setWatchedSeconds(10);

        when(videoProgressRepository.findById(progressId)).thenReturn(Optional.of(existing));
        when(videoProgressRepository.save(any(VideoProgress.class))).thenAnswer(inv -> inv.getArgument(0));

        VideoProgressDto dto = new VideoProgressDto();
        dto.setWatchedSeconds(99);
        dto.setStatus(existing.getStatus()); // or set your enum status if you have it
        LocalDateTime now = LocalDateTime.now();
        dto.setLastWatchedAt(now);

        // act
        VideoProgressDto out = service.update(progressId, dto);

        // assert
        verify(videoProgressRepository).save(existing);
        assertEquals(99, existing.getWatchedSeconds());
        assertEquals(now, existing.getLastWatchedAt());
        assertSame(dto, out); // service returns dto
    }

    @Test
    void update_shouldThrow_whenNotFound() {
        when(videoProgressRepository.findById(progressId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> service.update(progressId, new VideoProgressDto())
        );

        assertEquals("VideoProgress not found", ex.getMessage());
        verify(videoProgressRepository, never()).save(any());
    }

    // -------------------- getById --------------------

    @Test
    void getById_shouldReturnDto_whenFound() {
        VideoProgress vp = new VideoProgress();
        vp.setId(progressId);
        vp.setCourseVideo(new CourseVideo()); vp.setUser(new User());

        when(videoProgressRepository.findById(progressId)).thenReturn(Optional.of(vp));

        VideoProgressDto out = service.getById(progressId);

        assertNotNull(out);
        assertEquals(progressId, out.getId());
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        when(videoProgressRepository.findById(progressId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> service.getById(progressId)
        );

        assertEquals("VideoProgress not found", ex.getMessage());
    }

    // -------------------- getAll --------------------

    @Test
    void getAll_shouldMapAll() {
        VideoProgress v1 = new VideoProgress(); v1.setId(UUID.randomUUID());v1.setCourseVideo(new CourseVideo()); v1.setUser(new User());
        VideoProgress v2 = new VideoProgress(); v2.setId(UUID.randomUUID());v2.setCourseVideo(new CourseVideo()); v2.setUser(new User());

        when(videoProgressRepository.findAll()).thenReturn(List.of(v1, v2));

        List<VideoProgressDto> out = service.getAll();

        assertEquals(2, out.size());
        assertEquals(v1.getId(), out.get(0).getId());
        assertEquals(v2.getId(), out.get(1).getId());
    }

    // -------------------- getAll(Pageable) --------------------

    @Test
    void getAllPage_shouldMapPage() {
        Pageable pageable = PageRequest.of(0, 10);

        VideoProgress v1 = new VideoProgress(); v1.setId(UUID.randomUUID()); v1.setCourseVideo(new CourseVideo()); v1.setUser(new User());
        Page<VideoProgress> page = new PageImpl<>(List.of(v1), pageable, 1);

        when(videoProgressRepository.findAll(pageable)).thenReturn(page);

        Page<VideoProgressDto> out = service.getAll(pageable);

        assertEquals(1, out.getTotalElements());
        assertEquals(v1.getId(), out.getContent().get(0).getId());
    }

    // -------------------- deleteById --------------------

    @Test
    void deleteById_shouldDelete_whenExists() {
        when(videoProgressRepository.existsById(progressId)).thenReturn(true);

        service.deleteById(progressId);

        verify(videoProgressRepository).deleteById(progressId);
    }

    @Test
    void deleteById_shouldThrow_whenNotExists() {
        when(videoProgressRepository.existsById(progressId)).thenReturn(false);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> service.deleteById(progressId)
        );

        assertEquals("VideoProgress not found", ex.getMessage());
        verify(videoProgressRepository, never()).deleteById(any());
    }

    // -------------------- getByLessonId --------------------

    @Test
    void getByLessonId_shouldFetchVideos_thenProgress_thenMapCourseVideoIdAndUserId() {
        // arrange videos under lesson
        CourseVideo v1 = new CourseVideo(); v1.setId(UUID.randomUUID());
        CourseVideo v2 = new CourseVideo(); v2.setId(UUID.randomUUID());
        when(courseVideoRepository.findByLessonId(lessonId)).thenReturn(List.of(v1, v2));

        // arrange progress rows
        User user = new User(); user.setId(userId);

        VideoProgress p1 = new VideoProgress();
        p1.setId(UUID.randomUUID());
        p1.setUser(user);
        p1.setCourseVideo(v1);

        VideoProgress p2 = new VideoProgress();
        p2.setId(UUID.randomUUID());
        p2.setUser(user);
        p2.setCourseVideo(v2);

        when(videoProgressRepository.findByUserIdAndVideoIdIn(eq(userId), anyList()))
                .thenReturn(List.of(p1, p2));

        // act
        List<VideoProgressDto> out = service.getByLessonId(userId, lessonId);

        // assert
        assertEquals(2, out.size());

        // convertToDto should set these fields
        assertEquals(userId, out.get(0).getUserId());
        assertNotNull(out.get(0).getCourseVideoId());

        assertEquals(userId, out.get(1).getUserId());
        assertNotNull(out.get(1).getCourseVideoId());

        verify(courseVideoRepository).findByLessonId(lessonId);
        verify(videoProgressRepository).findByUserIdAndVideoIdIn(eq(userId), anyList());
    }
}
