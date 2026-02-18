package com.ying.learneyjourney.controller;

import com.ying.learneyjourney.Util.FirebaseAuthUtil;
import com.ying.learneyjourney.dto.VideoProgressDto;
import com.ying.learneyjourney.master.MasterController;
import com.ying.learneyjourney.master.SearchCriteria;
import com.ying.learneyjourney.request.UserIdCourseIdRequest;
import com.ying.learneyjourney.service.VideoProgressService;
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
@RequestMapping("/api/video-progress")
public class VideoProgressController implements MasterController<VideoProgressDto, UUID> {
    private final VideoProgressService videoProgressService;
    private final FirebaseAuthUtil firebaseAuthUtil;
    @Override
    public ResponseEntity<VideoProgressDto> getById(UUID uuid) {
        return ResponseEntity.ok(videoProgressService.getById(uuid));
    }

    @Override
    public ResponseEntity<Page<VideoProgressDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(videoProgressService.getAll(pageable));
    }

    @Override
    public ResponseEntity<VideoProgressDto> create(VideoProgressDto body) {
        return ResponseEntity.ok(videoProgressService.create(body));
    }

    @Override
    public ResponseEntity<VideoProgressDto> update(UUID uuid, VideoProgressDto body) {
        return ResponseEntity.ok(videoProgressService.update(uuid, body));
    }

    @Override
    public ResponseEntity<Void> delete(UUID uuid) {
        videoProgressService.deleteById(uuid);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/get-by-user-lesson")
    public ResponseEntity<List<VideoProgressDto>> getByLessonId(@RequestBody UUID lessonId, @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) throws Exception {
        String id = firebaseAuthUtil.getUserIdFromToken(authHeader);
        return ResponseEntity.ok(videoProgressService.getByLessonId(id, lessonId));
    }

    @PostMapping("/student/update-completed")
    public ResponseEntity<Void> setVideoProgressCompleted(@RequestBody UUID progressId) {
        videoProgressService.setVideoProgressCompleted(progressId);
        return ResponseEntity.ok().build();
    }
    @PostMapping("/set-video-progress-not-start")
    public ResponseEntity<Void> setVideoProgressAllNotStart(@RequestBody UserIdCourseIdRequest request) {
        videoProgressService.setVideoProgressAllNotStart(request.getUserId(), request.getCourseId());
        return ResponseEntity.ok().build();
    }

}
