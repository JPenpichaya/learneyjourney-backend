package com.ying.learneyjourney.service;

import com.ying.learneyjourney.criteria.CourseCriteria;
import com.ying.learneyjourney.dto.CourseDto;
import com.ying.learneyjourney.entity.Course;
import com.ying.learneyjourney.entity.TutorProfile;
import com.ying.learneyjourney.master.*;
import com.ying.learneyjourney.repository.CourseRepository;
import com.ying.learneyjourney.repository.TutorProfileRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.UUID;
@Service
@AllArgsConstructor
public class CourseService implements MasterService<CourseDto, UUID> {
    private final CourseRepository courseRepository;
    private final TutorProfileRepository tutorProfileRepository;
    @Override
    public CourseDto create(CourseDto dto) {
        TutorProfile profile = tutorProfileRepository.findById(dto.getTutorProfileId()).orElseThrow(() -> new IllegalArgumentException("Tutor Profile not found"));
        Course entity = CourseDto.toEntity(dto, profile);
        courseRepository.save(entity);
        dto.setId(entity.getId());
        return dto;
    }

    @Override
    public CourseDto update(UUID uuid, CourseDto dto) {
        Course course = courseRepository.findById(uuid).orElseThrow(() -> new IllegalArgumentException("Course not found"));
        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        courseRepository.save(course);
        return dto;
    }

    @Override
    public CourseDto getById(UUID uuid) {
        Course course = courseRepository.findById(uuid).orElseThrow(() -> new IllegalArgumentException("Course not found"));
        return CourseDto.from(course);
    }

    @Override
    public List<CourseDto> getAll() {
        return courseRepository.findAll().stream().map(CourseDto::from).toList();
    }

    @Override
    public Page<CourseDto> getAll(Pageable pageable) {
        return courseRepository.findAll(pageable).map(CourseDto::from);
    }

    @Override
    public void deleteById(UUID uuid) {
        if(!courseRepository.existsById(uuid)) throw new BusinessException("Course not found", "COURSE_NOT_FOUND");
        courseRepository.deleteById(uuid);
    }
    public Page<CourseDto> search(PageCriteria<CourseCriteria> condition){
        Page<Course> all = courseRepository.findAll(condition.getSpecification(), condition.generatePageRequest());
        return all.map(CourseDto::from);
    }
}
