package com.ying.learneyjourney.dto.response;

import com.ying.learneyjourney.constaint.ImageKind;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.ArrayList;
import java.util.List;

public class WorksheetAiResponse {

    @NotBlank
    private String html;

    @NotEmpty
    private List<ImageRequest> imageRequests = new ArrayList<>();

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public List<ImageRequest> getImageRequests() {
        return imageRequests;
    }

    public void setImageRequests(List<ImageRequest> imageRequests) {
        this.imageRequests = imageRequests;
    }

    public static class ImageRequest {
        @NotBlank
        private String id;

        private ImageKind kind = ImageKind.AUTO;

        @NotBlank
        private String altText;

        @NotBlank
        private String freepikQuery;

        @NotBlank
        private String unsplashQuery;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public ImageKind getKind() {
            return kind;
        }

        public void setKind(ImageKind kind) {
            this.kind = kind;
        }

        public String getAltText() {
            return altText;
        }

        public void setAltText(String altText) {
            this.altText = altText;
        }

        public String getFreepikQuery() {
            return freepikQuery;
        }

        public void setFreepikQuery(String freepikQuery) {
            this.freepikQuery = freepikQuery;
        }

        public String getUnsplashQuery() {
            return unsplashQuery;
        }

        public void setUnsplashQuery(String unsplashQuery) {
            this.unsplashQuery = unsplashQuery;
        }
    }
}