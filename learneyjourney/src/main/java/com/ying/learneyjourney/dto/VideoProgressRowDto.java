package com.ying.learneyjourney.dto;

import com.ying.learneyjourney.constaint.EnumVideoProgressStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class VideoProgressRowDto {
    private UUID id;
    private String description;
    private EnumVideoProgressStatus status;
    private Integer completedAt;
    private String title;
    private String url;
    private String duration;
    private Integer position;
    private String worksheet;
    private String contact;
}
