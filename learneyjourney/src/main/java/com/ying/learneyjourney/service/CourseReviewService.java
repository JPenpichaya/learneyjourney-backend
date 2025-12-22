package com.ying.learneyjourney.service;

import com.ying.learneyjourney.dto.CourseReviewDto;
import com.ying.learneyjourney.entity.CourseReview;
import com.ying.learneyjourney.master.MasterService;
import com.ying.learneyjourney.repository.CourseRepository;
import com.ying.learneyjourney.repository.CourseReviewRepository;
import com.ying.learneyjourney.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
@Service
@RequiredArgsConstructor
public class CourseReviewService implements MasterService<CourseReviewDto, UUID> {
    private final CourseReviewRepository courseReviewRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    @Override
    public CourseReviewDto create(CourseReviewDto dto) {
        CourseReview entity = CourseReviewDto.toEntity(dto);
        userRepository.findById(dto.getUserId()).ifPresent(entity::setUser);
        courseRepository.findById(dto.getCourseId()).ifPresent(entity::setCourse);
        courseReviewRepository.save(entity);
        dto.setId(entity.getId());
        return dto;
    }

    @Override
    public CourseReviewDto update(UUID uuid, CourseReviewDto dto) {
        return null;
    }

    @Override
    public CourseReviewDto getById(UUID uuid) {
        return null;
    }

    @Override
    public List<CourseReviewDto> getAll() {
        return null;
    }

    @Override
    public Page<CourseReviewDto> getAll(Pageable pageable) {
        return null;
    }

    @Override
    public void deleteById(UUID uuid) {
    }
}
