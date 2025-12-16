package com.ying.learneyjourney.log;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DbDebugRunner implements CommandLineRunner {

    @Value("${spring.profiles.active:}")
    private String active;

    @Value("${spring.datasource.url:}")
    private String url;

    @Value("${spring.datasource.username:}")
    private String user;

    private final Environment env;

    public DbDebugRunner(Environment env) {
        this.env = env;
    }

    @Override
    public void run(String... args) {
        System.out.println("ACTIVE_PROFILES=" + String.join(",", env.getActiveProfiles()));
        System.out.println("spring.profiles.active=" + active);
        System.out.println("spring.datasource.url=" + url);
        System.out.println("spring.datasource.username=" + user);
    }
}
