package com.ying.learneyjourney.config;

import com.stripe.Stripe;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {
    public StripeConfig(
            @Value("${stripe.secret-key}") String secretKey
    ) {
        // Initialize Stripe once at startup
        Stripe.apiKey = secretKey;
    }
}
