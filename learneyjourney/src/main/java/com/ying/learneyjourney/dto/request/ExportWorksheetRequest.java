package com.ying.learneyjourney.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ExportWorksheetRequest(
        @NotBlank String userEmail,
        @NotNull UUID worksheetId,
        @NotNull UUID versionId
) {
}