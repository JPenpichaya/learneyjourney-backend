package com.ying.learneyjourney.component;

import org.springframework.stereotype.Component;

@Component
public class SvgFallbackBuilder {

    public String build(String description, String altText) {
        String safeDescription = escapeXml(description);
        String safeAlt = escapeXml(altText);

        return """
                <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="600"
                    height="220"
                    viewBox="0 0 600 220"
                    role="img"
                    aria-label="%s"
                    style="max-width:100%%; height:auto; display:inline-block; background:white;"
                >
                  <rect x="1" y="1" width="598" height="218" fill="white" stroke="#444" stroke-width="2" rx="12"/>
                  <text x="300" y="40" text-anchor="middle" font-size="22" font-family="Arial, sans-serif" font-weight="bold" fill="#222">
                    Worksheet Image
                  </text>
                  <rect x="30" y="70" width="540" height="90" fill="#f8f8f8" stroke="#999" stroke-dasharray="6 4" rx="8"/>
                  <text x="300" y="125" text-anchor="middle" font-size="16" font-family="Arial, sans-serif" fill="#222">
                    %s
                  </text>
                </svg>
                """.formatted(safeAlt, safeDescription);
    }

    private String escapeXml(String input) {
        if (input == null) {
            return "";
        }

        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}