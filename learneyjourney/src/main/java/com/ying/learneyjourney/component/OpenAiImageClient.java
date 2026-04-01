package com.ying.learneyjourney.component;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ying.learneyjourney.config.OpenAiApiProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
public class OpenAiImageClient {

    private final RestClient restClient;
    private final OpenAiApiProperties properties;

    public OpenAiImageClient(RestClient restClient, OpenAiApiProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public String generatePngDataUrl(String description) {
        ImageGenerateRequest request = new ImageGenerateRequest(
                properties.getImageModel(),
                buildPrompt(description),
                1,
                "1024x1024",
                "png",
                "low",
                "opaque"
        );

        ImageGenerateResponse response = restClient.post()
                .uri(properties.getBaseUrl()+"/v1/images/generations")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(ImageGenerateResponse.class);

        if (response == null || response.data() == null || response.data().isEmpty()) {
            throw new IllegalStateException("OpenAI image response is empty");
        }

        String b64 = response.data().get(0).b64Json();
        if (b64 == null || b64.isBlank()) {
            throw new IllegalStateException("OpenAI image base64 is blank");
        }

        return "data:image/png;base64," + b64;
    }

    private String buildPrompt(String description) {
        return """
                Create a clean worksheet-friendly educational image.
                Style: simple, readable, child-safe, uncluttered.
                If the prompt implies a diagram, keep it diagram-like.
                If the prompt implies an illustration, make it visually clear but not overly decorative.
                Description: %s
                """.formatted(description);
    }

    public record ImageGenerateRequest(
            String model,
            String prompt,
            Integer n,
            String size,
            String output_format,
            String quality,
            String background
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ImageGenerateResponse(
            List<ImageData> data
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ImageData(
            @JsonProperty("b64_json") String b64Json
    ) {
    }
}