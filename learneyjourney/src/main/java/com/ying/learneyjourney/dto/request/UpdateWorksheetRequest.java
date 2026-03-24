package com.ying.learneyjourney.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateWorksheetRequest(
        @Size(max = 255) String title,
        @Size(max = 100) String subject,
        @Size(max = 20) String language,
        @Size(max = 20) String activeVersionLabel
) {
}