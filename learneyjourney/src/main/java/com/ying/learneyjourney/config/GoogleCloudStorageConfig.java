package com.ying.learneyjourney.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ImpersonatedCredentials;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.List;

@Configuration
public class GoogleCloudStorageConfig {
    @Bean
    public Storage storage() throws IOException {
        GoogleCredentials creds = GoogleCredentials.getApplicationDefault();
        System.out.println("CREDS CLASS = " + creds.getClass().getName());
        return StorageOptions.getDefaultInstance().getService();
    }
}
