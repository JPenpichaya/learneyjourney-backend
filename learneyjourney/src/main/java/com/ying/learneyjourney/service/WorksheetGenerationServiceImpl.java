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
import com.ying.learneyjourney.entity.Worksheet;
import com.ying.learneyjourney.entity.WorksheetVersion;
import com.ying.learneyjourney.exception.AiGatewayException;
import com.ying.learneyjourney.exception.NotFoundException;
import com.ying.learneyjourney.repository.UserRepository;
import com.ying.learneyjourney.repository.WorksheetRepository;
import com.ying.learneyjourney.repository.WorksheetVersionRepository;
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

    public static final int FREE_DAILY_GENERATION_LIMIT = 5;
    private static final String DEFAULT_MODEL = "gpt-4.1-mini";

    private final UserRepository userRepository;
    private final RestClient restClient;
    private final OpenAiApiProperties openAiApiProperties;
    private final WorksheetService worksheetService;
    private final WorksheetImageRenderer worksheetImageRenderer;
    private final WorksheetVersionRepository worksheetVersionRepository;
    private final WorksheetRepository worksheetRepository;

    public WorksheetGenerationServiceImpl(RestClient.Builder restClientBuilder,
                                          OpenAiApiProperties openAiApiProperties,
                                          WorksheetService worksheetService,
                                          UserRepository userRepository,
                                          WorksheetImageRenderer worksheetImageRenderer,
                                          WorksheetVersionRepository worksheetVersionRepository,
                                          WorksheetRepository worksheetRepository) {
        this.openAiApiProperties = openAiApiProperties;
        this.worksheetService = worksheetService;
        this.userRepository = userRepository;
        this.worksheetImageRenderer = worksheetImageRenderer;
        this.worksheetVersionRepository = worksheetVersionRepository;
        this.worksheetRepository = worksheetRepository;

        this.restClient = restClientBuilder
                .baseUrl(normalizeBaseUrl(openAiApiProperties.getBaseUrl()))
                .defaultHeader("Authorization", "Bearer " + openAiApiProperties.getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public GenerateWorksheetResponse generate(GenerateWorksheetRequest request, String userId) {
        validateRequest(request);
        validateApiKey();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        enforceGenerationLimit(user);

        String language = normalizeLanguage(request.outputLanguage());
        String systemPrompt = buildSystemPrompt(language);

        OpenAiChatRequest openAiRequest = new OpenAiChatRequest(
                DEFAULT_MODEL,
                List.of(
                        new OpenAiChatRequest.Message("system", systemPrompt),
                        new OpenAiChatRequest.Message("user", request.prompt())
                ),
                0.5
        );

        try {
            OpenAiChatResponse openAiResponse = restClient.post()
                    .uri("/chat/completions")
                    .body(openAiRequest)
                    .retrieve()
                    .body(OpenAiChatResponse.class);

            String renderedHtml = extractAndRenderHtml(openAiResponse);

            if (request.existingWorksheetId() != null) {
                Worksheet worksheet = worksheetRepository.findById(request.existingWorksheetId())
                        .orElseThrow(() -> new NotFoundException("Worksheet not found"));

                int nextOrder = worksheet.getVersions().size();
                String nextLabel = generateLabel(nextOrder);

                WorksheetVersion newVersion = new WorksheetVersion();
                newVersion.setWorksheet(worksheet);
                newVersion.setSortOrder(nextOrder);
                newVersion.setVersionLabel(nextLabel);
                newVersion.setHtmlContent(renderedHtml);

                worksheetVersionRepository.save(newVersion);

                worksheet.setActiveVersionLabel(nextLabel);
                worksheetRepository.save(worksheet);

                return new GenerateWorksheetResponse(worksheet.getId(), renderedHtml);
            } else {
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
                                                renderedHtml
                                        )
                                ),
                                "A"
                        ),
                        userId
                );

                return new GenerateWorksheetResponse(savedWorksheet.id(), renderedHtml);
            }

        } catch (RestClientResponseException e) {
            log.error("OpenAI API error. status={}, body={}",
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString());

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

    private String generateLabel(int index) {
        if (index < 0) return "A";
        StringBuilder label = new StringBuilder();
        int temp = index;
        while (temp >= 0) {
            label.insert(0, (char) ('A' + (temp % 26)));
            temp = (temp / 26) - 1;
        }
        return label.toString();
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

                ALLOWED TAGS:
                <h1>, <h2>, <h3>, <p>, <ol>, <ul>, <li>, <table>, <thead>, <tbody>, <tr>, <th>, <td>, <hr>, <strong>, <em>, <u>, <div>, <span>, <img>

                IMAGE RULES:
                - Include images or diagrams whenever they are relevant and helpful for the worksheet topic
                - DO NOT use real URLs
                - DO NOT embed base64 images directly
                - For every needed image or diagram, use this exact placeholder format inside img src:
                  {{IMAGE: clear visual description}}
                - Example:
                  <img src="{{IMAGE: a labeled diagram of the solar system}}" alt="Solar system diagram" style="width: 100%; max-width: 500px; display: block; margin: 12px auto;" />
                - If the worksheet topic benefits from a visual, include at least one image placeholder

                DESIGN RULES:
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

                OUTPUT RULES:
                - Return only worksheet HTML
                - No explanations before or after the HTML
                - Do not mention placeholders outside the HTML itself

                """ + languageInstruction;
    }

    private String extractAndRenderHtml(OpenAiChatResponse response) {
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
        html = html.trim();

        if (html.isBlank()) {
            throw new AiGatewayException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Generated worksheet HTML is empty."
            );
        }

        try {
            String renderedHtml = worksheetImageRenderer.renderImages(html);

            if (isBlank(renderedHtml)) {
                throw new AiGatewayException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Rendered worksheet HTML is empty."
                );
            }

            return renderedHtml.trim();
        } catch (Exception e) {
            log.error("Failed to render worksheet images", e);
            throw new AiGatewayException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to render worksheet images."
            );
        }
    }

    private void enforceGenerationLimit(User user) {
        if (user.getPlanType() == PlanType.PRO) {
            return;
        }

        int credits = user.getGenerationCredits() == null ? 0 : user.getGenerationCredits();
        if (credits > 0) {
            user.setGenerationCredits(credits - 1);
            userRepository.save(user);
            return;
        }

        resetDailyGenerationIfNeeded(user);

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

        if (user.getDailyGenerationResetDate() == null
                || !today.equals(user.getDailyGenerationResetDate())) {
            user.setDailyGenerationsUsed(0);
            user.setDailyGenerationResetDate(today);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.isBlank()) {
            return baseUrl;
        }

        String normalized = baseUrl.trim();
        if (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.endsWith("/v1")) {
            return normalized;
        }
        return normalized + "/v1";
    }
}