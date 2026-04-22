package com.ying.learneyjourney.service.image;

import com.fasterxml.jackson.databind.JsonNode;
import com.ying.learneyjourney.constaint.ImageProvider;
import com.ying.learneyjourney.dto.request.DownloadedImage;
import com.ying.learneyjourney.dto.response.ImageSearchResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FreepikImageSearchProvider implements ImageSearchProvider {

    private final RestTemplate restTemplate;
    private final String apiKey;

    public FreepikImageSearchProvider(
            RestTemplate restTemplate,
            @Value("${app.image.freepik.api-key}") String apiKey
    ) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
    }

    @Override
    @Cacheable(value = "image-search", key = "'freepik:' + #query + ':' + #limit")
    public List<ImageSearchResult> search(String query, int limit) {
        String encoded = UriUtils.encodeQueryParam(query, StandardCharsets.UTF_8);

        String url = "https://api.freepik.com/v1/resources"
                + "?term=" + encoded
                + "&limit=" + Math.max(1, Math.min(limit, 20))
                + "&page=1";

        HttpHeaders headers = new HttpHeaders();
        headers.set("x-freepik-api-key", apiKey);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        ResponseEntity<JsonNode> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                JsonNode.class
        );

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            return List.of();
        }

        JsonNode dataNode = firstNonMissing(response.getBody(), "data", "items", "resources");
        if (dataNode == null || !dataNode.isArray()) {
            return List.of();
        }

        List<ImageSearchResult> results = new ArrayList<>();

        for (JsonNode item : dataNode) {
            String id = text(item, "id");
            if (id == null || id.isBlank()) {
                continue;
            }

            String title = firstNonBlank(
                    text(item, "title"),
                    text(item, "slug"),
                    text(item, "name"),
                    "Freepik resource"
            );

            String previewUrl = firstNonBlank(
                    nestedText(item, "image", "source", "url"),
                    nestedText(item, "image", "url"),
                    nestedText(item, "preview", "url"),
                    nestedText(item, "thumbnails", "large", "url"),
                    nestedText(item, "thumbnails", "medium", "url"),
                    nestedText(item, "thumbnail", "url"),
                    text(item, "preview_url")
            );

            String sourcePage = firstNonBlank(
                    nestedText(item, "links", "detail"),
                    text(item, "url"),
                    text(item, "source_url")
            );

            String author = firstNonBlank(
                    nestedText(item, "author", "name"),
                    nestedText(item, "user", "name"),
                    text(item, "author")
            );

            Integer width = integer(item, "width");
            Integer height = integer(item, "height");

            String mimeType = firstNonBlank(
                    text(item, "mime_type"),
                    nestedText(item, "image", "mime_type"),
                    "image/jpeg"
            );

            results.add(new ImageSearchResult(
                    id,
                    ImageProvider.FREEPIK,
                    title,
                    previewUrl,
                    null,
                    sourcePage,
                    author,
                    mimeType,
                    width,
                    height
            ));
        }

        return results;
    }

    @Override
    public Optional<ImageSearchResult> bestMatch(String query) {
        return search(query, 5).stream().findFirst();
    }

    @Override
    public DownloadTarget resolveDownloadTarget(ImageSearchResult result) {
        String downloadUrl = "https://api.freepik.com/v1/resources/" + result.getId() + "/download";
        return new DownloadTarget(downloadUrl, result.getMimeType(), true);
    }

    @Override
    public DownloadedImage downloadApiProtectedAsset(String url, String mimeTypeHint) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-freepik-api-key", apiKey);
        headers.setAccept(List.of(
                MediaType.APPLICATION_OCTET_STREAM,
                MediaType.IMAGE_JPEG,
                MediaType.IMAGE_PNG,
                MediaType.valueOf("image/webp"),
                MediaType.ALL
        ));

        ResponseEntity<byte[]> response = restTemplate.exchange(
                URI.create(url),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                byte[].class
        );

        if (!response.getStatusCode().is2xxSuccessful()
                || response.getBody() == null
                || response.getBody().length == 0) {
            throw new IllegalArgumentException("Failed to download Freepik asset");
        }

        MediaType contentType = response.getHeaders().getContentType();
        String mimeType = contentType != null ? contentType.toString() : mimeTypeHint;
        if (mimeType == null || mimeType.isBlank()) {
            mimeType = "image/jpeg";
        }

        return new DownloadedImage(response.getBody(), mimeType, url);
    }

    private static JsonNode firstNonMissing(JsonNode root, String... fieldNames) {
        if (root == null) return null;
        for (String field : fieldNames) {
            JsonNode node = root.path(field);
            if (!node.isMissingNode() && !node.isNull()) {
                return node;
            }
        }
        return null;
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    private static String nestedText(JsonNode node, String... path) {
        JsonNode current = node;
        for (String part : path) {
            current = current.path(part);
            if (current.isMissingNode() || current.isNull()) {
                return null;
            }
        }
        return current.asText();
    }

    private static Integer integer(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isNumber() ? value.asInt() : null;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
