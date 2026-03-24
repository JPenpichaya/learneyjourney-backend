package com.ying.learneyjourney.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GenerateWorksheetRequest(
        @NotBlank String prompt,
        String outputLanguage,
        @NotBlank String title,
        @NotBlank String subject,
        @NotBlank String userEmail
) {
}
