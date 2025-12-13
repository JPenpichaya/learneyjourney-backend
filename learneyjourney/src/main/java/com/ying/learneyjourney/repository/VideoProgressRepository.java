package com.ying.learneyjourney.repository;

import com.ying.learneyjourney.entity.VideoProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface VideoProgressRepository extends JpaRepository<VideoProgress, UUID>, JpaSpecificationExecutor<VideoProgress> {
    @Query(
            value = "SELECT * FROM video_progress WHERE user_id = :userId AND course_video_id IN (:videoIds)",
            nativeQuery = true
    )
    List<VideoProgress> findByUserIdAndVideoIdIn(
            @Param("userId") String userId,
            @Param("videoIds") List<UUID> videoIds
    );
}
