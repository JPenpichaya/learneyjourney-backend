package com.ying.learneyjourney.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import org.w3c.dom.Document;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class PdfGenerationService {

    public byte[] generatePdfFromHtml(String html) {
        try {
            String cleanedHtml = preprocessHtml(html);

            org.jsoup.nodes.Document jsoupDoc = Jsoup.parse(cleanedHtml);
            jsoupDoc.outputSettings()
                    .syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml)
                    .escapeMode(org.jsoup.nodes.Entities.EscapeMode.xhtml)
                    .charset(StandardCharsets.UTF_8)
                    .prettyPrint(false);

            removeUnsafeNodes(jsoupDoc);
            normalizeElements(jsoupDoc);

            Document w3cDoc = new W3CDom().fromJsoup(jsoupDoc);

            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withW3cDocument(w3cDoc, null);
                builder.toStream(os);
                builder.run();
                return os.toByteArray();
            }
        } catch (Exception e) {
            log.error("PDF generation failed", e);
            logBrokenHtml(html);
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private String preprocessHtml(String html) {
        if (html == null || html.isBlank()) {
            return """
                    <html>
                      <body>
                        <p>No content available.</p>
                      </body>
                    </html>
                    """;
        }

        return html
                .replace("&nbsp;", "&#160;")
                .replace("&copy;", "&#169;")
                .replace("&reg;", "&#174;")
                .replace("&trade;", "&#8482;")
                .replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "");
    }

    private void removeUnsafeNodes(org.jsoup.nodes.Document doc) {
        doc.select("script, iframe, object, embed").remove();
    }

    private void normalizeElements(org.jsoup.nodes.Document doc) {
        Elements imgs = doc.select("img");
        imgs.forEach(img -> {
            String style = img.attr("style");
            if (!style.contains("max-width")) {
                style = style + (style.isBlank() ? "" : ";") + "max-width:100%;height:auto;";
                img.attr("style", style);
            }
        });

        Elements tables = doc.select("table");
        tables.forEach(table -> {
            String style = table.attr("style");
            if (!style.contains("width")) {
                style = style + (style.isBlank() ? "" : ";") + "width:100%;border-collapse:collapse;table-layout:fixed;";
                table.attr("style", style);
            }
        });
    }

    private void logBrokenHtml(String html) {
        if (html == null) {
            log.error("HTML content is null");
            return;
        }

        String[] lines = html.split("\\R");
        for (int i = 0; i < lines.length; i++) {
            log.error("HTML line {}: {}", i + 1, lines[i]);
        }
    }

}