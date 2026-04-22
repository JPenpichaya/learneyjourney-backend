package com.ying.learneyjourney.controller;

import com.ying.learneyjourney.service.worksheet.WorksheetExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/worksheets")
public class WorksheetExportController {

    private final WorksheetExportService worksheetExportService;

    @PostMapping("/{worksheetId}/export/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @PathVariable UUID worksheetId,
            @RequestBody ExportPdfRequest request
    ) {
        String userId = "current-user-id"; // replace with actual auth principal

        byte[] pdfBytes = worksheetExportService.exportWorksheetPdf(
                worksheetId,
                request.getVersionId(),
                userId
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("worksheet.pdf")
                        .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    public static class ExportPdfRequest {
        private UUID versionId;

        public UUID getVersionId() {
            return versionId;
        }

        public void setVersionId(UUID versionId) {
            this.versionId = versionId;
        }
    }
}