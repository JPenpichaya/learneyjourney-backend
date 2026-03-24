package com.ying.learneyjourney.controller;

import com.ying.learneyjourney.dto.request.GenerateWorksheetRequest;
import com.ying.learneyjourney.dto.response.GenerateWorksheetResponse;
import com.ying.learneyjourney.service.WorksheetGenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai/worksheets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WorksheetGenerationController {

    private final WorksheetGenerationService worksheetGenerationService;

    @PostMapping("/generate")
    public GenerateWorksheetResponse generate(@Valid @RequestBody GenerateWorksheetRequest request) {
        return worksheetGenerationService.generateWorksheet(request);
    }
}
