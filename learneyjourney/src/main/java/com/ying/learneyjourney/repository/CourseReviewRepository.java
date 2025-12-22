package com.ying.learneyjourney.repository;

import com.ying.learneyjourney.entity.CourseReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface CourseReviewRepository extends JpaRepository<CourseReview, UUID>, JpaSpecificationExecutor<CourseReview> {
    @Query(value = """
        SELECT COALESCE(AVG(cr.rating), 0)
        FROM course_review cr
        WHERE cr.course_id = :courseId
          AND cr.rating IS NOT NULL
        """, nativeQuery = true)
    Double getAverageRatingByCourseId(@Param("courseId") UUID courseId);
}
