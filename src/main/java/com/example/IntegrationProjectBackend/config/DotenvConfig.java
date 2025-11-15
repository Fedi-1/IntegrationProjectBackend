package com.example.IntegrationProjectBackend.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class DotenvConfig implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        File envFile = new File(".env");

        if (!envFile.exists()) {
            System.out.println("[DotenvConfig] ⚠️  .env file not found at: " + envFile.getAbsolutePath());
            return;
        }

        try {
            Map<String, Object> envVars = new HashMap<>();
            BufferedReader reader = new BufferedReader(new FileReader(envFile));
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                int separatorIndex = line.indexOf("=");
                if (separatorIndex > 0) {
                    String key = line.substring(0, separatorIndex).trim();
                    String value = line.substring(separatorIndex + 1).trim();
                    envVars.put(key, value);
                    System.out.println("[DotenvConfig] Loaded: " + key + " = "
                            + value.substring(0, Math.min(10, value.length())) + "...");
                }
            }
            reader.close();

            environment.getPropertySources().addFirst(new MapPropertySource("dotenv", envVars));
            System.out.println(
                    "[DotenvConfig] ✓ Successfully loaded " + envVars.size() + " environment variables from .env");

        } catch (Exception e) {
            System.err.println("[DotenvConfig] ❌ Error loading .env file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
