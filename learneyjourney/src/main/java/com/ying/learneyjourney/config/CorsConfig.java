package com.ying.learneyjourney.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {
        @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(List.of(
                "https://www.learneyjourney.com",
                "https://learneyjourney.com"
        ));          // use addAllowedOrigin for exact origins
        cfg.setAllowCredentials(true);              // if you ever send cookies (not needed for bearer tokens)
        cfg.setAllowedHeaders(List.of("Authorization","Content-Type","Accept","X-Requested-With", "Origin"));
        cfg.setExposedHeaders(List.of("Authorization"));                    // or be explicit: Authorization, Content-Type
        cfg.addAllowedMethod(HttpMethod.GET);
        cfg.addAllowedMethod(HttpMethod.POST);
        cfg.addAllowedMethod(HttpMethod.PUT);
        cfg.addAllowedMethod(HttpMethod.DELETE);
        cfg.addAllowedMethod(HttpMethod.PATCH);
        cfg.addAllowedMethod(HttpMethod.OPTIONS);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
//    @Bean
//    CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration cfg = new CorsConfiguration();
//        cfg.setAllowedOrigins(List.of("*"));                 // tighten to your site in prod
//        cfg.setAllowedMethods(List.of("POST", "GET", "OPTIONS"));
//        cfg.setAllowedHeaders(List.of("*"));
//        cfg.setAllowCredentials(false);
//        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
//        src.registerCorsConfiguration("/**", cfg);
//        return src;
//    }
}
