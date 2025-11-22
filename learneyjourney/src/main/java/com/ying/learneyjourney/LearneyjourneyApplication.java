package com.ying.learneyjourney;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@SpringBootApplication
public class LearneyjourneyApplication {

	public static void main(String[] args) {
		SpringApplication.run(LearneyjourneyApplication.class, args);
	}

}
