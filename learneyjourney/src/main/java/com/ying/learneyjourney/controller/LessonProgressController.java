package com.ying.learneyjourney.controller;

import com.ying.learneyjourney.Util.FirebaseAuthUtil;
import com.ying.learneyjourney.dto.FullCourseProgressDto;
import com.ying.learneyjourney.dto.LessonProgressDto;
import com.ying.learneyjourney.dto.StudentLessonProgressDto;
import com.ying.learneyjourney.dto.response.LatestLessonUpdateResponse;
import com.ying.learneyjourney.dto.response.OverallProgressResponse;
import com.ying.learneyjourney.master.MasterController;
import com.ying.learneyjourney.master.SearchCriteria;
import com.ying.learneyjourney.request.UserIdCourseIdRequest;
import com.ying.learneyjourney.service.LessonProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lesson-progress")
@RequiredArgsConstructor
public class LessonProgressController implements MasterController<LessonProgressDto, UUID> {
    private final LessonProgressService lessonProgressService;
    private final FirebaseAuthUtil firebaseAuthUtil;
    @Override
    public ResponseEntity<LessonProgressDto> getById(UUID uuid) {
        return ResponseEntity.ok(lessonProgressService.getById(uuid));
    }

    @Override
    public ResponseEntity<Page<LessonProgressDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(lessonProgressService.getAll(pageable));
    }

    @Override
    public ResponseEntity<LessonProgressDto> create(LessonProgressDto body) {
        return ResponseEntity.ok(lessonProgressService.create(body));
    }

    @Override
    public ResponseEntity<LessonProgressDto> update(UUID uuid, LessonProgressDto body) {
        return ResponseEntity.ok(lessonProgressService.update(uuid, body));
    }

    @Override
    public ResponseEntity<Void> delete(UUID uuid) {
        lessonProgressService.deleteById(uuid);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/full-course-progress")
    public ResponseEntity<FullCourseProgressDto> fullCourseProgress(@RequestBody UUID courseId, @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) throws Exception {
        String userId = firebaseAuthUtil.getUserIdFromToken(authHeader);
        return ResponseEntity.ok(lessonProgressService.getFullCourseProgress(courseId, userId));
    }
    @PostMapping("/get-by-user-lesson")
    public ResponseEntity<List<LessonProgressDto>> getByLessonId(@RequestBody UserIdCourseIdRequest request) {
        return ResponseEntity.ok(lessonProgressService.getByCourseId(request.getUserId(), request.getCourseId()));
    }
    @PostMapping("/get-all-details")
    public ResponseEntity<List<StudentLessonProgressDto>> getStudentLessonProgress(@RequestBody UUID courseId, @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) throws Exception {
        String id = firebaseAuthUtil.getUserIdFromToken(authHeader);
        return ResponseEntity.ok(lessonProgressService.getStudentLessonProgress(courseId, id));
    }

    @PostMapping("/lastest-update")
    public ResponseEntity<LatestLessonUpdateResponse> getStudentLatestProgress(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) throws Exception {
        String id = firebaseAuthUtil.getUserIdFromToken(authHeader);
        return ResponseEntity.ok(lessonProgressService.getLatestUpdateLesson(id));
    }

    @PostMapping("/overall-progress")
    public ResponseEntity<OverallProgressResponse> getOverallProgressResponseController(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) throws Exception {
        String id = firebaseAuthUtil.getUserIdFromToken(authHeader);
        return ResponseEntity.ok(lessonProgressService.getOverallProgress(id));
    }

}
