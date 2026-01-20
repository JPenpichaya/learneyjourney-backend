package com.ying.learneyjourney.controller;
import com.ying.learneyjourney.dto.request.ImageUploadRequest;
import com.ying.learneyjourney.dto.response.ImageUploadResponse;
import com.ying.learneyjourney.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageUploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("relatedEntityType") String relatedEntityType,
            @RequestParam("relatedEntityId") UUID relatedEntityId,
            @RequestParam(value = "visibility", required = false) String visibility,
            @RequestParam(value = "description", required = false) String description
    ) throws IOException {

        ImageUploadRequest req = new ImageUploadRequest();
        req.setRelatedEntityType(relatedEntityType);
        req.setRelatedEntityId(relatedEntityId);
        req.setVisibility(visibility == null ? "PRIVATE" : visibility);
        req.setDescription(description);

        return ResponseEntity.ok(fileUploadService.uploadAndSave(file, req));
    }
}