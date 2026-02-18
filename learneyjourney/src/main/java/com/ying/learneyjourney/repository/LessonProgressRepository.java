package com.ying.learneyjourney.repository;

import com.ying.learneyjourney.entity.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
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

    @Query("SELECT COUNT(DISTINCT lp.courseLesson.course.id) " +
            "FROM LessonProgress lp " +
            "WHERE lp.user.id = :userId " +
            "AND lp.courseLesson.course.id NOT IN (" +
            "    SELECT lp2.courseLesson.course.id " +
            "    FROM LessonProgress lp2 " +
            "    WHERE lp2.user.id = :userId " +
            "    GROUP BY lp2.courseLesson.course.id " +
            "    HAVING COUNT(lp2.id) = (" +
            "        SELECT COUNT(cl.id) " +
            "        FROM CourseLesson cl " +
            "        WHERE cl.course.id = lp2.courseLesson.course.id" +
            "    ) AND SUM(CASE WHEN lp2.status = 'COMPLETED' THEN 1 ELSE 0 END) = COUNT(lp2.id)" +
            ")")
    long countInProgressCoursesByUserId(@Param("userId") String userId);

    @Query("SELECT COUNT(DISTINCT lp.courseLesson.course.id) " +
            "FROM LessonProgress lp " +
            "WHERE lp.user.id = :userId " +
            "AND lp.courseLesson.course.id IN (" +
            "    SELECT lp2.courseLesson.course.id " +
            "    FROM LessonProgress lp2 " +
            "    WHERE lp2.user.id = :userId " +
            "    GROUP BY lp2.courseLesson.course.id " +
            "    HAVING COUNT(lp2.id) = (" +
            "        SELECT COUNT(cl.id) " +
            "        FROM CourseLesson cl " +
            "        WHERE cl.course.id = lp2.courseLesson.course.id" +
            "    ) AND SUM(CASE WHEN lp2.status = 'COMPLETED' THEN 1 ELSE 0 END) = COUNT(lp2.id)" +
            ")")
    long countCompletedCoursesByUserId(@Param("userId") String userId);

    @Query("SELECT COUNT(DISTINCT lp.courseLesson.course.id) FROM LessonProgress lp WHERE lp.user.id = :userId")
    long countAllCoursesByUserId(@Param("userId") String userId);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO lesson_progress (id, user_id, course_lesson_id, status) " +
            "SELECT gen_random_uuid(), :userId, cl.id, 'NOT_START' " +
            "FROM course_lesson cl " +
            "WHERE cl.course_id = :courseId " +
            "AND NOT EXISTS (SELECT 1 FROM lesson_progress lp WHERE lp.user_id = :userId AND lp.course_lesson_id = cl.id)", nativeQuery = true)
    void insertLessonProgressForCourse(@Param("userId") String userId, @Param("courseId") UUID courseId);
}
