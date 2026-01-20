package com.ying.learneyjourney.dto.request;

import lombok.Data;

import java.util.UUID;
@Data
public class ImageUploadRequest {
    private String relatedEntityType;
    private UUID relatedEntityId;
    private String visibility;     // "PUBLIC" or "PRIVATE"
    private String description;
}
