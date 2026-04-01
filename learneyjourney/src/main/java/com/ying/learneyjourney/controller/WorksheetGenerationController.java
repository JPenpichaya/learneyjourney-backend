package com.ying.learneyjourney.controller;

import com.ying.learneyjourney.Util.FirebaseAuthUtil;
import com.ying.learneyjourney.dto.request.GenerateWorksheetRequest;
import com.ying.learneyjourney.dto.response.GenerateWorksheetResponse;
import com.ying.learneyjourney.service.WorksheetGenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai/worksheets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WorksheetGenerationController {

    private final WorksheetGenerationService worksheetGenerationService;
    private final FirebaseAuthUtil firebaseAuthUtil;

    @PostMapping("/generate")
    public GenerateWorksheetResponse generate(@Valid @RequestBody GenerateWorksheetRequest request,
                                              @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) throws Exception {
        String userId = firebaseAuthUtil.getUserIdFromToken(authHeader);
        return worksheetGenerationService.generate(request, userId);
    }

//    @PostMapping(value = "/generate/html", produces = MediaType.TEXT_HTML_VALUE)
//    public String generateHtml(@Valid @RequestBody GenerateWorksheetRequest request,
//                               @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
//        String userId = firebaseAuthUtil.getUserIdFromToken(authHeader);
//        return worksheetGenerationService.generate(request, userId).getHtml();
//    }
}
