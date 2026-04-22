//package com.ying.learneyjourney.service.worksheet;
//
//import com.ying.learneyjourney.component.*;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//@Slf4j
//@Service
//public class WorksheetImageRendererAi {
//
//    private static final Pattern IMAGE_TAG_PATTERN = Pattern.compile(
//            "<img([^>]*?)src=\"\\{\\{IMAGE:\\s*(.+?)\\s*}}\"([^>]*?)(?:/?>)",
//            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
//    );
//
//    private final ImageIntentClassifier classifier;
//    private final OpenAiSvgClient openAiSvgClient;
//    private final OpenAiImageClient openAiImageClient;
//    private final SvgSanitizer svgSanitizer;
//    private final SvgFallbackBuilder svgFallbackBuilder;
//
//    public WorksheetImageRendererAi(
//            ImageIntentClassifier classifier,
//            OpenAiSvgClient openAiSvgClient,
//            OpenAiImageClient openAiImageClient,
//            SvgSanitizer svgSanitizer,
//            SvgFallbackBuilder svgFallbackBuilder
//    ) {
//        this.classifier = classifier;
//        this.openAiSvgClient = openAiSvgClient;
//        this.openAiImageClient = openAiImageClient;
//        this.svgSanitizer = svgSanitizer;
//        this.svgFallbackBuilder = svgFallbackBuilder;
//    }
//
//    public String renderImages(String html) {
//        if (html == null || html.isBlank()) {
//            return html;
//        }
//
//        Matcher matcher = IMAGE_TAG_PATTERN.matcher(html);
//        StringBuffer sb = new StringBuffer();
//
//        while (matcher.find()) {
//            String beforeAttrs = matcher.group(1) == null ? "" : matcher.group(1);
//            String description = matcher.group(2) == null ? "" : matcher.group(2).trim();
//            String afterAttrs = matcher.group(3) == null ? "" : matcher.group(3);
//
//            String allAttrs = beforeAttrs + " " + afterAttrs;
//            String alt = extractAttribute(allAttrs, "alt");
//            String style = extractAttribute(allAttrs, "style");
//
//            String replacement = buildReplacement(description, alt, style);
//            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
//        }
//
//        matcher.appendTail(sb);
//        return sb.toString();
//    }
//
//    private String buildReplacement(String description, String alt, String style) {
//        String altText = (alt == null || alt.isBlank()) ? description : alt;
//
//        try {
////            if (classifier.shouldGenerateSvg(description)) {
////                log.info("Generating SVG for description: {}", description);
////
////                String rawSvg = openAiSvgClient.generateSvg(description);
////                log.info("Raw SVG response: {}", rawSvg);
////
////                String safeSvg = svgSanitizer.sanitize(rawSvg);
////                log.info("SVG sanitized successfully");
////
////                return wrapInlineSvg(safeSvg, style);
////            }
//
//            log.info("Generating PNG image for description: {}", description);
//            String dataUrl = openAiImageClient.generatePngDataUrl(description);
//            return buildImgTag(dataUrl, altText, style);
//
//        } catch (Exception e) {
//            log.error("Image rendering failed for description: {}", description, e);
//            String fallbackSvg = svgFallbackBuilder.build(description, altText);
//            return wrapInlineSvg(fallbackSvg, style);
//        }
//    }
//
//    private String wrapInlineSvg(String svg, String originalStyle) {
//        String wrapperStyle = normalizeWrapperStyle(originalStyle);
//        return """
//                <div style="%s">
//                  %s
//                </div>
//                """.formatted(wrapperStyle, svg);
//    }
//
//    private String buildImgTag(String src, String alt, String style) {
//        String safeAlt = escapeHtml(alt);
//        String finalStyle = normalizeImageStyle(style);
//
//        return """
//                <img src="%s" alt="%s" style="%s" />
//                """.formatted(src, safeAlt, finalStyle);
//    }
//
//    private String normalizeWrapperStyle(String originalStyle) {
//        String base = "display:block; margin:12px 0; text-align:center;";
//        if (originalStyle == null || originalStyle.isBlank()) {
//            return base;
//        }
//
//        String cleaned = originalStyle.trim();
//        if (!cleaned.endsWith(";")) {
//            cleaned += ";";
//        }
//        return base + " " + cleaned;
//    }
//
//    private String normalizeImageStyle(String originalStyle) {
//        String base = "max-width:100%; height:auto; display:block; margin:12px auto;";
//        if (originalStyle == null || originalStyle.isBlank()) {
//            return base;
//        }
//
//        String cleaned = originalStyle.trim();
//        if (!cleaned.endsWith(";")) {
//            cleaned += ";";
//        }
//        return base + " " + cleaned;
//    }
//
//    private String extractAttribute(String input, String attributeName) {
//        if (input == null || input.isBlank()) {
//            return null;
//        }
//
//        Pattern pattern = Pattern.compile(attributeName + "\\s*=\\s*\"([^\"]*)\"", Pattern.CASE_INSENSITIVE);
//        Matcher matcher = pattern.matcher(input);
//        return matcher.find() ? matcher.group(1) : null;
//    }
//
//    private String escapeHtml(String input) {
//        if (input == null) {
//            return "";
//        }
//
//        return input
//                .replace("&", "&amp;")
//                .replace("\"", "&quot;")
//                .replace("<", "&lt;")
//                .replace(">", "&gt;");
//    }
//}