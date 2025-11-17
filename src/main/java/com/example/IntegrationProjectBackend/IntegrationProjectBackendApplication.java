package com.example.IntegrationProjectBackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import jakarta.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class IntegrationProjectBackendApplication {

	@PostConstruct
	public void init() {
		// Set default timezone to Tunisia
		TimeZone.setDefault(TimeZone.getTimeZone("Africa/Tunis"));
		System.out.println("🕐 Application timezone set to: " + TimeZone.getDefault().getID());
	}

	public static void main(String[] args) {
		// Debug: Print database configuration to verify environment variables
		System.out.println("===========================================");
		System.out.println("DATABASE CONFIGURATION:");
		System.out.println("SPRING_DATASOURCE_URL: " + System.getenv("SPRING_DATASOURCE_URL"));
		System.out.println("SPRING_DATASOURCE_USERNAME: " + System.getenv("SPRING_DATASOURCE_USERNAME"));
		System.out.println("GROQ_API_KEY present: " + (System.getenv("GROQ_API_KEY") != null));
		System.out.println("===========================================");

		SpringApplication.run(IntegrationProjectBackendApplication.class, args);
	}

}
