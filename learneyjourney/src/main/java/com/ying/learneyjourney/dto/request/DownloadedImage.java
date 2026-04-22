package com.ying.learneyjourney.dto.request;

public class DownloadedImage {
    private final byte[] bytes;
    private final String mimeType;
    private final String sourceUrl;

    public DownloadedImage(byte[] bytes, String mimeType, String sourceUrl) {
        this.bytes = bytes;
        this.mimeType = mimeType;
        this.sourceUrl = sourceUrl;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }
}
