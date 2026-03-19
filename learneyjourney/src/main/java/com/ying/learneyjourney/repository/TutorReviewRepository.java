package com.ying.learneyjourney.repository;

import com.ying.learneyjourney.entity.TutorReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TutorReviewRepository extends JpaRepository<TutorReview, UUID>, JpaSpecificationExecutor<TutorReview> {
    @Query("select t from TutorReview t where t.tutor.id = ?1")
    List<TutorReview> findListByTutorId(UUID id);

    @Query(value = """
        SELECT COALESCE(AVG(cr.rating), 0)
        FROM tutor_review cr
        WHERE cr.tutor_id = :courseId
          AND cr.rating IS NOT NULL
        """, nativeQuery = true)
    Double getAverageRatingByTutorId(@Param("tutorId") UUID tutorId);
}
