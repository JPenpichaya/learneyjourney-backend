package com.ying.learneyjourney.controller;

import com.ying.learneyjourney.Util.FirebaseAuthUtil;
import com.ying.learneyjourney.criteria.StudentNoteCriteria;
import com.ying.learneyjourney.dto.StudentNoteDto;
import com.ying.learneyjourney.entity.StudentNote;
import com.ying.learneyjourney.master.MasterController;
import com.ying.learneyjourney.master.PageCriteria;
import com.ying.learneyjourney.master.SearchCriteria;
import com.ying.learneyjourney.service.StudentNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/student-notes")
public class StudentNoteController implements MasterController<StudentNoteDto, UUID> {
    private final StudentNoteService studentNoteService;
    private final FirebaseAuthUtil firebaseAuthUtil;
    @Override
    public ResponseEntity<StudentNoteDto> getById(UUID uuid) {
        return ResponseEntity.ok(studentNoteService.getById(uuid));
    }

    @Override
    public ResponseEntity<Page<StudentNoteDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(studentNoteService.getAll(pageable));
    }

    @Override
    public ResponseEntity<StudentNoteDto> create(StudentNoteDto body) {
        return ResponseEntity.ok(studentNoteService.create(body));
    }

    @Override
    public ResponseEntity<StudentNoteDto> update(UUID uuid, StudentNoteDto body) {
        return ResponseEntity.ok(studentNoteService.update(uuid, body));
    }

    @Override
    public ResponseEntity<Void> delete(UUID uuid) {
        studentNoteService.deleteById(uuid);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/search-list")
    public ResponseEntity<List<StudentNoteDto>> searchList(@RequestBody PageCriteria<StudentNoteCriteria> conditions) {
        return ResponseEntity.ok(studentNoteService.getAllList(conditions));
    }
    @PostMapping("/create-update")
    public ResponseEntity<StudentNoteDto> createUpdate(@RequestBody StudentNoteDto body, @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) throws Exception {
        String id = firebaseAuthUtil.getUserIdFromToken(authHeader);
        return ResponseEntity.ok(studentNoteService.createUpdate(body, id));
    }
}
