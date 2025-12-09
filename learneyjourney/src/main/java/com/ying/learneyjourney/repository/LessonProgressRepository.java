package com.ying.learneyjourney.repository;

import com.ying.learneyjourney.entity.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, UUID>, JpaSpecificationExecutor<LessonProgress> {
    @Query("select l from LessonProgress l where l.courseLesson.id = ?1")
    List<LessonProgress> findByCourseLessonId(UUID id);

    @Query(
            value = "SELECT * FROM lesson_progress WHERE course_lesson_id IN (:ids) AND user_id = :userId",
            nativeQuery = true
    )
    List<LessonProgress> findByCourseLessonIdInAndUserId(
            @Param("ids") List<UUID> ids,
            @Param("userId") String userId
    );
}
