package com.ying.learneyjourney.service;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.ying.learneyjourney.Util.ImageProcessor;
import com.ying.learneyjourney.dto.request.ImageUploadRequest;
import com.ying.learneyjourney.dto.response.ImageUploadResponse;
import com.ying.learneyjourney.entity.MediaAsset;
import com.ying.learneyjourney.repository.MediaAssetRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final Storage storage;
    private final MediaAssetRepository mediaAssetRepository;

    @Value("${gcs.bucket}")
    private String bucket;

    @Value("${gcs.public-base-url:https://storage.googleapis.com}")
    private String publicBaseUrl;

    public ImageUploadResponse uploadAndSave(MultipartFile file, ImageUploadRequest req) throws IOException {
        validateImage(file);

        // 1) Generate keys
        String id = UUID.randomUUID().toString();
        String originalKey = "images/original/" + id + ".jpg";
        String thumbKey = "images/thumb/" + id + ".jpg";

        // 2) Prepare bytes
        byte[] originalBytes = file.getBytes();
        byte[] thumbBytes = ImageProcessor.resizeAndCompress(originalBytes, 500, 0.75f);

        // 3) Upload to bucket
        try {
            uploadToGcs(originalKey, originalBytes, file.getContentType());
            uploadToGcs(thumbKey, thumbBytes, "image/jpeg");

            // 4) Save DB record (transaction)
            MediaAsset saved = saveMediaAssetRecord(file, req, originalKey, thumbKey,
                    originalBytes.length, thumbBytes.length);

            // 5) Return URLs (public URL style; for private, you’d generate signed URLs here)
            ImageUploadResponse resp = new ImageUploadResponse();
            resp.setAssetId(saved.getId());
            resp.setOriginalUrl(signedUrl(originalKey));
            resp.setThumbnailUrl(signedUrl(thumbKey));
            return resp;
        } catch (RuntimeException ex) {
            // If DB save or anything fails after upload, cleanup objects
            safeDelete(originalKey);
            safeDelete(thumbKey);
            throw ex;
        }
    }

    @Transactional
    protected MediaAsset saveMediaAssetRecord(
            MultipartFile file,
            ImageUploadRequest req,
            String originalKey,
            String thumbKey,
            long originalSize,
            long thumbSize
    ) {

        Map<String, Object> variants = new HashMap<>();
        variants.put("original", Map.of(
                "object_key", originalKey,
                "content_type", file.getContentType(),
                "size", originalSize
        ));
        variants.put("thumb", Map.of(
                "object_key", thumbKey,
                "content_type", "image/jpeg",
                "size", thumbSize,
                "width", 500
        ));

        MediaAsset entity = new MediaAsset();
        entity.setOwnerUserId(null) ;// set from auth later
        entity.setVisibility(req.getVisibility());
                entity.setAssetKind("IMAGE");
                entity.setDescription(req.getDescription());
                entity.setBucket(bucket);
                entity.setObjectKey(originalKey);
                entity.setVariants(variants);
                entity.setOriginalFilename(file.getOriginalFilename());
                entity.setContentType(file.getContentType());
                entity.setAssetSize(originalSize);
                entity.setRelatedEntityType(req.getRelatedEntityType());
                entity.setRelatedEntityId(req.getRelatedEntityId());

        return mediaAssetRepository.save(entity);
    }

    private void uploadToGcs(String objectKey, byte[] bytes, String contentType) {
        BlobId blobId = BlobId.of(bucket, objectKey);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(contentType)
                .build();
        storage.create(blobInfo, bytes);
    }

    private void safeDelete(String objectKey) {
        try {
            storage.delete(BlobId.of(bucket, objectKey));
        } catch (Exception ignored) {}
    }

    private String signedUrl(String objectKey) {
        BlobInfo blobInfo = BlobInfo.newBuilder(bucket, objectKey).build();

        URL url = storage.signUrl(
                blobInfo,
                15, TimeUnit.MINUTES,                 // expiry
                Storage.SignUrlOption.withV4Signature()
        );

        return url.toString();
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("File is empty");
        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Only image files allowed");
        }
    }
}