package com.ying.learneyjourney.service;

import com.ying.learneyjourney.constaint.EnumCourseBadge;
import com.ying.learneyjourney.criteria.CourseCriteria;
import com.ying.learneyjourney.dto.CourseDetailDto;
import com.ying.learneyjourney.dto.CourseDto;
import com.ying.learneyjourney.dto.request.CreateCourseRequest;
import com.ying.learneyjourney.dto.response.CourseInfoResponse;
import com.ying.learneyjourney.dto.response.OverallProgressResponse;
import com.ying.learneyjourney.entity.*;
import com.ying.learneyjourney.master.*;
import com.ying.learneyjourney.repository.*;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

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
        return all.map(course -> {
            CourseDto courseDto = new CourseDto();
            CourseVideoRepository.DurationDisplayRow durationDisplay = courseVideoRepository.getCourseDurationDisplay(course.getId());
            String totalDuration = durationDisplay != null ? String.format("%s %s", durationDisplay.getDisplayValue(), durationDisplay.getDisplayUnit()) : "0 mins";
            Long totalLessons = courseLessonRepository.countByCourseId(course.getId());
            courseDto = CourseDto.from(course);
            courseDto.setLessons(totalLessons);
            courseDto.setDuration(totalDuration);
            return courseDto;
        });
    }

    public CourseInfoResponse getCourseDetailById(UUID courseId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new IllegalArgumentException("Course not found"));
        Double averageRating = courseReviewRepository.getAverageRatingByCourseId(courseId);
        Long totalEnrollments = enrollmentRepository.countByCourseId(courseId);
        Long totalLessons = courseLessonRepository.countByCourseId(courseId);
        CourseVideoRepository.DurationDisplayRow durationDisplay = courseVideoRepository.getCourseDurationDisplay(courseId);
        String totalDuration = durationDisplay != null ? String.format("%s %s", durationDisplay.getDisplayValue(), durationDisplay.getDisplayUnit()) : "0 mins";
        CourseInfoResponse detailDto = new CourseInfoResponse();
        detailDto.setId(course.getId());
        detailDto.setTitle(course.getTitle());
        detailDto.setSubtitle(course.getSubtitle());
        detailDto.setDescription(course.getDescription());
        detailDto.setCategory(course.getCategory());
        detailDto.setLevel(course.getLevel());
        detailDto.setDuration(totalDuration);
        detailDto.setHaveCertificate(course.getHaveCertificate());
        detailDto.setLessons(totalLessons);
        detailDto.setOutcomes(course.getOutcomes());
        detailDto.setRate(averageRating);
        detailDto.setStudents(String.format("%s students", totalEnrollments));
        CourseInfoResponse.TutorProfile tutorProfile = new CourseInfoResponse.TutorProfile();
        tutorProfile.setName(course.getTutorProfile().getUser().getDisplayName());
        tutorProfile.setTitle(course.getTutorProfile().getBio());
        detailDto.setTutorProfile(tutorProfile);

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

    public void createCourseFully(CreateCourseRequest request){
        TutorProfile tutorProfile = tutorProfileRepository.findById(request.getCourseInfo().getTutorProfileId()).orElseThrow(() -> new BusinessException("Tutor Profile not found", "TUTOR_PROFILE_NOT_FOUND"));
        Course course = getCourse(request, tutorProfile);
        courseRepository.save(course);
        Integer positionLesson = 1;
        for(CreateCourseRequest.LessonInfo lessonInfo : request.getLessonInfo()) {
            CourseLesson lesson = new CourseLesson();
            lesson.setCourse(course);
            lesson.setTitle(lessonInfo.getTitle());
            lesson.setDescription(lessonInfo.getDescription());
            lesson.setPosition(positionLesson);
            courseLessonRepository.save(lesson);
            Integer positionVideo = 1;
            for(CreateCourseRequest.VideoInfo videoInfo : lessonInfo.getVideos()) {
                CourseVideo video = new CourseVideo();
                video.setCourseLesson(lesson);
                video.setTitle(videoInfo.getTitle());
                video.setUrl(videoInfo.getUrl());
                video.setPosition(positionVideo);
                courseVideoRepository.save(video);
                positionVideo++;
            }
        }
    }

    @NotNull
    private static Course getCourse(CreateCourseRequest request, TutorProfile tutorProfile) {
        Course course = new Course();
        course.setTitle(request.getCourseInfo().getTitle());
        course.setDescription(request.getCourseInfo().getDescription());
        course.setTutorProfile(tutorProfile);
        course.setCategory(request.getCourseInfo().getCategory());
        course.setLevel(request.getCourseInfo().getLevel());
        course.setImageUrl(request.getCourseInfo().getImageUrl());
        course.setIsLive(request.getCourseInfo().getIsLive());
        course.setBadge(EnumCourseBadge.NEW);
        course.setAccess(request.getCourseInfo().getAccess());
        course.setPriceThb(request.getCourseInfo().getPriceThb());
        course.setSubtitle(request.getCourseInfo().getSubtitle());
        course.setOutcomes(request.getCourseInfo().getOutcomes());
        return course;
    }

}
