package com.ying.learneyjourney.mapper;

import com.ying.learneyjourney.dto.response.CourseInfoResponse;
import com.ying.learneyjourney.dto.response.WorksheetDetailResponse;
import com.ying.learneyjourney.entity.Worksheet;
import org.springframework.stereotype.Component;

@Component
public class WorksheetMapper {

    public CourseInfoResponse.WorksheetSummaryResponse toSummary(Worksheet worksheet) {
        return new CourseInfoResponse.WorksheetSummaryResponse(
                worksheet.getId(),
                worksheet.getTitle(),
                worksheet.getSubject(),
                worksheet.getLanguage(),
                worksheet.getActiveVersionLabel(),
                worksheet.getVersions().size(),
                worksheet.getExportCount(),
                worksheet.getCreatedAt(),
                worksheet.getUpdatedAt()
        );
    }

    public WorksheetDetailResponse toDetail(Worksheet worksheet) {
        return new WorksheetDetailResponse(
                worksheet.getId(),
                worksheet.getTitle(),
                worksheet.getSubject(),
                worksheet.getPromptText(),
                worksheet.getLanguage(),
                worksheet.getActiveVersionLabel(),
                worksheet.getExportCount(),
                worksheet.getCreatedAt(),
                worksheet.getUpdatedAt(),
                worksheet.getVersions().stream().map(v -> new WorksheetDetailResponse.VersionResponse(
                        v.getId(),
                        v.getVersionLabel(),
                        v.getSortOrder(),
                        v.getHtmlContent(),
                        v.getExportCount(),
                        v.getCreatedAt(),
                        v.getUpdatedAt()
                )).toList()
        );
    }
}
