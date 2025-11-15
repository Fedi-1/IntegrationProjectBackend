package com.example.IntegrationProjectBackend.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "generated_schedules")
public class GeneratedSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false)
    private String day;

    @Column(nullable = false)
    private String timeSlot;

    @Column(nullable = false)
    private String activity;

    @Column
    private Integer durationMinutes;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subjects subject;

    @Column
    private String topic;

    @Column(nullable = false)
    private LocalDateTime generatedAt;

    @Column
    private String sessionId;

    @Column
    private Boolean completed;

    @Column
    private LocalDateTime completedAt;

    public GeneratedSchedule() {
        this.generatedAt = LocalDateTime.now();
        this.completed = false;
    }

    public GeneratedSchedule(Student student, String day, String timeSlot, String activity,
            Integer durationMinutes, Subjects subject, String topic, String sessionId) {
        this.student = student;
        this.day = day;
        this.timeSlot = timeSlot;
        this.activity = activity;
        this.durationMinutes = durationMinutes;
        this.subject = subject;
        this.topic = topic;
        this.sessionId = sessionId;
        this.generatedAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

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

    public Subjects getSubject() {
        return subject;
    }

    public void setSubject(Subjects subject) {
        this.subject = subject;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
