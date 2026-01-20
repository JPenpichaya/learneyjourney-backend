package com.ying.learneyjourney.repository;

import com.ying.learneyjourney.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
public interface CourseRepository extends JpaRepository<Course, UUID>, JpaSpecificationExecutor<Course>{
    @Query("select c from Course c where c.id in (?1)")
    List<Course> findByIn_courseId(List<UUID> id);

    @Query("select c from Course c where c.isShowOnProfile = true and c.tutorProfile.id = ?1 order by c.createdAt desc")
    List<Course> findBy_IsShowCourseOnProfile_True(UUID tutorProfileId);
}
