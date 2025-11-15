package com.example.IntegrationProjectBackend.models;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "subjects")
public class Subjects {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL)
    private List<StudentSubject> studentSubjects;

    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL)
    private List<Schedule> schedules;

    public Subjects() {}

    public Subjects(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<StudentSubject> getStudentSubjects() { return studentSubjects; }
    public void setStudentSubjects(List<StudentSubject> studentSubjects) { this.studentSubjects = studentSubjects; }
    public List<Schedule> getSchedules() { return schedules; }
    public void setSchedules(List<Schedule> schedules) { this.schedules = schedules; }
}
