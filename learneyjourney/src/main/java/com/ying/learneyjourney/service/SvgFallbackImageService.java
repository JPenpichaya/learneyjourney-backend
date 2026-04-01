package com.ying.learneyjourney.service;

import com.ying.learneyjourney.dto.ResolvedImage;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
public class SvgFallbackImageService {

    public Optional<ResolvedImage> resolve(String description) {
        String svg = """
                <svg xmlns="http://www.w3.org/2000/svg" width="600" height="320" viewBox="0 0 600 320">
                  <rect x="1" y="1" width="598" height="318" fill="white" stroke="#444" stroke-width="2" rx="12"/>
                  <text x="300" y="42" text-anchor="middle" font-size="22" font-family="Arial" font-weight="bold">Worksheet Image</text>
                  <rect x="40" y="70" width="520" height="150" rx="8" fill="#f8f8f8" stroke="#999" stroke-dasharray="6 4"/>
                  <text x="300" y="255" text-anchor="middle" font-size="16" font-family="Arial">%s</text>
                </svg>
                """.formatted(escapeXml(description));

        String dataUrl = "data:image/svg+xml;charset=UTF-8," +
                URLEncoder.encode(svg, StandardCharsets.UTF_8).replace("+", "%20");

        return Optional.of(new ResolvedImage(dataUrl, description, "svg-fallback", true));
    }

    private String escapeXml(String input) {
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}