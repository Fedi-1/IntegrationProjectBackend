package com.example.IntegrationProjectBackend.models;

import jakarta.persistence.*;

@Entity
@Table(name = "student_subjects", uniqueConstraints = {@UniqueConstraint(columnNames = {"student_id", "subject_id"})})
public class StudentSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subjects subject; 

    public StudentSubject() {}

    public StudentSubject(Student student, Subjects subject) {
        this.student = student;
        this.subject = subject;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public Subjects getSubject() { return subject; }
    public void setSubject(Subjects subject) { this.subject = subject; }
}
