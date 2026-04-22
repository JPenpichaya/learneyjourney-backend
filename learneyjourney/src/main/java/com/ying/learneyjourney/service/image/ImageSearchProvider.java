package com.ying.learneyjourney.service.image;

import com.ying.learneyjourney.constaint.ImageProvider;
import com.ying.learneyjourney.dto.request.DownloadedImage;
import com.ying.learneyjourney.dto.response.ImageSearchResult;

import java.util.List;
import java.util.Optional;

public interface ImageSearchProvider {
    List<ImageSearchResult> search(String query, int limit);

    Optional<ImageSearchResult> bestMatch(String query);

    DownloadTarget resolveDownloadTarget(ImageSearchResult result);

    default DownloadedImage downloadApiProtectedAsset(String url, String mimeTypeHint) {
        throw new UnsupportedOperationException("Provider does not support protected binary download");
    }

    class DownloadTarget {
        private final String url;
        private final String mimeTypeHint;
        private final boolean providerProtected;

        public DownloadTarget(String url, String mimeTypeHint, boolean providerProtected) {
            this.url = url;
            this.mimeTypeHint = mimeTypeHint;
            this.providerProtected = providerProtected;
        }

        public String getUrl() {
            return url;
        }

        public String getMimeTypeHint() {
            return mimeTypeHint;
        }

        public boolean isProviderProtected() {
            return providerProtected;
        }
    }
}