package com.ying.learneyjourney.checker;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class Checker {

    public void EnvDebug(org.springframework.core.env.Environment env) {
        System.out.println("Active profiles: " + String.join(",", env.getActiveProfiles()));
        System.out.println("DATABASE_URL (Spring) = " + env.getProperty("DATABASE_URL"));
    }
    @PostConstruct
    public void debugEnv() {
        System.out.println("DATABASE_URL = " + System.getenv("DATABASE_URL"));
    }
}
