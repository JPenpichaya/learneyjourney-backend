package com.ying.learneyjourney.service;

import com.ying.learneyjourney.config.OpenAiApiProperties;
import com.ying.learneyjourney.constaint.PlanType;
import com.ying.learneyjourney.dto.request.CreateWorksheetRequest;
import com.ying.learneyjourney.dto.request.GenerateWorksheetRequest;
import com.ying.learneyjourney.dto.request.OpenAiChatRequest;
import com.ying.learneyjourney.dto.request.OpenAiChatResponse;
import com.ying.learneyjourney.dto.response.GenerateWorksheetResponse;
import com.ying.learneyjourney.dto.response.WorksheetDetailResponse;
import com.ying.learneyjourney.entity.User;
import com.ying.learneyjourney.exception.AiGatewayException;
import com.ying.learneyjourney.exception.NotFoundException;
import com.ying.learneyjourney.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class WorksheetGenerationServiceImpl implements WorksheetGenerationService {
    private final UserRepository userRepository;

    private final RestClient restClient;
    private final OpenAiApiProperties openAiApiProperties;
    private final WorksheetService worksheetService;

    public WorksheetGenerationServiceImpl(RestClient.Builder restClientBuilder,
                                          OpenAiApiProperties openAiApiProperties, WorksheetService worksheetService,
                                          UserRepository userRepository) {
        this.openAiApiProperties = openAiApiProperties;
        this.worksheetService = worksheetService;
        this.restClient = restClientBuilder
                .baseUrl(openAiApiProperties.getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + openAiApiProperties.getApiKey())
                .build();
        this.userRepository = userRepository;
    }

    public static final int FREE_DAILY_GENERATION_LIMIT = 5;

    @Override
    public GenerateWorksheetResponse generateWorksheet(GenerateWorksheetRequest request, String userId) {
        validateRequest(request);
        validateApiKey();

        String language = normalizeLanguage(request.outputLanguage());
        String systemPrompt = buildSystemPrompt(language);

        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        enforceGenerationLimit(user);

        OpenAiChatRequest openAiRequest = new OpenAiChatRequest(
                "gpt-4.1-mini",
                List.of(
                        new OpenAiChatRequest.Message("system", systemPrompt),
                        new OpenAiChatRequest.Message("user", request.prompt())
                ),
                0.7
        );

        try {
            OpenAiChatResponse openAiResponse = restClient.post()
                    .uri(openAiApiProperties.getBaseUrl() + "/v1/chat/completions")
                    .header("Authorization", "Bearer " + openAiApiProperties.getApiKey())
                    .header("Content-Type", "application/json")
                    .body(openAiRequest)
                    .retrieve()
                    .body(OpenAiChatResponse.class);

            String html = extractHtml(openAiResponse);

            WorksheetDetailResponse savedWorksheet = worksheetService.create(
                    new CreateWorksheetRequest(
                            request.userEmail(),
                            request.title(),
                            request.subject(),
                            request.prompt(),
                            language,
                            List.of(
                                    new CreateWorksheetRequest.VersionPayload(
                                            "A",
                                            0,
                                            html
                                    )
                            ),
                            "A"
                    ), userId
            );

            return new GenerateWorksheetResponse(savedWorksheet.id(), html);

        } catch (RestClientResponseException e) {
            log.error("OpenAI API error. status={}, body={}", e.getStatusCode().value(), e.getResponseBodyAsString());

            if (e.getStatusCode().value() == 401) {
                throw new AiGatewayException(HttpStatus.UNAUTHORIZED, "Invalid OpenAI API key.");
            }

            if (e.getStatusCode().value() == 429) {
                throw new AiGatewayException(HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded. Please try again later.");
            }

            throw new AiGatewayException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate worksheet.");
        } catch (AiGatewayException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during worksheet generation", e);
            throw new AiGatewayException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    e.getMessage() != null ? e.getMessage() : "Unknown error"
            );
        }
    }

    private void validateRequest(GenerateWorksheetRequest request) {
        if (request == null) {
            throw new AiGatewayException(HttpStatus.BAD_REQUEST, "Request body is required.");
        }

        if (isBlank(request.prompt())) {
            throw new AiGatewayException(HttpStatus.BAD_REQUEST, "Prompt is required.");
        }

        if (isBlank(request.title())) {
            throw new AiGatewayException(HttpStatus.BAD_REQUEST, "Title is required.");
        }

        if (isBlank(request.subject())) {
            throw new AiGatewayException(HttpStatus.BAD_REQUEST, "Subject is required.");
        }
    }

    private void validateApiKey() {
        if (isBlank(openAiApiProperties.getApiKey())) {
            throw new AiGatewayException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "OPENAI_API_KEY is not configured."
            );
        }

        if (isBlank(openAiApiProperties.getBaseUrl())) {
            throw new AiGatewayException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "OpenAI base URL is not configured."
            );
        }
    }

    private String normalizeLanguage(String outputLanguage) {
        if (isBlank(outputLanguage) || "auto".equalsIgnoreCase(outputLanguage)) {
            return "auto";
        }
        return outputLanguage.trim();
    }

    private String buildSystemPrompt(String language) {
        String languageInstruction = "auto".equalsIgnoreCase(language)
                ? ""
                : "\nIMPORTANT: Generate the worksheet content in " + language + ".";

        return """
                You are a professional worksheet and educational document generator. You create beautiful, well-structured HTML worksheets for teachers and educators.

                IMPORTANT RULES:
                - Return ONLY the HTML content for the worksheet body
                - Do NOT return markdown
                - Do NOT wrap output in ```html ``` code fences
                - Do NOT include <html>, <head>, or <body> tags
                - Use clean semantic HTML only
                - Allowed tags include: <h1>, <h2>, <h3>, <p>, <ol>, <ul>, <li>, <table>, <thead>, <tbody>, <tr>, <th>, <td>, <hr>, <strong>, <em>, <u>, <div>, <span>
                - Include inline CSS styles for layout and spacing
                - Make it look like a clean printable worksheet
                - Add a clear title
                - Add instructions where appropriate
                - Number questions clearly
                - Use underscores for blanks: ____________
                - Format multiple choice options as A) B) C) D)
                - If the user asks for an answer key, include it in a clearly separated section at the bottom
                - Use font-family: inherit
                - Keep the design professional, simple, and readable
                """ + languageInstruction;
    }

    private String extractHtml(OpenAiChatResponse response) {
        if (response == null
                || response.choices() == null
                || response.choices().isEmpty()
                || response.choices().get(0) == null
                || response.choices().get(0).message() == null
                || response.choices().get(0).message().content() == null) {
            throw new AiGatewayException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "OpenAI returned an empty response."
            );
        }

        String html = response.choices().get(0).message().content().trim();

        // Remove accidental markdown fences
        html = html.replaceFirst("^```html\\s*", "");
        html = html.replaceFirst("^```\\s*", "");
        html = html.replaceFirst("\\s*```$", "");

        if (html.isBlank()) {
            throw new AiGatewayException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Generated worksheet HTML is empty."
            );
        }

        return html.trim();
    }

    private void enforceGenerationLimit(User user) {
        resetDailyGenerationIfNeeded(user);

        if (user.getPlanType() == PlanType.PRO) {
            return;
        }

        int used = user.getDailyGenerationsUsed() == null ? 0 : user.getDailyGenerationsUsed();

        if (used >= FREE_DAILY_GENERATION_LIMIT) {
            throw new AiGatewayException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Daily free generation limit reached. Please come back tomorrow or upgrade."
            );
        }

        user.setDailyGenerationsUsed(used + 1);
        userRepository.save(user);
    }

    private void resetDailyGenerationIfNeeded(User user) {
        LocalDate today = LocalDate.now();

        if (user.getDailyGenerationResetDate() == null || !today.equals(user.getDailyGenerationResetDate())) {
            user.setDailyGenerationsUsed(0);
            user.setDailyGenerationResetDate(today);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
