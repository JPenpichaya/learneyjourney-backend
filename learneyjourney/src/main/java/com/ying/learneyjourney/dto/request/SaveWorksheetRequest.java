package com.ying.learneyjourney.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record SaveWorksheetRequest(
        String userEmail,
        @NotNull UUID versionId,
        @NotBlank @Size(max = 20) String versionLabel,
        @NotBlank String htmlContent,
        Boolean setActive
) {
}
