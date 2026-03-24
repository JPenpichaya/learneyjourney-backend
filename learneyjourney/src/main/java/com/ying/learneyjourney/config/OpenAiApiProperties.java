package com.ying.learneyjourney.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "openai")
public class OpenAiApiProperties {

    private String baseUrl;
    private String apiKey;
    private String model;

}