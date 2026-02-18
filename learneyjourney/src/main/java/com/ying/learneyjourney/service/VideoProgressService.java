package com.ying.learneyjourney.service;

import com.ying.learneyjourney.constaint.EnumLessonProgressStatus;
import com.ying.learneyjourney.constaint.EnumVideoProgressStatus;
import com.ying.learneyjourney.dto.LessonProgressDto;
import com.ying.learneyjourney.dto.VideoProgressDto;
import com.ying.learneyjourney.entity.CourseVideo;
import com.ying.learneyjourney.entity.VideoProgress;
import com.ying.learneyjourney.master.MasterService;
import com.ying.learneyjourney.master.SearchCriteria;

import com.ying.learneyjourney.repository.CourseVideoRepository;
import com.ying.learneyjourney.repository.UserRepository;
import com.ying.learneyjourney.repository.VideoProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoProgressService implements MasterService<VideoProgressDto, UUID> {
    private final VideoProgressRepository videoProgressRepository;
    private final CourseVideoRepository courseVideoRepository;
    private final UserRepository userRepository;
    private final LessonProgressService lessonProgressService;

    @Override
    public VideoProgressDto create(VideoProgressDto dto) {
        VideoProgress entity = VideoProgressDto.toEntity(dto);
        userRepository.findById(dto.getUserId()).ifPresent(entity::setUser);
        courseVideoRepository.findById(dto.getCourseVideoId()).ifPresent(entity::setCourseVideo);
        videoProgressRepository.save(entity);
        dto.setId(entity.getId());
        return dto;
    }

    @Override
    public VideoProgressDto update(UUID uuid, VideoProgressDto dto) {
        VideoProgress videoProgress = videoProgressRepository.findById(uuid).orElseThrow(() -> new RuntimeException("VideoProgress not found"));
        videoProgress.setWatchedSeconds(dto.getWatchedSeconds());
        videoProgress.setStatus(dto.getStatus());
        videoProgress.setLastWatchedAt(dto.getLastWatchedAt());
        videoProgressRepository.save(videoProgress);
        return dto;
    }

    @Override
    public VideoProgressDto getById(UUID uuid) {
        return videoProgressRepository.findById(uuid).map(VideoProgressDto::from).orElseThrow(() -> new RuntimeException("VideoProgress not found"));
    }

    @Override
    public List<VideoProgressDto> getAll() {
        return videoProgressRepository.findAll().stream().map(VideoProgressDto::from).toList();
    }

    @Override
    public Page<VideoProgressDto> getAll(Pageable pageable) {
        return videoProgressRepository.findAll(pageable).map(VideoProgressDto::from);
    }

    @Override
    public void deleteById(UUID uuid) {
        if(!videoProgressRepository.existsById(uuid)) {
            throw new RuntimeException("VideoProgress not found");
        }
        videoProgressRepository.deleteById(uuid);
    }

    public List<VideoProgressDto> getByLessonId(String userId, UUID lessonId) {
        List<CourseVideo> videos = courseVideoRepository.findByLessonId(lessonId);
        List<UUID> ids = videos.stream().map(CourseVideo::getId).toList();
        List<VideoProgress> videoProgressList = videoProgressRepository.findByUserIdAndVideoIdIn(userId, ids);
        return videoProgressList.stream().map(this::convertToDto).toList();
    }

    private VideoProgressDto convertToDto(VideoProgress videoProgress){
        VideoProgressDto from = VideoProgressDto.from(videoProgress);
        from.setCourseVideoId(videoProgress.getCourseVideo().getId());
        from.setUserId(videoProgress.getUser().getId());
        return from;
    }

    public void setVideoProgressAllNotStart(String userId, UUID courseId){
        videoProgressRepository.insertVideoProgressForCourse(userId, courseId);
    }

    public void setVideoProgressCompleted(UUID progressId){
        VideoProgress videoProgress = videoProgressRepository.findById(progressId).orElseThrow(() -> new RuntimeException("VideoProgress not found"));
        videoProgress.setStatus(EnumVideoProgressStatus.COMPLETED);
        videoProgressRepository.save(videoProgress);
        if(videoProgressRepository.areAllVideosInLessonCompleted(videoProgress.getUser().getId(), videoProgress.getCourseVideo().getCourseLesson().getId())){
            LessonProgressDto lesson = new LessonProgressDto();
            lesson.setStatus(EnumLessonProgressStatus.COMPLETED);
            lesson.setCompletedAt(LocalDateTime.now());
            lessonProgressService.update(videoProgress.getCourseVideo().getCourseLesson().getId(), lesson);
        }
    }
}
