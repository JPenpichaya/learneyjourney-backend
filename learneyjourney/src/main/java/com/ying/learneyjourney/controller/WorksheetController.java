package com.ying.learneyjourney.controller;

import com.ying.learneyjourney.dto.request.CreateWorksheetRequest;
import com.ying.learneyjourney.dto.request.ExportWorksheetRequest;
import com.ying.learneyjourney.dto.request.SaveWorksheetRequest;
import com.ying.learneyjourney.dto.request.UpdateWorksheetRequest;
import com.ying.learneyjourney.dto.response.CourseInfoResponse;
import com.ying.learneyjourney.dto.response.PagedResponse;
import com.ying.learneyjourney.dto.response.WorksheetDetailResponse;
import com.ying.learneyjourney.service.WorksheetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/worksheets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WorksheetController {

    private final WorksheetService worksheetService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorksheetDetailResponse create(@Valid @RequestBody CreateWorksheetRequest request) {
        return worksheetService.create(request);
    }

    @GetMapping
    public PagedResponse<CourseInfoResponse.WorksheetSummaryResponse> list(
            @RequestParam String userEmail,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return worksheetService.list(userEmail, keyword, page, size);
    }

    @GetMapping("/{id}")
    public WorksheetDetailResponse getById(
            @PathVariable UUID id,
            @RequestParam String userEmail
    ) {
        return worksheetService.getById(userEmail, id);
    }

    @PatchMapping("/{id}")
    public WorksheetDetailResponse updateMeta(
            @PathVariable UUID id,
            @RequestParam String userEmail,
            @Valid @RequestBody UpdateWorksheetRequest request
    ) {
        return worksheetService.updateMeta(userEmail, id, request);
    }

    @PutMapping("/{id}/save")
    public WorksheetDetailResponse saveVersion(
            @PathVariable UUID id,
            @Valid @RequestBody SaveWorksheetRequest request
    ) {
        return worksheetService.saveVersion(id, request);
    }

    @PostMapping("/{id}/duplicate")
    public WorksheetDetailResponse duplicate(
            @PathVariable UUID id,
            @RequestParam String userEmail
    ) {
        return worksheetService.duplicate(userEmail, id);
    }

    @PostMapping("/export")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void export(@Valid @RequestBody ExportWorksheetRequest request) {
        worksheetService.export(request);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(
            @PathVariable UUID id,
            @RequestParam String userEmail
    ) {
        worksheetService.delete(userEmail, id);
        return Map.of("message", "Worksheet deleted successfully");
    }
}
