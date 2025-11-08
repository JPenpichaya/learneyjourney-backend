package com.ying.learneyjourney.auth;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseAdminConfig {

    @Value("${app.allow-origins:}")
    private String allowOrigins; // for a clean log that actually reads application.yml

    @PostConstruct
    public void init() throws Exception {
        if (!FirebaseApp.getApps().isEmpty()) return;

        Exception lastError = null;
        GoogleCredentials creds = null;
        String source = "unknown";

        // 1) GOOGLE_APPLICATION_CREDENTIALS
        try {
            String saPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
            if (saPath != null && !saPath.isBlank()) {
                File f = new File(saPath);
                if (f.exists()) {
                    try (FileInputStream in = new FileInputStream(f)) {
                        creds = GoogleCredentials.fromStream(in);
                        source = "GOOGLE_APPLICATION_CREDENTIALS";
                    }
                } else {
                    System.err.println("[Firebase] File not found at GOOGLE_APPLICATION_CREDENTIALS: " + saPath);
                }
            }
        } catch (Exception e) { lastError = e; }

        // 2) ADC
        if (creds == null) {
            try {
                creds = GoogleCredentials.getApplicationDefault();
                source = "Application Default Credentials";
            } catch (Exception e) { lastError = e; }
        }

        // 3) FIREBASE_CONFIG json
        if (creds == null) {
            try {
                String json = System.getenv("FIREBASE_CONFIG");
                if (json != null && !json.isBlank()) {
                    try (ByteArrayInputStream in = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8))) {
                        creds = GoogleCredentials.fromStream(in);
                        source = "FIREBASE_CONFIG";
                    }
                }
            } catch (Exception e) { lastError = e; }
        }

        if (creds == null) {
            throw new IllegalStateException("""
        [Firebase] Could not initialize credentials.
        Tried:
        - GOOGLE_APPLICATION_CREDENTIALS
        - Application Default Credentials (ADC)
        - FIREBASE_CONFIG
        Fix locally: `gcloud auth application-default login` or set GOOGLE_APPLICATION_CREDENTIALS.
        On Cloud Run: attach the correct service account.
      """, lastError);
        }

        // 🔑 Ensure PROJECT ID is set
        String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");
        if (projectId == null || projectId.isBlank()) {
            projectId = System.getenv("GCLOUD_PROJECT");
        }
        if (projectId == null || projectId.isBlank()) {
            projectId = "p-ying-api"; // <-- replace with your real project ID (fallback)
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(creds)
                .setProjectId(projectId) // 👈 REQUIRED for verifyIdToken()
                .build();

        FirebaseApp.initializeApp(options);
        System.out.println("[Firebase] Initialized (source=" + source + ", project=" + projectId + ")");
    }

    @PostConstruct
    public void logCorsConfig() {
        System.out.println("[CORS] app.allow-origins = " + (allowOrigins == null ? "(not set)" : allowOrigins));
    }
}