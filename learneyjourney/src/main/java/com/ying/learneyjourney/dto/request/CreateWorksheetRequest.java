package com.ying.learneyjourney.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateWorksheetRequest(
        @NotBlank String userEmail,
        @NotBlank @Size(max = 255) String title,
        @NotBlank @Size(max = 100) String subject,
        @NotBlank String promptText,
        @NotBlank @Size(max = 20) String language,
        @NotEmpty List<VersionPayload> versions,
        String activeVersionLabel
) {
    public record VersionPayload(
            @NotBlank @Size(max = 20) String versionLabel,
            Integer sortOrder,
            @NotBlank String htmlContent
    ) {}
}
