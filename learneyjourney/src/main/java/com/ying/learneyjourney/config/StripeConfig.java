package com.ying.learneyjourney.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@ConfigurationProperties(prefix = "stripe")
@Profile("!test")
@Getter
@Setter
public class StripeConfig {
    private String secretKey;
    private String webhookSecret;
    private Identity identity = new Identity();
    private Connect connect = new Connect();

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    @Getter @Setter
    public static class Identity {
        private String returnUrl;
    }

    @Getter @Setter
    public static class Connect {
        private String refreshUrl;
        private String returnUrl;
    }
}