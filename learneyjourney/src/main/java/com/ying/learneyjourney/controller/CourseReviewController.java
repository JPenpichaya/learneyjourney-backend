package com.ying.learneyjourney.controller;

import com.ying.learneyjourney.dto.CourseReviewDto;
import com.ying.learneyjourney.master.MasterController;
import com.ying.learneyjourney.service.CourseReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/course-review")
@RequiredArgsConstructor
public class CourseReviewController implements MasterController<CourseReviewDto, UUID> {
    private final CourseReviewService courseReviewService;
    @Override
    public ResponseEntity<CourseReviewDto> getById(UUID uuid) {
        return null;
    }

    @Override
    public ResponseEntity<Page<CourseReviewDto>> getAll(Pageable pageable) {
        return null;
    }

    @Override
    public ResponseEntity<CourseReviewDto> create(CourseReviewDto body) {
        return ResponseEntity.ok(courseReviewService.create(body));
    }

    @Override
    public ResponseEntity<CourseReviewDto> update(UUID uuid, CourseReviewDto body) {
        return null;
    }

    @Override
    public ResponseEntity<Void> delete(UUID uuid) {
        return null;
    }
}
