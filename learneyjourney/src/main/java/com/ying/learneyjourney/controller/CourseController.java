package com.ying.learneyjourney.controller;

import com.ying.learneyjourney.criteria.CourseCriteria;
import com.ying.learneyjourney.dto.CourseDto;
import com.ying.learneyjourney.master.MasterController;
import com.ying.learneyjourney.master.PageCriteria;
import com.ying.learneyjourney.master.SearchCriteria;
import com.ying.learneyjourney.service.CourseService;
import lombok.AllArgsConstructor;
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
@RequestMapping("/api/course")
@AllArgsConstructor
public class CourseController implements MasterController<CourseDto, UUID> {
    private final CourseService courseService;
    @Override
    public ResponseEntity<CourseDto> getById(UUID uuid) {
        return ResponseEntity.ok(courseService.getById(uuid));
    }

    @Override
    public ResponseEntity<Page<CourseDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(courseService.getAll(pageable));
    }

    @Override
    public ResponseEntity<CourseDto> create(CourseDto body) {
        return ResponseEntity.ok(courseService.create(body));
    }

    @Override
    public ResponseEntity<CourseDto> update(UUID uuid, CourseDto body) {
        return ResponseEntity.ok(courseService.update(uuid, body));
    }

    @Override
    public ResponseEntity<Void> delete(UUID uuid) {
        courseService.deleteById(uuid);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/search")
    public ResponseEntity<Page<CourseDto>> search(@RequestBody PageCriteria<CourseCriteria> conditions) {
        return ResponseEntity.ok(courseService.search(conditions));
    }
}
