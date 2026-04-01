package com.ying.learneyjourney.component;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.regex.Pattern;

@Component
public class SvgSanitizer {

    private static final Set<String> FORBIDDEN_TAGS = Set.of(
            "script", "foreignObject", "style", "image", "use", "animate",
            "set", "animateTransform", "animateMotion", "iframe", "object", "embed"
    );

    private static final Pattern EVENT_HANDLER_PATTERN =
            Pattern.compile("\\son[a-zA-Z]+\\s*=\\s*\"[^\"]*\"", Pattern.CASE_INSENSITIVE);

    private static final Pattern JS_HREF_PATTERN =
            Pattern.compile("(href|xlink:href)\\s*=\\s*\"\\s*javascript:[^\"]*\"", Pattern.CASE_INSENSITIVE);

    public String sanitize(String rawSvg) {
        if (rawSvg == null || rawSvg.isBlank()) {
            throw new IllegalArgumentException("SVG is blank");
        }

        String svg = rawSvg.trim();

        if (!svg.startsWith("<svg") || !svg.endsWith("</svg>")) {
            throw new IllegalArgumentException("Invalid SVG root");
        }

        String lower = svg.toLowerCase();
        for (String tag : FORBIDDEN_TAGS) {
            if (lower.contains("<" + tag) || lower.contains("</" + tag)) {
                throw new IllegalArgumentException("Forbidden SVG tag: " + tag);
            }
        }

        svg = EVENT_HANDLER_PATTERN.matcher(svg).replaceAll("");
        svg = JS_HREF_PATTERN.matcher(svg).replaceAll("");

        return svg;
    }
}