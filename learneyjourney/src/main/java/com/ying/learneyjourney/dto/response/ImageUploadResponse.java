package com.ying.learneyjourney.dto.response;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
public class ImageUploadResponse implements Serializable {
    private UUID assetId;
    private String originalUrl;
    private String thumbnailUrl;
}
