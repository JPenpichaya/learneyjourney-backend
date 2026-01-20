package com.ying.learneyjourney.service;

import com.ying.learneyjourney.criteria.CourseCriteria;
import com.ying.learneyjourney.dto.CourseDetailDto;
import com.ying.learneyjourney.dto.CourseDto;
import com.ying.learneyjourney.entity.Course;
import com.ying.learneyjourney.entity.TutorProfile;
import com.ying.learneyjourney.entity.VideoProgress;
import com.ying.learneyjourney.master.*;
import com.ying.learneyjourney.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.UUID;
@Service
@AllArgsConstructor
public class CourseService implements MasterService<CourseDto, UUID> {
    private final CourseRepository courseRepository;
    private final TutorProfileRepository tutorProfileRepository;
    private final CourseReviewRepository courseReviewRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseLessonRepository courseLessonRepository;
    private final CourseVideoRepository courseVideoRepository;
    private final VideoProgressRepository videoProgressRepository;
    @Override
    public CourseDto create(CourseDto dto) {
        TutorProfile profile = tutorProfileRepository.findById(dto.getTutorProfileId()).orElseThrow(() -> new IllegalArgumentException("Tutor Profile not found"));
        Course entity = CourseDto.toEntity(dto, profile);
        courseRepository.save(entity);
        dto.setId(entity.getId());
        return dto;
    }

    @Override
    public CourseDto update(UUID uuid, CourseDto dto) {
        Course course = courseRepository.findById(uuid).orElseThrow(() -> new IllegalArgumentException("Course not found"));
        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        courseRepository.save(course);
        return dto;
    }

    @Override
    public CourseDto getById(UUID uuid) {
        Course course = courseRepository.findById(uuid).orElseThrow(() -> new IllegalArgumentException("Course not found"));
        return CourseDto.from(course);
    }

    @Override
    public List<CourseDto> getAll() {
        return courseRepository.findAll().stream().map(CourseDto::from).toList();
    }

    @Override
    public Page<CourseDto> getAll(Pageable pageable) {
        return courseRepository.findAll(pageable).map(CourseDto::from);
    }

    @Override
    public void deleteById(UUID uuid) {
        if(!courseRepository.existsById(uuid)) throw new BusinessException("Course not found", "COURSE_NOT_FOUND");
        courseRepository.deleteById(uuid);
    }
    public Page<CourseDto> search(PageCriteria<CourseCriteria> condition){
        Page<Course> all = courseRepository.findAll(condition.getSpecification(), condition.generatePageRequest());
        return all.map(CourseDto::from);
    }

    public CourseDetailDto getCourseDetailById(UUID courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new IllegalArgumentException("Course not found"));
        Double averageRating = courseReviewRepository.getAverageRatingByCourseId(courseId);
        Long totalEnrollments = enrollmentRepository.countByCourseId(courseId);
        Long totalLessons = courseLessonRepository.countByCourseId(courseId);
        CourseVideoRepository.DurationDisplayRow durationDisplay = courseVideoRepository.getCourseDurationDisplay(courseId);
        String totalDuration = durationDisplay != null ? String.format("%s %s", durationDisplay.getDisplayValue(), durationDisplay.getDisplayUnit()) : "0 mins";
        CourseDetailDto detailDto = new CourseDetailDto();
        detailDto.setId(course.getId());
        detailDto.setTotalStudents(totalEnrollments);
        detailDto.setTotalLessons(totalLessons);
        detailDto.setRating(averageRating);
        detailDto.setTotalDuration(totalDuration);
        return detailDto;
    }

    public Page<CourseDto> getEnrolledCouresByUserId(PageCriteria<CourseCriteria> condition, String userId){
        condition.getCondition().setUserId(userId);
        Page<Course> courses = courseRepository.findAll(condition.getSpecification(), condition.generatePageRequest());
        return courses.map(CourseDto::from);
    }

    public CourseDto getLatestProgressCourse(String userId){
        VideoProgress progress = videoProgressRepository.findFirstByUserIdAndStatusOrderByUpdatedAtDesc(userId, "PROGRESS");
        if(progress == null){
            throw new BusinessException("No course in progress", "NO_COURSE_IN_PROGRESS");
        }
        UUID courseId = progress.getCourseVideo().getCourseLesson().getCourse().getId();
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new IllegalArgumentException("Course not found"));
        return CourseDto.from(course);
    }

    public CourseDto getIsShowCourseOnProfile(UUID profileId){
        List<Course> onProfileTrue = courseRepository.findBy_IsShowCourseOnProfile_True(profileId);
        if(onProfileTrue.isEmpty()){
            throw new BusinessException("No course to show on profile", "NO_COURSE_ON_PROFILE");
        }
        Course course = onProfileTrue.getFirst();
        return CourseDto.from(course);
    }

    public Page<CourseDto> getCoursesByTutorProfileId(UUID tutorProfileId, PageCriteria<CourseCriteria> condition){
        condition.getCondition().setTutorId(tutorProfileId);
        Page<Course> courses = courseRepository.findAll(condition.getSpecification(), condition.generatePageRequest());
        return courses.map(CourseDto::from);
    }

}
