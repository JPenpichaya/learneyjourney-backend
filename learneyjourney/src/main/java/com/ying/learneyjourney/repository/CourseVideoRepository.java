package com.ying.learneyjourney.repository;

import com.ying.learneyjourney.entity.CourseVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface CourseVideoRepository extends JpaRepository<CourseVideo, UUID>, JpaSpecificationExecutor<CourseVideo> {
    @Query("select c from CourseVideo c where c.courseLesson.id = ?1")
    List<CourseVideo> findByLessonId(UUID id);
}
