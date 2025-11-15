package com.example.IntegrationProjectBackend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * Service to test connectivity with Flask ML service
 */
@Service
public class MLServiceHealthCheckService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ml.service.url:http://127.0.0.1:5000}")
    private String mlServiceUrl;

    /**
     * Check if Flask ML service is accessible
     * 
     * @return true if service is reachable, false otherwise
     */
    public boolean isMLServiceAvailable() {
        try {
            restTemplate.getForEntity(mlServiceUrl, String.class);
            return true;
        } catch (Exception e) {
            System.err.println("ML Service is not available at: " + mlServiceUrl);
            System.err.println("Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get ML service URL
     * 
     * @return configured ML service URL
     */
    public String getMLServiceUrl() {
        return mlServiceUrl;
    }

    /**
     * Get ML service status information
     * 
     * @return status message
     */
    public String getMLServiceStatus() {
        if (isMLServiceAvailable()) {
            return "ML Service is running at: " + mlServiceUrl;
        } else {
            return "ML Service is NOT available at: " + mlServiceUrl +
                    "\nPlease make sure Flask app.py is running on port 5000";
        }
    }
}
