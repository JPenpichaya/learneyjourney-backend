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

    @Query(value = """
                SELECT
                  cv.id                AS id,
                  cl.description       AS description,
                  vp.status            AS status,
                  vp.watched_seconds   AS completedAt,
                  cv.title             AS title,
                  cv.url               AS url,
                  cv.duration          AS duration,
                  cv.position          AS position
                FROM video_progress vp
                JOIN course_video cv   ON cv.id = vp.course_video_id
                JOIN course_lesson cl  ON cl.id = cv.lesson_id
                WHERE vp.user_id = :userId
                  AND cl.id = :lessonId
                ORDER BY cv.position ASC
            """, nativeQuery = true)
    List<VideoProgressRow> findVideoProgressRows(@Param("userId") String userId,
                                                 @Param("lessonId") UUID lessonId);

    public interface VideoProgressRow {
        UUID getId();

        String getDescription();

        String getStatus();       // or EnumVideoProgressStatus if mapping works

        Integer getCompletedAt(); // watched_seconds

        String getTitle();

        String getUrl();

        Integer getDuration();

        Integer getPosition();
    }
}
