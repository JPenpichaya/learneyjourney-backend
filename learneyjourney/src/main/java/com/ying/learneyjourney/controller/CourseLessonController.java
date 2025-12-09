package com.ying.learneyjourney.controller;

import com.ying.learneyjourney.dto.CourseLessonDto;
import com.ying.learneyjourney.master.MasterController;
import com.ying.learneyjourney.master.SearchCriteria;
import com.ying.learneyjourney.service.CourseLessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/course-lessons")
@RequiredArgsConstructor
public class CourseLessonController implements MasterController<CourseLessonDto, UUID> {
    private final CourseLessonService courseLessonService;
    @Override
    public ResponseEntity<CourseLessonDto> getById(UUID uuid) {
        return ResponseEntity.ok(courseLessonService.getById(uuid));
    }

    @Override
    public ResponseEntity<Page<CourseLessonDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(courseLessonService.getAll(pageable));
    }

    @Override
    public ResponseEntity<CourseLessonDto> create(CourseLessonDto body) {
        return ResponseEntity.ok(courseLessonService.create(body));
    }

    @Override
    public ResponseEntity<CourseLessonDto> update(UUID uuid, CourseLessonDto body) {
        return ResponseEntity.ok(courseLessonService.update(uuid, body));
    }

    @Override
    public ResponseEntity<Void> delete(UUID uuid) {
        courseLessonService.deleteById(uuid);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/get-by-course")
    public ResponseEntity<List<CourseLessonDto>> getListByCourseId(@RequestBody UUID courseId) {
        return ResponseEntity.ok(courseLessonService.getByCourse(courseId));
    }

}
