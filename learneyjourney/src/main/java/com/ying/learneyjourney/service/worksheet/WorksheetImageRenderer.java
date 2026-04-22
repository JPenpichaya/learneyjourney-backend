package com.ying.learneyjourney.service.worksheet;

import com.ying.learneyjourney.component.*;
import com.ying.learneyjourney.constaint.ImageProvider;
import com.ying.learneyjourney.dto.request.DownloadedImage;
import com.ying.learneyjourney.dto.response.ImageSearchResult;
import com.ying.learneyjourney.service.image.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorksheetImageRenderer {

    private static final Pattern IMAGE_PLACEHOLDER = Pattern.compile("\\{\\{IMAGE:(.+?)}}");

    private final ImageProviderRegistry registry;
    private final RemoteImageDownloadService downloader;
    private final ImageResizeService imageResizeService;
    private final ImageEmbedService imageEmbedService;

    @Cacheable(value = "image-resolve", key = "#html")
    public String renderImages(String html) {
        if (html == null || html.isBlank()) {
            return html;
        }

        Matcher matcher = IMAGE_PLACEHOLDER.matcher(html);
        StringBuffer out = new StringBuffer();

        while (matcher.find()) {
            String query = matcher.group(1).trim();
            String replacement = resolveSingle(query);
            matcher.appendReplacement(out, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(out);
        return out.toString();
    }

    private String resolveSingle(String query) {
        List<ImageProvider> providerOrder = chooseProviderOrder(query);

        for (ImageProvider provider : providerOrder) {
            try {
                String result = tryResolveWithProvider(query, provider);
                if (result != null && !result.isBlank()) {
                    return result;
                }
            } catch (Exception e) {
                log.warn("Failed resolving image with provider {} for query '{}': {}", provider, query, e.getMessage());
            }
        }

        log.warn("Could not resolve image for query '{}'", query);
        return "";
    }

    private String tryResolveWithProvider(String query, ImageProvider provider) {
        ImageSearchProvider searchProvider = registry.get(provider);
        Optional<ImageSearchResult> match = searchProvider.bestMatch(query);

        if (match.isEmpty()) {
            return "";
        }

        ImageSearchResult result = match.get();
        ImageSearchProvider.DownloadTarget target = searchProvider.resolveDownloadTarget(result);

        DownloadedImage downloaded;
        if (provider == ImageProvider.FREEPIK && target.getUrl().startsWith("https://api.freepik.com/")) {
            downloaded = resolveFreepikApiDownload(target.getUrl(), result.getMimeType());
        } else {
            HttpHeaders extraHeaders = new HttpHeaders();
            downloaded = downloader.download(
                    target.getUrl(),
                    target.getMimeTypeHint(),
                    extraHeaders
            );
        }

        byte[] optimized = imageResizeService.resizeToJpeg(downloaded.getBytes(), 1200);
        return imageEmbedService.toDataUrl(optimized, "image/jpeg");
    }

    private DownloadedImage resolveFreepikApiDownload(String apiDownloadUrl, String mimeTypeHint) {
        ImageSearchProvider provider = registry.get(ImageProvider.FREEPIK);
        if (!(provider instanceof FreepikImageSearchProvider freepik)) {
            throw new IllegalStateException("Freepik provider not available");
        }

        return freepik.downloadApiProtectedAsset(apiDownloadUrl, mimeTypeHint);
    }

    private List<ImageProvider> chooseProviderOrder(String query) {
        String q = query == null ? "" : query.toLowerCase();

        if (containsAny(q,
                "diagram", "label", "labeled", "labelled", "worksheet", "vector",
                "illustration", "icon", "cell", "dna", "process", "cycle",
                "structure", "map", "chart")) {
            return List.of(ImageProvider.FREEPIK, ImageProvider.UNSPLASH);
        }

        if (containsAny(q,
                "photo", "real", "animal", "person", "teacher", "student",
                "dog", "cat", "forest", "mountain", "food", "city", "classroom")) {
            return List.of(ImageProvider.UNSPLASH, ImageProvider.FREEPIK);
        }

        return List.of(ImageProvider.FREEPIK, ImageProvider.UNSPLASH);
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}