package com.ying.learneyjourney.controller;

import com.ying.learneyjourney.dto.TutorProfilesDto;
import com.ying.learneyjourney.dto.UserDto;
import com.ying.learneyjourney.master.MasterController;
import com.ying.learneyjourney.master.SearchCriteria;
import com.ying.learneyjourney.service.TutorProfileService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tutor-profile")
@AllArgsConstructor
public class TutorProfileController implements MasterController<TutorProfilesDto, UUID> {
    private final TutorProfileService tutorProfileService;
    @Override
    public ResponseEntity<TutorProfilesDto> getById(UUID uuid) {
        return ResponseEntity.ok(tutorProfileService.getById(uuid));
    }

    @Override
    public ResponseEntity<Page<TutorProfilesDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(tutorProfileService.getAll(pageable));
    }

    @Override
    public ResponseEntity<TutorProfilesDto> create(TutorProfilesDto body) {
        return ResponseEntity.ok(tutorProfileService.create(body));
    }

    @Override
    public ResponseEntity<TutorProfilesDto> update(UUID uuid, TutorProfilesDto body) {
        return ResponseEntity.ok(tutorProfileService.update(uuid, body));
    }

    @Override
    public ResponseEntity<Void> delete(UUID uuid) {
        tutorProfileService.deleteById(uuid);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Page<TutorProfilesDto>> search(List<SearchCriteria> criteria, Pageable pageable) {
        return ResponseEntity.ok(tutorProfileService.search(criteria, pageable));
    }
}
