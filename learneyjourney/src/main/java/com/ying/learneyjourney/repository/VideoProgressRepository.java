package com.ying.learneyjourney.repository;

import com.ying.learneyjourney.constaint.EnumVideoProgressStatus;
import com.ying.learneyjourney.entity.VideoProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
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
                  vp.id                AS id,
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

    @Query(
            value = """
        SELECT * 
        FROM video_progress 
        WHERE user_id = :userId 
          AND status = :status 
        ORDER BY last_watched_at DESC 
        LIMIT 1
    """,
            nativeQuery = true
    )
    VideoProgress findFirstByUserIdAndStatusOrderByUpdatedAtDesc(
            @Param("userId") String userId,
            @Param("status") String status
    );

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO video_progress (id, user_id, course_video_id, status, watched_seconds, created_at, updated_at) " +
            "SELECT gen_random_uuid(), :userId, cv.id, 'NOT_START', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP " +
            "FROM course_video cv " +
            "JOIN course_lesson cl ON cv.lesson_id = cl.id " +
            "WHERE cl.course_id = :courseId " +
            "AND NOT EXISTS (SELECT 1 FROM video_progress vp WHERE vp.user_id = :userId AND vp.course_video_id = cv.id)", nativeQuery = true)
    void insertVideoProgressForCourse(@Param("userId") String userId, @Param("courseId") UUID courseId);

    @Query(value = """
        SELECT
            (SELECT COUNT(cv.id) FROM course_video cv WHERE cv.lesson_id = :lessonId)
            =
            (SELECT COUNT(vp.id)
             FROM video_progress vp
             JOIN course_video cv ON vp.course_video_id = cv.id
             WHERE cv.lesson_id = :lessonId
               AND vp.user_id = :userId
               AND vp.status = 'COMPLETED')
    """, nativeQuery = true)
    boolean areAllVideosInLessonCompleted(@Param("userId") String userId, @Param("lessonId") UUID lessonId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE video_progress SET last_watched_at = CURRENT_TIMESTAMP WHERE id = :videoProgressId", nativeQuery = true)
    void updateLastWatchedAt(@Param("videoProgressId") UUID videoProgressId);


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
