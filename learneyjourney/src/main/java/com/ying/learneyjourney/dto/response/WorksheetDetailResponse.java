package com.ying.learneyjourney.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record WorksheetDetailResponse(
        UUID id,
        String title,
        String subject,
        String promptText,
        String language,
        String activeVersionLabel,
        int exportCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<VersionResponse> versions
) {
    public record VersionResponse(
            UUID id,
            String versionLabel,
            Integer sortOrder,
            String htmlContent,
            Integer exportCount,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}
}
