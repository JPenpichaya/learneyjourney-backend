package com.ying.learneyjourney.controller;

import com.ying.learneyjourney.Util.FirebaseAuthUtil;
import com.ying.learneyjourney.criteria.CourseCriteria;
import com.ying.learneyjourney.dto.CourseDetailDto;
import com.ying.learneyjourney.dto.CourseDto;
import com.ying.learneyjourney.dto.request.CreateCourseRequest;
import com.ying.learneyjourney.dto.response.CourseInfoResponse;
import com.ying.learneyjourney.master.MasterController;
import com.ying.learneyjourney.master.PageCriteria;
import com.ying.learneyjourney.master.SearchCriteria;
import com.ying.learneyjourney.service.CourseService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/api/course")
@AllArgsConstructor
public class CourseController implements MasterController<CourseDto, UUID> {
    private final CourseService courseService;
    private final FirebaseAuthUtil firebaseAuthUtil;
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
    @PostMapping("/all/search")
    public ResponseEntity<Page<CourseDto>> search(@RequestBody PageCriteria<CourseCriteria> conditions) {
        return ResponseEntity.ok(courseService.search(conditions));
    }

    @PostMapping("/details/{courseId}")
    public ResponseEntity<CourseInfoResponse> getCourseDetails(@PathVariable  UUID courseId) {
        com.ying.learneyjourney.dto.response.CourseInfoResponse courseDetail = courseService.getCourseDetailById(courseId);
        return ResponseEntity.ok(courseDetail);
    }

    @PostMapping("/student/enrolled-courses")
    public ResponseEntity<Page<CourseDto>> getEnrolledCourses(@RequestBody PageCriteria<CourseCriteria> conditions , @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) throws Exception {
        String id = firebaseAuthUtil.getUserIdFromToken(authHeader);
        return ResponseEntity.ok(courseService.getEnrolledCouresByUserId(conditions, id));
    }

    @PostMapping("/get-latest-learning")
    public ResponseEntity<CourseDto> getLatestCourse(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) throws Exception {
        String id = firebaseAuthUtil.getUserIdFromToken(authHeader);
        return ResponseEntity.ok(courseService.getLatestProgressCourse(id));
    }

    @PostMapping("/get-is-show-profile/{profileId}")
    public ResponseEntity<CourseDto> getIsShowCourseOnProfile(@PathVariable UUID profileId) {
        return ResponseEntity.ok(courseService.getIsShowCourseOnProfile(profileId));
    }

    @PostMapping("/create-fully")
    public ResponseEntity<Void> createCourseFullyController(@RequestBody CreateCourseRequest request){
        courseService.createCourseFully(request);
        return ResponseEntity.ok().build();
    }
}
