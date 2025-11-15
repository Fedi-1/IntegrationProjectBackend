package com.example.IntegrationProjectBackend.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class ScheduleGenerationRequest {

    private String name;
    private Integer age;
    private List<SubjectInfo> subjects;

    @JsonProperty("max_study_duration") 
    private Integer maxStudyDuration;

    private Map<String, String> examDates;
    private Map<String, Object> learningPreferences;
    private Map<String, Object> constraints;

    public ScheduleGenerationRequest() {
    }

    // Getters & Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public List<SubjectInfo> getSubjects() {
        return subjects;
    }

    public void setSubjects(List<SubjectInfo> subjects) {
        this.subjects = subjects;
    }

    public Integer getMaxStudyDuration() {
        return maxStudyDuration;
    }

    public void setMaxStudyDuration(Integer maxStudyDuration) {
        this.maxStudyDuration = maxStudyDuration;
    }

    public Map<String, String> getExamDates() {
        return examDates;
    }

    public void setExamDates(Map<String, String> examDates) {
        this.examDates = examDates;
    }

    public Map<String, Object> getLearningPreferences() {
        return learningPreferences;
    }

    public void setLearningPreferences(Map<String, Object> learningPreferences) {
        this.learningPreferences = learningPreferences;
    }

    public Map<String, Object> getConstraints() {
        return constraints;
    }

    public void setConstraints(Map<String, Object> constraints) {
        this.constraints = constraints;
    }

    public static class SubjectInfo {
        private String name;
        private String difficulty;
        private Integer hoursPerWeek;

        public SubjectInfo() {
        }

        // Getters & Setters
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDifficulty() {
            return difficulty;
        }

        public void setDifficulty(String difficulty) {
            this.difficulty = difficulty;
        }

        public Integer getHoursPerWeek() {
            return hoursPerWeek;
        }

        public void setHoursPerWeek(Integer hoursPerWeek) {
            this.hoursPerWeek = hoursPerWeek;
        }
    }
}
