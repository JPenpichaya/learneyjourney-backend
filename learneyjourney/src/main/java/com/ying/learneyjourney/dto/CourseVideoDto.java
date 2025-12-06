package com.ying.learneyjourney.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class CourseVideoDto implements Serializable {
    private UUID id;
    private String title;
    private String url;
    private Integer duration;
    private Integer position;

    public static CourseVideoDto from(com.ying.learneyjourney.entity.CourseVideo video) {
        CourseVideoDto dto = new CourseVideoDto();
        dto.setId(video.getId());
        dto.setTitle(video.getTitle());
        dto.setUrl(video.getUrl());
        dto.setDuration(video.getDuration());
        dto.setPosition(video.getPosition());
        return dto;
    }

    public static com.ying.learneyjourney.entity.CourseVideo toEntity(CourseVideoDto dto) {
        com.ying.learneyjourney.entity.CourseVideo video = new com.ying.learneyjourney.entity.CourseVideo();
        video.setTitle(dto.getTitle());
        video.setUrl(dto.getUrl());
        video.setDuration(dto.getDuration());
        video.setPosition(dto.getPosition());
        return video;
    }
}
