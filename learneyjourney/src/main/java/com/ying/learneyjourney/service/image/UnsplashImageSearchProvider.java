package com.ying.learneyjourney.service.image;

import com.fasterxml.jackson.databind.JsonNode;
import com.ying.learneyjourney.constaint.ImageProvider;
import com.ying.learneyjourney.dto.response.ImageSearchResult;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UnsplashImageSearchProvider implements ImageSearchProvider {

    private final RestTemplate restTemplate;
    private final String accessKey;

    public UnsplashImageSearchProvider(
            RestTemplate restTemplate,
            @Value("${app.image.unsplash.access-key}") String accessKey
    ) {
        this.restTemplate = restTemplate;
        this.accessKey = accessKey;
    }

    @Override
    @Cacheable(value = "image-search", key = "'unsplash:' + #query + ':' + #limit")
    public List<ImageSearchResult> search(String query, int limit) {
        String encoded = UriUtils.encodeQueryParam(query, StandardCharsets.UTF_8);
        String url = "https://api.unsplash.com/search/photos?query=" + encoded
                + "&page=1&per_page=" + Math.max(1, Math.min(limit, 30))
                + "&orientation=landscape";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Client-ID " + accessKey);
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

        List<ImageSearchResult> results = new ArrayList<>();
        for (JsonNode item : response.getBody().path("results")) {
            String id = item.path("id").asText();
            String title = item.path("alt_description").asText("Unsplash image");
            String previewUrl = item.path("urls").path("regular").asText();
            String downloadUrl = item.path("urls").path("full").asText(previewUrl);
            String sourcePage = item.path("links").path("html").asText();
            String author = item.path("user").path("name").asText();
            Integer width = item.path("width").isNumber() ? item.path("width").asInt() : null;
            Integer height = item.path("height").isNumber() ? item.path("height").asInt() : null;

            results.add(new ImageSearchResult(
                    id,
                    ImageProvider.UNSPLASH,
                    title,
                    previewUrl,
                    downloadUrl,
                    sourcePage,
                    author,
                    "image/jpeg",
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
        String url = result.getDownloadUrl() != null && !result.getDownloadUrl().isBlank()
                ? result.getDownloadUrl()
                : result.getPreviewUrl();

        return new DownloadTarget(url, result.getMimeType(), false);
    }
}