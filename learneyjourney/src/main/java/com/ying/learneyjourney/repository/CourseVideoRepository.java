package com.ying.learneyjourney.repository;

import com.ying.learneyjourney.entity.CourseVideo;
import com.ying.learneyjourney.entity.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CourseVideoRepository extends JpaRepository<CourseVideo, UUID>, JpaSpecificationExecutor<CourseVideo> {
    @Query("select c from CourseVideo c where c.courseLesson.id = ?1")
    List<CourseVideo> findByLessonId(UUID id);

    @Query(
            value = "SELECT * FROM course_video WHERE lesson_id IN (:ids) ORDER BY position ASC",
            nativeQuery = true
    )
    List<CourseVideo> findByLessonIds(
            @Param("ids") List<UUID> ids
    );

    @Query(value = """
        SELECT
          COALESCE(SUM(cv.duration), 0) AS totalSeconds,

          CASE
            WHEN COALESCE(SUM(cv.duration), 0) >= 604800 THEN ROUND(COALESCE(SUM(cv.duration), 0) / 604800.0, 1)
            WHEN COALESCE(SUM(cv.duration), 0) >= 86400  THEN ROUND(COALESCE(SUM(cv.duration), 0) / 86400.0, 1)
            ELSE ROUND(COALESCE(SUM(cv.duration), 0) / 3600.0, 1)
          END AS displayValue,

          CASE
            WHEN COALESCE(SUM(cv.duration), 0) >= 604800 THEN 'weeks'
            WHEN COALESCE(SUM(cv.duration), 0) >= 86400  THEN 'days'
            ELSE 'hours'
          END AS displayUnit,

          CASE
            WHEN COALESCE(SUM(cv.duration), 0) >= 604800 THEN (ROUND(COALESCE(SUM(cv.duration), 0) / 604800.0, 1)::text || ' weeks')
            WHEN COALESCE(SUM(cv.duration), 0) >= 86400  THEN (ROUND(COALESCE(SUM(cv.duration), 0) / 86400.0, 1)::text  || ' days')
            ELSE (ROUND(COALESCE(SUM(cv.duration), 0) / 3600.0, 1)::text || ' hours')
          END AS displayText

        FROM course_video cv
        JOIN course_lesson cl ON cl.id = cv.lesson_id
        WHERE cl.course_id = :courseId
        """, nativeQuery = true)
    DurationDisplayRow getCourseDurationDisplay(@Param("courseId") UUID courseId);

    public interface DurationDisplayRow {
        Long getTotalSeconds();
        Double getDisplayValue();
        String getDisplayUnit();
        String getDisplayText(); // optional but convenient
    }
}
