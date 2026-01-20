package com.ying.learneyjourney.repository;

import com.ying.learneyjourney.entity.TutorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TutorProfileRepository extends JpaRepository<TutorProfile, UUID>, JpaSpecificationExecutor<TutorProfile> {
    @Query("select t from TutorProfile t where t.user.id = ?1")
    Optional<TutorProfile> findByUserId(String id);

    @Query(value = """
                SELECT tp.*
                FROM purchase p
                JOIN course c ON c.id = CAST(p.course_id AS uuid)
                JOIN tutor_profile tp ON tp.id = c.tutor_profile_id
                WHERE p.id = :purchaseId
            """, nativeQuery = true)
    Optional<TutorProfile> findTutorProfileByPurchaseId(@Param("purchaseId") UUID purchaseId);

}
