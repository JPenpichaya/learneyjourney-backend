package com.ying.learneyjourney.service;

import com.ying.learneyjourney.entity.Worksheet;
import com.ying.learneyjourney.entity.WorksheetVersion;
import com.ying.learneyjourney.repository.WorksheetRepository;
import com.ying.learneyjourney.repository.WorksheetVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorksheetExportService {
    private final WorksheetRepository worksheetRepository;
    private final WorksheetVersionRepository worksheetVersionRepository;
    private final PdfGenerationService pdfGenerationService;

    public byte[] exportWorksheetPdf(UUID worksheetId, UUID versionId, String userId) {
        Worksheet worksheet = worksheetRepository.findById(worksheetId)
                .orElseThrow(() -> new IllegalArgumentException("Worksheet not found"));

        WorksheetVersion version = worksheetVersionRepository.findById(versionId)
                .orElseThrow(() -> new IllegalArgumentException("Worksheet version not found"));

        if (!version.getWorksheet().getId().equals(worksheet.getId())) {
            throw new IllegalArgumentException("Version does not belong to worksheet");
        }

        // TODO:
        // 1. validate ownership / permissions
        // 2. deduct credits / record export
        // 3. increment export count

        String html = buildPdfHtml(version.getHtmlContent());
        return pdfGenerationService.generatePdfFromHtml(html);
    }

    private String sanitizeForPdf(String html) {
        if (html == null) return "";

        return html
                .replace("&copy;", "&#169;")
                .replace("&reg;", "&#174;")
                .replace("&trade;", "&#8482;")
                .replace("&nbsp;", "&#160;");
    }

    private String buildPdfHtml(String content) {
        return """
                <html>
                  <head>
                    <meta charset="UTF-8" />
                    <style>
                      @page {
                        size: A4;
                        margin: 16mm;
                      }
                
                      body {
                        font-family: Arial, sans-serif;
                        font-size: 12pt;
                        color: #111;
                        line-height: 1.5;
                      }
                
                      img {
                        max-width: 100%%;
                        height: auto;
                      }
                
                      table {
                        width: 100%%;
                        border-collapse: collapse;
                      }
                
                      * {
                        box-sizing: border-box;
                      }
                    </style>
                  </head>
                  <body>
                    %s
                  </body>
                </html>
                """.formatted(content);
    }
}