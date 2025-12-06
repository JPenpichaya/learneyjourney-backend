package com.ying.learneyjourney.controller;

import com.ying.learneyjourney.dto.EnrollmentDto;
import com.ying.learneyjourney.master.MasterController;
import com.ying.learneyjourney.master.SearchCriteria;
import com.ying.learneyjourney.repository.EnrollmentRepository;
import com.ying.learneyjourney.service.EnrollmentService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/api/enrollment")
@AllArgsConstructor
public class EnrollmentController implements MasterController<EnrollmentDto, UUID> {
    private final EnrollmentService enrollmentService;
    @Override
    public ResponseEntity<EnrollmentDto> getById(UUID uuid) {
        return ResponseEntity.ok(enrollmentService.getById(uuid));
    }

    @Override
    public ResponseEntity<Page<EnrollmentDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(enrollmentService.getAll(pageable));
    }

    @Override
    public ResponseEntity<EnrollmentDto> create(EnrollmentDto body) {
        return ResponseEntity.ok(enrollmentService.create(body));
    }

    @Override
    public ResponseEntity<EnrollmentDto> update(UUID uuid, EnrollmentDto body) {
        return ResponseEntity.ok(enrollmentService.update(uuid, body));
    }

    @Override
    public ResponseEntity<Void> delete(UUID uuid) {
        enrollmentService.deleteById(uuid);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Page<EnrollmentDto>> search(List<SearchCriteria> criteria, Pageable pageable) {
        return ResponseEntity.ok(enrollmentService.search(criteria, pageable));
    }
}
