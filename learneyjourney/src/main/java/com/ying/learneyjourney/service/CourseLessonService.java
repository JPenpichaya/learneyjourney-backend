package com.ying.learneyjourney.service;

import com.ying.learneyjourney.dto.CourseLessonDto;
import com.ying.learneyjourney.entity.Course;
import com.ying.learneyjourney.entity.CourseLesson;
import com.ying.learneyjourney.master.MasterService;
import com.ying.learneyjourney.master.SearchCriteria;

import com.ying.learneyjourney.repository.CourseLessonRepository;
import com.ying.learneyjourney.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseLessonService implements MasterService<CourseLessonDto, UUID> {
    private final CourseLessonRepository courseLessonRepository;
    private final CourseRepository courseRepository;
    @Override
    public CourseLessonDto create(CourseLessonDto dto) {
        CourseLesson entity = CourseLessonDto.toEntity(dto);
        courseRepository.findById(dto.getCourseId()).ifPresent(entity::setCourse);
        courseLessonRepository.save(entity);
        dto.setId(entity.getId());
        return dto;
    }

    @Override
    public CourseLessonDto update(UUID uuid, CourseLessonDto dto) {
        Optional<CourseLesson> optional = courseLessonRepository.findById(uuid);
        CourseLesson courseLesson = optional.orElseThrow(() -> new IllegalArgumentException("Course lesson not found"));
        courseLesson.setTitle(dto.getTitle());
        courseLesson.setDescription(dto.getDescription());
        courseLesson.setPosition(dto.getPosition());
        courseLessonRepository.save(courseLesson);
        return dto;
    }

    @Override
    public CourseLessonDto getById(UUID uuid) {
        CourseLesson courseLesson = courseLessonRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Course lesson not found"));
        return CourseLessonDto.from(courseLesson);
    }

    @Override
    public List<CourseLessonDto> getAll() {
        return courseLessonRepository.findAll().stream().map(CourseLessonDto::from).toList();
    }

    @Override
    public Page<CourseLessonDto> getAll(Pageable pageable) {
        return courseLessonRepository.findAll(pageable).map(CourseLessonDto::from);
    }

    @Override
    public void deleteById(UUID uuid) {
        if(!courseLessonRepository.existsById(uuid)) throw new IllegalArgumentException("Course lesson not found");
        courseLessonRepository.deleteById(uuid);
    }

    public List<CourseLessonDto>  getByCourse(UUID courseId){
        return courseLessonRepository.findByCourse(courseId)
                .stream().map(e -> addCourseId(e, courseId)).toList();
    }

    private CourseLessonDto addCourseId(CourseLesson lesson, UUID courseId){
        CourseLessonDto dto = CourseLessonDto.from(lesson);
        dto.setCourseId(courseId);
        return dto;
    }

}
