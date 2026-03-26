package com.ying.learneyjourney.service;

import com.ying.learneyjourney.dto.request.GenerateWorksheetRequest;
import com.ying.learneyjourney.dto.response.GenerateWorksheetResponse;

public interface WorksheetGenerationService {
    GenerateWorksheetResponse generateWorksheet(GenerateWorksheetRequest request, String userId);
}