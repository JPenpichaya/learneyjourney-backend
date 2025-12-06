package com.ying.learneyjourney.repository;

import com.ying.learneyjourney.entity.CourseLesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface CourseLessonRepository extends JpaRepository<CourseLesson, UUID>, JpaSpecificationExecutor<CourseLesson>{
}
