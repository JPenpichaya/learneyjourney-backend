package com.ying.learneyjourney.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@ConfigurationProperties(prefix = "agora")
@Configuration
public class AgoraProperties {

    private String appId;
    private String appCertificate;
    private int expireSeconds = 3600;
}