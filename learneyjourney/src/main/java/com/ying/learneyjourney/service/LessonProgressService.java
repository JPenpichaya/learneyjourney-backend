package com.ying.learneyjourney.service;

import com.ying.learneyjourney.constaint.EnumLessonProgressStatus;
import com.ying.learneyjourney.dto.FullCourseProgressDto;
import com.ying.learneyjourney.dto.LessonProgressDto;
import com.ying.learneyjourney.entity.CourseLesson;
import com.ying.learneyjourney.entity.LessonProgress;
import com.ying.learneyjourney.master.MasterService;
import com.ying.learneyjourney.master.SearchCriteria;

import com.ying.learneyjourney.repository.CourseLessonRepository;
import com.ying.learneyjourney.repository.LessonProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LessonProgressService implements MasterService<LessonProgressDto, UUID> {
    private final LessonProgressRepository lessonProgressRepository;
    private final CourseLessonRepository courseLessonRepository;
    @Override
    public LessonProgressDto create(LessonProgressDto dto) {
        LessonProgress entity = LessonProgressDto.toEntity(dto);
        lessonProgressRepository.save(entity);
        dto.setId(dto.getId());
        return dto;
    }

    @Override
    public LessonProgressDto update(UUID uuid, LessonProgressDto dto) {
        LessonProgress lessonProgress = lessonProgressRepository.findById(uuid).orElseThrow(() -> new IllegalArgumentException("Lesson progress not found"));
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
        List<CourseLesson> lessons = courseLessonRepository.findByCourse(courseId);
        List<UUID> ids = lessons.stream().map(CourseLesson::getId).toList();
        List<LessonProgress> progresses = lessonProgressRepository.findByCourseLessonIdInAndUserId(ids, userId);
        long completedLessons = progresses.stream().filter(lp -> EnumLessonProgressStatus.COMPLETED.equals(lp.getStatus())).count();
        long allLessons = lessons.size();
        int percent = (allLessons == 0) ? 0 : (int)((completedLessons * 100) / allLessons);
        FullCourseProgressDto dto = new FullCourseProgressDto();
        dto.setCourseId(courseId);
        dto.setUserId(userId);
        dto.setCompletedPercentage(percent);
        dto.setTotalLessons(allLessons);
        dto.setCompletedLessons(completedLessons);
        return dto;
    }

}
