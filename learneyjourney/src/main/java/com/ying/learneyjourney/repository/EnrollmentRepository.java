package com.ying.learneyjourney.repository;

import com.ying.learneyjourney.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID>, JpaSpecificationExecutor<Enrollment> {
    @Query("select e from Enrollment e where e.user.id = ?1")
    List<Enrollment> findBy_UserId(String id);

    @Query("select count(e) from Enrollment e where e.course.id = ?1")
    long countByCourseId(UUID id);
}
