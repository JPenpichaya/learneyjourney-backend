package com.ying.learneyjourney.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StartupDebugConfig {

    @Value("${spring.datasource.url:NOT_SET}")
    private String datasourceUrl;

    @Value("${spring.datasource.username:NOT_SET}")
    private String datasourceUsername;

    @PostConstruct
    public void logConfig() {
        System.out.println("spring.datasource.url = " + datasourceUrl);
        System.out.println("spring.datasource.username = " + datasourceUsername);
        System.out.println("DATABASE_URL env exists = " + (System.getenv("DATABASE_URL") != null));
        System.out.println("DATABASE_USERNAME env exists = " + (System.getenv("DATABASE_USERNAME") != null));
        System.out.println("DATABASE_PASSWORD env exists = " + (System.getenv("DATABASE_PASSWORD") != null));
        System.out.println("SPRING_PROFILES_ACTIVE = " + System.getenv("SPRING_PROFILES_ACTIVE"));
    }
}
