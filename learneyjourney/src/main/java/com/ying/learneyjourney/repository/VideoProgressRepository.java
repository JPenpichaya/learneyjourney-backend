package com.ying.learneyjourney.repository;

import com.ying.learneyjourney.entity.VideoProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface VideoProgressRepository extends JpaRepository<VideoProgress, UUID>, JpaSpecificationExecutor<VideoProgress> {
}
