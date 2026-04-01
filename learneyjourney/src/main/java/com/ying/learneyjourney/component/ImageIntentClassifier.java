package com.ying.learneyjourney.component;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ImageIntentClassifier {

    private static final Set<String> DIAGRAM_WORDS = Set.of(
            "diagram", "label", "labeled", "worksheet", "educational",
            "chart", "graph", "timeline", "map", "fraction", "geometry",
            "shape", "number line", "life cycle", "parts of", "process",
            "orbit", "solar system", "water cycle", "plant", "science diagram"
    );

    public boolean shouldGenerateSvg(String description) {
        String text = normalize(description);
        return DIAGRAM_WORDS.stream().anyMatch(text::contains);
    }

    private String normalize(String input) {
        return input == null ? "" : input.toLowerCase().trim();
    }
}