package com.ying.learneyjourney.controller;

import com.ying.learneyjourney.dto.CourseVideoDto;
import com.ying.learneyjourney.master.MasterController;
import com.ying.learneyjourney.master.SearchCriteria;
import com.ying.learneyjourney.repository.CourseLessonRepository;
import com.ying.learneyjourney.service.CourseVideoService;
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
@RequestMapping("/api/course-videos")
@RequiredArgsConstructor
public class CourseVideoController implements MasterController<CourseVideoDto, UUID> {
    private final CourseVideoService courseVideoService;
    @Override
    public ResponseEntity<CourseVideoDto> getById(UUID uuid) {
        return ResponseEntity.ok(courseVideoService.getById(uuid));
    }

    @Override
    public ResponseEntity<Page<CourseVideoDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(courseVideoService.getAll(pageable));
    }

    @Override
    public ResponseEntity<CourseVideoDto> create(CourseVideoDto body) {
        return ResponseEntity.ok(courseVideoService.create(body));
    }

    @Override
    public ResponseEntity<CourseVideoDto> update(UUID uuid, CourseVideoDto body) {
        return ResponseEntity.ok(courseVideoService.update(uuid, body));
    }

    @Override
    public ResponseEntity<Void> delete(UUID uuid) {
        courseVideoService.deleteById(uuid);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/get-by-lesson")
    public ResponseEntity<List<CourseVideoDto>> getByLessonId(@RequestBody UUID lessonId) {
        return ResponseEntity.ok(courseVideoService.getByCourseLessonId(lessonId));
    }

}
