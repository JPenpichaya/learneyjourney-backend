package com.ying.learneyjourney.repository;

import com.ying.learneyjourney.entity.CourseLesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
public interface CourseLessonRepository extends JpaRepository<CourseLesson, UUID>, JpaSpecificationExecutor<CourseLesson>{
    @Query("select c from CourseLesson c where c.course.id = ?1 order by c.position ASC")
    List<CourseLesson> findByCourse(UUID id);

    @Query("select count(c) from CourseLesson c where c.course.id = ?1")
    long countByCourseId(UUID id);
}
