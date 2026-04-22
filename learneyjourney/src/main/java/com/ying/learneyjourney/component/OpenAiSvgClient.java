package com.ying.learneyjourney.component;

import com.ying.learneyjourney.config.OpenAiApiProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OpenAiSvgClient {

    private final RestClient.Builder restClientBuilder;
    private final OpenAiApiProperties properties;


    public String generateSvg(String description) {
        String prompt = """
                You are an expert educational diagram generator.

                Create a clean worksheet-friendly SVG diagram.

                RULES:
                - Return valid SVG only
                - Do not return markdown
                - Do not return explanations
                - Do not include code fences
                - Use only safe SVG elements: svg, g, rect, circle, ellipse, line, path, polygon, polyline, text
                - Do not include script, foreignObject, style tags, defs, pattern, mask, image, use, animate, or external references
                - Set width="600" height="320" viewBox="0 0 600 320"
                - Use a white background
                - Make the diagram clear, readable, and suitable for students
                - Add labels where helpful
                - Keep the design simple and educational

                Create an SVG for:
                %s
                """.formatted(description);

        RestClient restClient = restClientBuilder.build();

        OpenAiTextClient.ChatCompletionRequest request =
                new OpenAiTextClient.ChatCompletionRequest(
                        properties.getTextModel(),
                        List.of(new OpenAiTextClient.Message("user", prompt)),
                        0.2
                );

        OpenAiTextClient.ChatCompletionResponse response = restClient.post()
                .uri(properties.getBaseUrl()+"/v1/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(OpenAiTextClient.ChatCompletionResponse.class);

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new IllegalStateException("OpenAI SVG response is empty");
        }

        String svg = response.choices().get(0).message().content();
        if (svg == null || svg.isBlank()) {
            throw new IllegalStateException("OpenAI SVG content is blank");
        }

        return svg.trim();
    }
}