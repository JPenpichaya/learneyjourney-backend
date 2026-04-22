package com.ying.learneyjourney.component;

import com.ying.learneyjourney.constaint.ImageKind;
import org.springframework.stereotype.Component;

@Component
public class ImagePlaceholderParser {

    public ParsedImagePlaceholder parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return new ParsedImagePlaceholder("", ImageKind.AUTO);
        }

        String[] parts = raw.split("\\|");
        String query = parts[0].trim();

        ImageKind kind = ImageKind.AUTO;
        if (parts.length > 1) {
            try {
                kind = ImageKind.valueOf(parts[1].trim().toUpperCase());
            } catch (Exception ignored) {
                kind = ImageKind.AUTO;
            }
        }

        return new ParsedImagePlaceholder(query, kind);
    }

    public record ParsedImagePlaceholder(String query, ImageKind kind) {}
}