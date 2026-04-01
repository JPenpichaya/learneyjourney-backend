package com.ying.learneyjourney.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record GenerateWorksheetRequest(
        @NotBlank String prompt,
        String outputLanguage,
        @NotBlank String title,
        @NotBlank String subject,
        String userEmail,
        UUID existingWorksheetId
) {
}
