package com.ying.learneyjourney.service.image;

import com.ying.learneyjourney.config.ImageDownloadProperties;
import com.ying.learneyjourney.dto.request.DownloadedImage;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RemoteImageDownloadService {

    private final RestTemplate restTemplate;
    private final Set<String> allowedHosts;
    private final int maxImageBytes;

    public RemoteImageDownloadService(
            RestTemplate restTemplate,
            ImageDownloadProperties properties
    ) {
        this.restTemplate = restTemplate;
        this.allowedHosts = new HashSet<>(properties.getAllowedHosts());
        this.maxImageBytes = properties.getMaxImageBytes();
    }

    @Cacheable(value = "image-download", key = "#url")
    public DownloadedImage download(String url, String mimeTypeHint) {
        return download(url, mimeTypeHint, null);
    }

    @Cacheable(value = "image-download", key = "#url")
    public DownloadedImage download(String url, String mimeTypeHint, HttpHeaders extraHeaders) {
        URI uri = URI.create(url);
        validateHost(uri.getHost());

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(List.of(
                MediaType.IMAGE_JPEG,
                MediaType.IMAGE_PNG,
                MediaType.IMAGE_GIF,
                MediaType.valueOf("image/webp"),
                MediaType.APPLICATION_OCTET_STREAM,
                MediaType.ALL
        ));

        if (extraHeaders != null) {
            extraHeaders.forEach((key, values) -> values.forEach(value -> headers.add(key, value)));
        }

        RequestEntity<Void> request = new RequestEntity<>(headers, HttpMethod.GET, uri);
        ResponseEntity<byte[]> response = restTemplate.exchange(request, byte[].class);

        if (!response.getStatusCode().is2xxSuccessful()
                || response.getBody() == null
                || response.getBody().length == 0) {
            throw new IllegalArgumentException("Failed to download image from " + url);
        }

        if (response.getBody().length > maxImageBytes) {
            throw new IllegalArgumentException("Image too large: " + response.getBody().length + " bytes");
        }

        MediaType contentType = response.getHeaders().getContentType();
        String mimeType = contentType != null ? contentType.toString() : mimeTypeHint;
        if (mimeType == null || mimeType.isBlank()) {
            mimeType = "image/jpeg";
        }

        return new DownloadedImage(response.getBody(), mimeType, url);
    }

    private void validateHost(String host) {
        if (host == null || !allowedHosts.contains(host)) {
            throw new IllegalArgumentException("Image host is not allowed: " + host);
        }
    }
}