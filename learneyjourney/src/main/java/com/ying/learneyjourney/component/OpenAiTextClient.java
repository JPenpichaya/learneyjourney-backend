package com.ying.learneyjourney.component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ying.learneyjourney.config.OpenAiApiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OpenAiTextClient {

    private final RestClient.Builder restClientBuilder;
    private final OpenAiApiProperties properties;


    public String generateHtml(String prompt) {
        ChatCompletionRequest request = new ChatCompletionRequest(
                properties.getTextModel(),
                List.of(new Message("user", prompt)),
                0.4
        );

        RestClient restClient = restClientBuilder.build();

        ChatCompletionResponse response = restClient.post()
                .uri(properties.getBaseUrl()+"/v1/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(ChatCompletionResponse.class);

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new IllegalStateException("OpenAI text response is empty");
        }

        String content = response.choices().get(0).message().content();
        if (content == null || content.isBlank()) {
            throw new IllegalStateException("OpenAI text content is blank");
        }

        return content;
    }

    public record ChatCompletionRequest(
            String model,
            List<Message> messages,
            Double temperature
    ) {
    }

    public record Message(
            String role,
            String content
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChatCompletionResponse(
            List<Choice> choices
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Choice(
            AssistantMessage message
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AssistantMessage(
            String role,
            String content
    ) {
    }
}