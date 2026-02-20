package com.ying.learneyjourney.service;

import com.ying.learneyjourney.Util.TimeManagement;
import com.ying.learneyjourney.constaint.EnumLessonProgressStatus;
import com.ying.learneyjourney.constaint.EnumVideoProgressStatus;
import com.ying.learneyjourney.dto.FullCourseProgressDto;
import com.ying.learneyjourney.dto.LessonProgressDto;
import com.ying.learneyjourney.dto.StudentLessonProgressDto;
import com.ying.learneyjourney.dto.VideoProgressRowDto;
import com.ying.learneyjourney.dto.response.LatestLessonUpdateResponse;
import com.ying.learneyjourney.dto.response.OverallProgressResponse;
import com.ying.learneyjourney.entity.*;
import com.ying.learneyjourney.master.BusinessException;
import com.ying.learneyjourney.master.MasterService;

import com.ying.learneyjourney.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LessonProgressService implements MasterService<LessonProgressDto, UUID> {
    private final LessonProgressRepository lessonProgressRepository;
    private final CourseLessonRepository courseLessonRepository;
    private final UserRepository userRepository;
    private final CourseVideoRepository courseVideoRepository;
    private final VideoProgressRepository videoProgressRepository;
    private final CourseRepository courseRepository;
    @Override
    public LessonProgressDto create(LessonProgressDto dto) {
        LessonProgress entity = LessonProgressDto.toEntity(dto);
        userRepository.findById(dto.getUserId()).ifPresent(entity::setUser);
        courseLessonRepository.findById(dto.getCourseLessonId()).ifPresent(entity::setCourseLesson);
        lessonProgressRepository.save(entity);
        dto.setId(dto.getId());
        return dto;
    }

    @Override
    public LessonProgressDto update(UUID uuid, LessonProgressDto dto) {
        return null;
    }

    public LessonProgressDto updateLessonProgress(List<UUID> uuidList, LessonProgressDto dto) {
        List<LessonProgress> lessonProgressList = lessonProgressRepository.findByCourseLessonIdInAndUserId(uuidList, dto.getUserId());
        LessonProgress lessonProgress = lessonProgressList.getFirst();
        lessonProgress.setStatus(dto.getStatus());
        lessonProgress.setCompletedAt(dto.getCompletedAt());
        lessonProgressRepository.save(lessonProgress);
        return LessonProgressDto.from(lessonProgress);
    }

    @Override
    public LessonProgressDto getById(UUID uuid) {
        return lessonProgressRepository.findById(uuid).map(LessonProgressDto::from).orElseThrow(() -> new IllegalArgumentException("Lesson progress not found"));
    }

    @Override
    public List<LessonProgressDto> getAll() {
        return lessonProgressRepository.findAll().stream().map(LessonProgressDto::from).toList();
    }

    @Override
    public Page<LessonProgressDto> getAll(Pageable pageable) {
        return lessonProgressRepository.findAll(pageable).map(LessonProgressDto::from);
    }

    @Override
    public void deleteById(UUID uuid) {
        if(!lessonProgressRepository.existsById(uuid)) throw new IllegalArgumentException("Lesson progress not found");
        lessonProgressRepository.deleteById(uuid);
    }

    public List<LessonProgressDto> getByCourseId(String userId, UUID courseId){
        List<CourseLesson> lessons = courseLessonRepository.findByCourse(courseId);
        List<UUID> ids = lessons.stream().map(CourseLesson::getId).toList();
        List<LessonProgress> progresses = lessonProgressRepository.findByCourseLessonIdInAndUserId(ids, userId);
        return progresses.stream().map(this::convertToDto).toList();
    }

    private LessonProgressDto convertToDto(LessonProgress lessonProgress){
        LessonProgressDto from = LessonProgressDto.from(lessonProgress);
        from.setCourseLessonId(lessonProgress.getCourseLesson().getId());
        return from;
    }

    public FullCourseProgressDto getFullCourseProgress(UUID courseId, String userId) {
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new BusinessException("Course not found", "COURSE_NOT_FOUND"));
        
        List<CourseLesson> lessons = courseLessonRepository.findByCourse(courseId);
        List<UUID> lessonIds = lessons.stream().map(CourseLesson::getId).toList();
        
        List<CourseVideo> allCourseVideos = courseVideoRepository.findByLessonIds(lessonIds);
        List<UUID> allVideoIds = allCourseVideos.stream().map(CourseVideo::getId).toList();

        List<VideoProgress> userVideoProgresses = videoProgressRepository.findByUserIdAndVideoIdIn(userId, allVideoIds);

        long totalVideos = allCourseVideos.size();
        long completedVideos = userVideoProgresses.stream()
                                    .filter(vp -> EnumVideoProgressStatus.COMPLETED.equals(vp.getStatus()))
                                    .count();
        
        int percent = (totalVideos == 0) ? 0 : (int)((completedVideos * 100) / totalVideos);
        
        FullCourseProgressDto dto = new FullCourseProgressDto();
        dto.setCourseId(courseId);
        dto.setCourseTitle(course.getTitle());
        dto.setCourseSubtitle(course.getSubtitle());
        dto.setUserId(userId);
        dto.setCompletedPercentage(percent);
        dto.setTotalLessons(totalVideos); // Renamed to reflect video count
        dto.setCompletedLessons(completedVideos); // Renamed to reflect video count
        return dto;
    }

    public List<StudentLessonProgressDto> getStudentLessonProgress(UUID courseId, String userId){
        List<CourseLesson> lessons = courseLessonRepository.findByCourse(courseId);
        List<LessonProgress> lessonProgresses = lessonProgressRepository.findByCourseLessonIdInAndUserId(
                lessons.stream().map(CourseLesson::getId).toList(),
                userId);
        List<StudentLessonProgressDto> list = new ArrayList<>();
        for (CourseLesson lesson : lessons) {
            StudentLessonProgressDto dto = new StudentLessonProgressDto();
            Optional<LessonProgress> lessonProgress = lessonProgresses.stream().filter(lp -> lp.getCourseLesson().getId().equals(lesson.getId())).findFirst();
            lessonProgress.ifPresent(e -> dto.setStatus(e.getStatus()));
            List<VideoProgressRepository.VideoProgressRow> videoProgressRows = videoProgressRepository.findVideoProgressRows(userId, lesson.getId());
            List<VideoProgressRowDto> videoDtos = new ArrayList<>();
            for (VideoProgressRepository.VideoProgressRow row : videoProgressRows) {
                VideoProgressRowDto videoDto = new VideoProgressRowDto();
                videoDto.setId(row.getId());
                videoDto.setDescription(row.getDescription());
                videoDto.setStatus(EnumVideoProgressStatus.valueOf(row.getStatus()));
                videoDto.setCompletedAt(row.getCompletedAt());
                videoDto.setTitle(row.getTitle());
                videoDto.setUrl(row.getUrl());
                videoDto.setDuration(TimeManagement.formatSecondsToHMS(row.getDuration()));
                videoDto.setPosition(row.getPosition());
                videoDto.setWorksheet(row.getWorksheet());
                videoDto.setContact(row.getContact());
                videoDtos.add(videoDto);
            }
            dto.setPosition(lesson.getPosition());
            dto.setLessonId(lesson.getId());
            dto.setTitle(lesson.getTitle());
            dto.setVideos(videoDtos);

            list.add(dto);
        }
        return list;
    }

    public LatestLessonUpdateResponse getLatestUpdateLesson(String userId){
        LatestLessonUpdateResponse response = new LatestLessonUpdateResponse();
        VideoProgress videoProgress = videoProgressRepository.findNextVideoProgressForUser(userId).orElse(null);
        List<LatestLessonUpdateResponse.Course> courseList = new ArrayList<>();
        if(videoProgress == null) return response;
        CourseVideo courseVideo = videoProgress.getCourseVideo();
        FullCourseProgressDto fullCourseProgress = getFullCourseProgress(courseVideo.getCourseLesson().getCourse().getId(), userId);
        LatestLessonUpdateResponse.Course course = new LatestLessonUpdateResponse.Course();
        course.setTitle(courseVideo.getTitle());
        course.setId(courseVideo.getCourseLesson().getCourse().getId());
        course.setLastAtLesson(courseVideo.getCourseLesson().getPosition());
        course.setDuration(courseVideo.getDuration() == null ? null : TimeManagement.formatSecondsToHMS(courseVideo.getDuration()));
        course.setProgress(fullCourseProgress.getCompletedPercentage());
        courseList.add(course);
        response.setCourse(courseList);
        return response;
    }

    public OverallProgressResponse getOverallProgress(String userId){
        OverallProgressResponse response = new OverallProgressResponse();
        long progressCoursesByUserId = lessonProgressRepository.countInProgressCoursesByUserId(userId);
        long completedCoursesByUserId = lessonProgressRepository.countCompletedCoursesByUserId(userId);
        long allCoursesByUserId = lessonProgressRepository.countAllCoursesByUserId(userId);
        response.setCompleted(completedCoursesByUserId);
        response.setProgress(progressCoursesByUserId);
        response.setOverall(TimeManagement.calculateWatchPercentage(completedCoursesByUserId, allCoursesByUserId));
        return response;
    }

    public void setLessonProgressAllNotStart(String userId, UUID courseId){
        lessonProgressRepository.insertLessonProgressForCourse(userId, courseId);
    }
}
