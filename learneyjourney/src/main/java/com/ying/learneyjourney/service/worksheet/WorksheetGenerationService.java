package com.ying.learneyjourney.service.worksheet;

import com.ying.learneyjourney.dto.request.GenerateWorksheetRequest;
import com.ying.learneyjourney.dto.response.GenerateWorksheetResponse;

public interface WorksheetGenerationService {
    GenerateWorksheetResponse generate(GenerateWorksheetRequest request, String userId);
}