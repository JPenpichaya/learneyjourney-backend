package com.ying.learneyjourney.service.image;

import com.ying.learneyjourney.component.ImageProviderRegistry;
import com.ying.learneyjourney.constaint.ImageProvider;
import com.ying.learneyjourney.dto.request.DownloadedImage;
import com.ying.learneyjourney.dto.response.ImageSearchResult;
import com.ying.learneyjourney.dto.response.WorksheetAiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AiImageRequestResolverService {

    private final ImageProviderRegistry registry;
    private final ImageProviderDecisionService providerDecisionService;
    private final RemoteImageDownloadService remoteDownloader;
    private final ImageResizeService imageResizeService;
    private final ImageEmbedService imageEmbedService;

    public String resolveAndEmbed(WorksheetAiResponse aiResponse) {
        Map<String, String> replacements = new HashMap<>();

        for (WorksheetAiResponse.ImageRequest req : aiResponse.getImageRequests()) {
            replacements.put(req.getId(), resolveSingle(req));
        }

        String html = aiResponse.getHtml();

        for (WorksheetAiResponse.ImageRequest req : aiResponse.getImageRequests()) {
            String placeholder = "{{IMAGE:" + req.getId() + "}}";
            String dataUrl = replacements.getOrDefault(req.getId(), "");
            html = html.replace(placeholder, dataUrl);
        }

        return html;
    }

    private String resolveSingle(WorksheetAiResponse.ImageRequest req) {
        List<ImageProvider> order = providerDecisionService.providerOrder(req.getKind());

        for (ImageProvider provider : order) {
            String query = provider == ImageProvider.FREEPIK
                    ? req.getFreepikQuery()
                    : req.getUnsplashQuery();

            String result = tryProvider(provider, query);
            if (result != null && !result.isBlank()) {
                return result;
            }
        }

        return "";
    }

    private String tryProvider(ImageProvider provider, String query) {
        try {
            ImageSearchProvider searchProvider = registry.get(provider);
            Optional<ImageSearchResult> match = searchProvider.bestMatch(query);

            if (match.isEmpty()) {
                return "";
            }

            ImageSearchResult image = match.get();
            ImageSearchProvider.DownloadTarget target = searchProvider.resolveDownloadTarget(image);

            DownloadedImage downloaded;
            if (target.isProviderProtected()) {
                downloaded = searchProvider.downloadApiProtectedAsset(target.getUrl(), target.getMimeTypeHint());
            } else {
                downloaded = remoteDownloader.download(target.getUrl(), target.getMimeTypeHint());
            }

            byte[] optimized = imageResizeService.resizeToJpeg(downloaded.getBytes(), 1200);
            return imageEmbedService.toDataUrl(optimized, "image/jpeg");
        } catch (Exception e) {
            return "";
        }
    }
}