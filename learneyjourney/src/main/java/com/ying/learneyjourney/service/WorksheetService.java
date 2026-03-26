package com.ying.learneyjourney.service;

import com.ying.learneyjourney.dto.request.CreateWorksheetRequest;
import com.ying.learneyjourney.dto.request.ExportWorksheetRequest;
import com.ying.learneyjourney.dto.request.SaveWorksheetRequest;
import com.ying.learneyjourney.dto.request.UpdateWorksheetRequest;
import com.ying.learneyjourney.dto.response.CourseInfoResponse;
import com.ying.learneyjourney.dto.response.PagedResponse;
import com.ying.learneyjourney.dto.response.WorksheetDetailResponse;

import java.util.UUID;

public interface WorksheetService {
    WorksheetDetailResponse create(CreateWorksheetRequest request, String userId);
    PagedResponse<CourseInfoResponse.WorksheetSummaryResponse> list(String userId, String keyword, int page, int size);
    WorksheetDetailResponse getById(String userId, UUID id);
    WorksheetDetailResponse updateMeta(String userId, UUID id, UpdateWorksheetRequest request);
    WorksheetDetailResponse saveVersion(UUID worksheetId, SaveWorksheetRequest request, String userId);
    WorksheetDetailResponse duplicate(String userId, UUID id);
    void delete(String userId, UUID id);
    void export(ExportWorksheetRequest request, String userId);
}
