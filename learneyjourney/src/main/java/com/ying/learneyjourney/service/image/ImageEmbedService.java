package com.ying.learneyjourney.service.image;

import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class ImageEmbedService {

    public String toDataUrl(byte[] bytes, String mimeType) {
        String safeMime = (mimeType == null || mimeType.isBlank()) ? "image/jpeg" : mimeType;
        return "data:" + safeMime + ";base64," + Base64.getEncoder().encodeToString(bytes);
    }
}