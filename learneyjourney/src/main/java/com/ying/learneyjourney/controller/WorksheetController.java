package com.ying.learneyjourney.controller;

import com.ying.learneyjourney.Util.FirebaseAuthUtil;
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
import org.springframework.http.HttpHeaders;
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
    private final FirebaseAuthUtil firebaseAuthUtil;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorksheetDetailResponse create(@Valid @RequestBody CreateWorksheetRequest request,
                                          @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) throws Exception {
        String userId = firebaseAuthUtil.getUserIdFromToken(authHeader);
        return worksheetService.create(request, userId);
    }

    @GetMapping
    public PagedResponse<CourseInfoResponse.WorksheetSummaryResponse> list(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) throws Exception {
        String userId = firebaseAuthUtil.getUserIdFromToken(authHeader);
        return worksheetService.list(userId, keyword, page, size);
    }

    @GetMapping("/{id}")
    public WorksheetDetailResponse getById(
            @PathVariable UUID id,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader
    ) throws Exception {
        String userId = firebaseAuthUtil.getUserIdFromToken(authHeader);
        return worksheetService.getById(userId, id);
    }

    @PatchMapping("/{id}")
    public WorksheetDetailResponse updateMeta(
            @PathVariable UUID id,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader,
            @Valid @RequestBody UpdateWorksheetRequest request
    ) throws Exception {
        String userId = firebaseAuthUtil.getUserIdFromToken(authHeader);
        return worksheetService.updateMeta(userId, id, request);
    }

    @PutMapping("/{id}/save")
    public WorksheetDetailResponse saveVersion(
            @PathVariable UUID id,
            @Valid @RequestBody SaveWorksheetRequest request,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader
    ) throws Exception {
        String userId = firebaseAuthUtil.getUserIdFromToken(authHeader);
        return worksheetService.saveVersion(id, request, userId);
    }

    @PostMapping("/{id}/duplicate")
    public WorksheetDetailResponse duplicate(
            @PathVariable UUID id,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader
    ) throws Exception {
        String userId = firebaseAuthUtil.getUserIdFromToken(authHeader);
        return worksheetService.duplicate(userId, id);
    }

    @PostMapping("/export")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void export(@Valid @RequestBody ExportWorksheetRequest request,
                       @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) throws Exception {
        String userId = firebaseAuthUtil.getUserIdFromToken(authHeader);
        worksheetService.export(request, userId);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(
            @PathVariable UUID id,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader
    ) throws Exception {
        String userId = firebaseAuthUtil.getUserIdFromToken(authHeader);
        worksheetService.delete(userId, id);
        return Map.of("message", "Worksheet deleted successfully");
    }
}
