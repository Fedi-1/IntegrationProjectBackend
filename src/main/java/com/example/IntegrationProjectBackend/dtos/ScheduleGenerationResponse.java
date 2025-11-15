package com.example.IntegrationProjectBackend.dtos;

import java.util.Map;

public class ScheduleGenerationResponse {

    private Map<String, Map<String, ActivityBlock>> schedule;
    private String message;
    private boolean success;

    public ScheduleGenerationResponse() {
    }

    public ScheduleGenerationResponse(Map<String, Map<String, ActivityBlock>> schedule, String message,
            boolean success) {
        this.schedule = schedule;
        this.message = message;
        this.success = success;
    }

    // Getters & Setters
    public Map<String, Map<String, ActivityBlock>> getSchedule() {
        return schedule;
    }

    public void setSchedule(Map<String, Map<String, ActivityBlock>> schedule) {
        this.schedule = schedule;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public static class ActivityBlock {
        private String activity;
        private Integer durationMinutes;
        private String subject;
        private String topic;

        public ActivityBlock() {
        }

        // Getters & Setters
        public String getActivity() {
            return activity;
        }

        public void setActivity(String activity) {
            this.activity = activity;
        }

        public Integer getDurationMinutes() {
            return durationMinutes;
        }

        public void setDurationMinutes(Integer durationMinutes) {
            this.durationMinutes = durationMinutes;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }
    }
}
