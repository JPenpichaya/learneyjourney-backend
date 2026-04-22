package com.ying.learneyjourney.dto.response;

import com.ying.learneyjourney.constaint.ImageProvider;

public class ImageSearchResult {
    private String id;
    private ImageProvider provider;
    private String title;
    private String previewUrl;
    private String downloadUrl;
    private String sourcePage;
    private String author;
    private String mimeType;
    private Integer width;
    private Integer height;

    public ImageSearchResult() {
    }

    public ImageSearchResult(
            String id,
            ImageProvider provider,
            String title,
            String previewUrl,
            String downloadUrl,
            String sourcePage,
            String author,
            String mimeType,
            Integer width,
            Integer height
    ) {
        this.id = id;
        this.provider = provider;
        this.title = title;
        this.previewUrl = previewUrl;
        this.downloadUrl = downloadUrl;
        this.sourcePage = sourcePage;
        this.author = author;
        this.mimeType = mimeType;
        this.width = width;
        this.height = height;
    }

    public String getId() {
        return id;
    }

    public ImageProvider getProvider() {
        return provider;
    }

    public String getTitle() {
        return title;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getSourcePage() {
        return sourcePage;
    }

    public String getAuthor() {
        return author;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setProvider(ImageProvider provider) {
        this.provider = provider;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public void setSourcePage(String sourcePage) {
        this.sourcePage = sourcePage;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }
}