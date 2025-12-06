package com.ying.learneyjourney.repository;

import com.ying.learneyjourney.entity.CourseVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface CourseVideoRepository extends JpaRepository<CourseVideo, UUID>, JpaSpecificationExecutor<CourseVideo> {
}
