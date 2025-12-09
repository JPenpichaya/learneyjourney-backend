package com.ying.learneyjourney.dto;

import com.ying.learneyjourney.constaint.EnumVideoProgressStatus;
import com.ying.learneyjourney.entity.CourseVideo;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
@Data
public class VideoProgressDto implements Serializable {
    private UUID id;
    private String userId;

    private UUID courseVideoId;

    private Integer watchedSeconds;

    private EnumVideoProgressStatus status;

    private LocalDateTime lastWatchedAt;

    public static VideoProgressDto from(com.ying.learneyjourney.entity.VideoProgress vp) {
        VideoProgressDto dto = new VideoProgressDto();
        dto.setId(vp.getId());
        dto.setUserId(vp.getUser().getId());
        dto.setCourseVideoId(vp.getCourseVideo().getId());
        dto.setWatchedSeconds(vp.getWatchedSeconds());
        dto.setStatus(vp.getStatus());
        dto.setLastWatchedAt(vp.getLastWatchedAt());
        return dto;
    }

    public static com.ying.learneyjourney.entity.VideoProgress toEntity(VideoProgressDto dto) {
        com.ying.learneyjourney.entity.VideoProgress vp = new com.ying.learneyjourney.entity.VideoProgress();
        vp.setWatchedSeconds(dto.getWatchedSeconds());
        vp.setStatus(dto.getStatus());
        vp.setLastWatchedAt(dto.getLastWatchedAt());
        return vp;
    }

}
