package com.ying.learneyjourney.repository;

import com.ying.learneyjourney.entity.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, UUID>, JpaSpecificationExecutor<LessonProgress> {
}
