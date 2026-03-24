package com.ying.learneyjourney.dto.response;

import java.util.UUID;

public record GenerateWorksheetResponse(
        UUID worksheetId,
        String html
) {
}
