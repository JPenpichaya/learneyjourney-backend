package com.ying.learneyjourney.checker;

import org.springframework.stereotype.Component;

@Component
class EnvPrint {
    EnvPrint(org.springframework.core.env.Environment env) {
        System.out.println("Active profiles: " + String.join(",", env.getActiveProfiles()));
        System.out.println("DATABASE_URL: " + env.getProperty("DATABASE_URL"));
    }
}