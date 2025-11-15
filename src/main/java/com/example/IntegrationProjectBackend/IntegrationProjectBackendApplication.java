package com.example.IntegrationProjectBackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IntegrationProjectBackendApplication {

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
