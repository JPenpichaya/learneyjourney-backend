package com.ying.learneyjourney.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
public class AuditorConfig {
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth == null || !auth.isAuthenticated()) {
                return Optional.of("system"); // fallback
            }

            Object principal = auth.getPrincipal();

            if (principal instanceof String s && !s.isBlank()) {
                return Optional.of(s); // this is the Firebase uid
            }


            return Optional.of("system");
        };
    }
}
