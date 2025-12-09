package com.ying.learneyjourney.service;

import com.ying.learneyjourney.dto.CourseVideoDto;
import com.ying.learneyjourney.entity.CourseVideo;
import com.ying.learneyjourney.master.MasterService;
import com.ying.learneyjourney.master.SearchCriteria;

import com.ying.learneyjourney.repository.CourseVideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseVideoService implements MasterService<CourseVideoDto, UUID> {
    private final CourseVideoRepository courseVideoRepository;
    @Override
    public CourseVideoDto create(CourseVideoDto dto) {
        CourseVideo entity = CourseVideoDto.toEntity(dto);
        courseVideoRepository.save(entity);
        dto.setId(entity.getId());
        return dto;
    }

    @Override
    public CourseVideoDto update(UUID uuid, CourseVideoDto dto) {
        CourseVideo courseVideo = courseVideoRepository.findById(uuid).orElseThrow(() -> new IllegalArgumentException("Course video not found"));
        courseVideo.setTitle(dto.getTitle());
        courseVideo.setUrl(dto.getUrl());
        courseVideo.setDuration(dto.getDuration());
        courseVideo.setPosition(dto.getPosition());
        courseVideoRepository.save(courseVideo);
        return dto;
    }

    @Override
    public CourseVideoDto getById(UUID uuid) {
        return courseVideoRepository.findById(uuid)
                .map(CourseVideoDto::from)
                .orElseThrow(() -> new IllegalArgumentException("Course video not found"));
    }

    @Override
    public List<CourseVideoDto> getAll() {
        return courseVideoRepository.findAll().stream().map(CourseVideoDto::from).toList();
    }

    @Override
    public Page<CourseVideoDto> getAll(Pageable pageable) {
        return courseVideoRepository.findAll(pageable).map(CourseVideoDto::from);
    }

    @Override
    public void deleteById(UUID uuid) {
        if(!courseVideoRepository.existsById(uuid)) throw new IllegalArgumentException("Course video not found");
        courseVideoRepository.deleteById(uuid);
    }

    public List<CourseVideoDto> getByCourseLessonId(UUID courseLessonId) {
        return courseVideoRepository.findByLessonId(courseLessonId).stream()
                .map(e -> addCourseLessonId(e, courseLessonId))
                .toList();
    }

    private CourseVideoDto addCourseLessonId(CourseVideo video, UUID courseLessonId){
        CourseVideoDto dto = CourseVideoDto.from(video);
        dto.setLessonId(courseLessonId);
        return dto;
    }
}
