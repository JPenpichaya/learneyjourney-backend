package com.ying.learneyjourney.service.worksheet;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.ying.learneyjourney.service.image.AiImageRequestResolverService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

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
    private static final String DEFAULT_MODEL = "gpt-4o";

    private final UserRepository userRepository;
    private final RestClient restClient;
    private final OpenAiApiProperties openAiApiProperties;
    private final WorksheetService worksheetService;
    private final WorksheetImageRenderer worksheetImageRenderer;
    private final WorksheetVersionRepository worksheetVersionRepository;
    private final WorksheetRepository worksheetRepository;
    private final ObjectMapper objectMapper;

    public WorksheetGenerationServiceImpl(RestClient.Builder restClientBuilder,
                                          OpenAiApiProperties openAiApiProperties,
                                          WorksheetService worksheetService,
                                          UserRepository userRepository,
                                          WorksheetImageRenderer worksheetImageRenderer,
                                          WorksheetVersionRepository worksheetVersionRepository,
                                          WorksheetRepository worksheetRepository,
                                          ObjectMapper objectMapper) {
        this.openAiApiProperties = openAiApiProperties;
        this.worksheetService = worksheetService;
        this.userRepository = userRepository;
        this.worksheetImageRenderer = worksheetImageRenderer;
        this.worksheetVersionRepository = worksheetVersionRepository;
        this.worksheetRepository = worksheetRepository;
        this.objectMapper = objectMapper;

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

            String rawHtml = extractHtml(openAiResponse);
            String resolvedHtml = renderImagesSafely(rawHtml);

            if (request.existingWorksheetId() != null) {
                Worksheet worksheet = worksheetRepository.findById(request.existingWorksheetId())
                        .orElseThrow(() -> new NotFoundException("Worksheet not found"));

                int nextOrder = worksheet.getVersions().size();
                String nextLabel = generateLabel(nextOrder);

                WorksheetVersion newVersion = new WorksheetVersion();
                newVersion.setWorksheet(worksheet);
                newVersion.setSortOrder(nextOrder);
                newVersion.setVersionLabel(nextLabel);
                newVersion.setHtmlContent(rawHtml);
                newVersion.setResolvedHtml(resolvedHtml);

                worksheetVersionRepository.save(newVersion);

                worksheet.setActiveVersionLabel(nextLabel);
                worksheetRepository.save(worksheet);

                return new GenerateWorksheetResponse(
                        worksheet.getId(),
                        rawHtml,
                        resolvedHtml
                );
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
                                                rawHtml,
                                                resolvedHtml
                                        )
                                ),
                                "A"
                        ),
                        userId
                );

                return new GenerateWorksheetResponse(
                        savedWorksheet.id(),
                        rawHtml,
                        resolvedHtml
                );
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
        html = html.trim();

        if (html.isBlank()) {
            throw new AiGatewayException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Generated worksheet HTML is empty."
            );
        }

        return html;
    }

    private String renderImagesSafely(String rawHtml) {
        try {
            String renderedHtml = worksheetImageRenderer.renderImages(rawHtml);

            if (renderedHtml == null || renderedHtml.trim().isEmpty()) {
                throw new AiGatewayException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Rendered worksheet HTML is empty."
                );
            }

            return renderedHtml.trim();
        } catch (AiGatewayException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to render worksheet images", e);
            throw new AiGatewayException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to render worksheet images."
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
                You are a professional worksheet and educational document designer specializing in creating modern, visually appealing, classroom-ready HTML worksheets.
                
                  Your output should feel like a premium educational product — clean, engaging, and thoughtfully designed for both students and teachers.
    
                  CORE OBJECTIVE
                  Create a beautiful, structured, and highly readable worksheet using clean semantic HTML with modern layout styling and strong visual hierarchy.
    
                  STRICT OUTPUT RULES
                  - Return ONLY the HTML content for the worksheet body
                  - Do NOT return markdown
                  - Do NOT wrap output in code blocks
                  - Do NOT include <html>, <head>, or <body> tags
                  - Do NOT include explanations
    
                  ALLOWED HTML TAGS
                  <h1>, <h2>, <h3>, <p>, <ol>, <ul>, <li>, <table>, <thead>, <tbody>, <tr>, <th>, <td>, <hr>, <strong>, <em>, <u>, <div>, <span>, <img>
    
                  DESIGN REQUIREMENTS
                  Make the worksheet look modern and polished, not plain.
    
                  Use:
                  - Soft spacing and padding
                  - Clear section separation using <div> containers
                  - Subtle visual hierarchy using font size, bold headings, spacing, and clean grouping
                  - Centered header section with strong title presence
                  - Section cards using bordered or lightly shaded div blocks
                  - Balanced margins for print with max-width: 800px and centered layout
    
                  Styling Guidelines:
                  - font-family: inherit
                  - Use inline CSS only
                  - Use spacing like margin-bottom: 16px to 28px
                  - Use soft borders: border: 1px solid #ddd
                  - Use rounded corners: border-radius: 8px
                  - Use light background sections: background-color: #f9f9f9
                  - Keep it print-friendly and avoid dark backgrounds
    
                  PRINT AND PAGINATION RULES (VERY IMPORTANT)
                  The worksheet will be exported to PDF, so the HTML must be pagination-safe and print-friendly.
    
                  Follow these rules:
                  - Keep sections logically grouped so a question block stays together when possible
                  - Avoid breaking a single question across pages
                  - Avoid placing large empty spaces before page breaks
                  - Do not create layouts that depend on screen-only viewing
                  - Keep images moderate in size so they do not force awkward page breaks
                  - Use simple vertical stacking rather than multi-column layouts
                  - Important sections such as instructions, question groups, tables, and answer key should be wrapped in div containers with print-friendly inline styles
                  - For major sections, use inline styles like:
                    page-break-inside: avoid;
                    break-inside: avoid;
                  - For sections that should start on a new PDF page when appropriate, use:
                    page-break-before: always;
                  - Keep tables readable and avoid overly wide tables
                  - If there is an answer key, place it in a clearly separated section at the bottom, and start it on a new page only if the worksheet is already long
    
                  IMAGE RULES
                  - Include visuals whenever they improve understanding
                  - Use ONLY this format for images:
                    <img src="{{IMAGE: clear visual description}}" alt="description" style="width: 100%; max-width: 500px; display: block; margin: 16px auto;" />
                  - Do NOT use real URLs
                  - Do NOT use base64
                  - Use at least one image for science, math, geography, biology, diagrams, labeling tasks, or any visual-heavy topic
                  - Images should use print-safe sizing and should not dominate the page
    
                  CONTENT STRUCTURE
                  Organize the worksheet like this:
    
                  1. Header Section
                  - Large centered title
                  - Short subtitle or topic description
                  - Optional student info line:
                    Name: ____________   Date: ____________
    
                  2. Instructions Block
                  - Clear and concise
                  - Placed inside a styled container
    
                  3. Main Sections
                  Use multiple sections where appropriate, such as:
                  - Multiple Choice
                  - Fill in the Blanks
                  - Short Answer
                  - Matching
                  - Diagram Labeling
                  - True or False
                  - Challenge Task
    
                  4. Question Formatting
                  - Number all questions clearly
                  - Format multiple choice options as:
                    A) ...
                    B) ...
                    C) ...
                    D) ...
                  - Use blanks like: ____________
    
                  5. Optional Challenge Section
                  - Add one or more higher-order thinking questions when suitable
    
                  6. Answer Key
                  - If the user asks for an answer key, include it at the bottom
                  - Separate it clearly with <hr>
                  - Label it clearly
                  - Keep answer key neatly grouped
                  - Start on a new page only when necessary
    
                  QUALITY STANDARD
                  The worksheet should feel like:
                  - A premium printable PDF
                  - Suitable for international schools, tutoring centers, and polished classroom use
                  - Clean, modern, and visually engaging
                  - Professionally designed, not just text dumped onto a page
    
                  AVOID
                  - Plain walls of text
                  - Cramped layout
                  - Weak spacing
                  - Bare, boring formatting
                  - Missing instructions
                  - Overly decorative styles that hurt print readability
                  - Large blocks that split badly across pages
                  - Oversized images or tables that overflow
    
                  FINAL REMINDER
                  You are not just generating content.
                  You are designing an educational worksheet that should look attractive, modern, professional, and ready to print as a well-paginated PDF.
                  Return only the worksheet HTML.
                """ + languageInstruction;
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