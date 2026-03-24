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
    WorksheetDetailResponse create(CreateWorksheetRequest request);
    PagedResponse<CourseInfoResponse.WorksheetSummaryResponse> list(String userEmail, String keyword, int page, int size);
    WorksheetDetailResponse getById(String userEmail, UUID id);
    WorksheetDetailResponse updateMeta(String userEmail, UUID id, UpdateWorksheetRequest request);
    WorksheetDetailResponse saveVersion(UUID worksheetId, SaveWorksheetRequest request);
    WorksheetDetailResponse duplicate(String userEmail, UUID id);
    void delete(String userEmail, UUID id);
    void export(ExportWorksheetRequest request);
}
