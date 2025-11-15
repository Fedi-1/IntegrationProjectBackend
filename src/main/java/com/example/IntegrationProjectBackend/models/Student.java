package com.example.IntegrationProjectBackend.models;

import jakarta.persistence.*;
import java.util.List;

@Entity
@DiscriminatorValue("STUDENT")
@Table(name = "students")
public class Student extends User {

    @Column(nullable = false)
    private int maxStudyDuration;

    @Column(nullable = true, length = 20)
    private String parentCin;

    // Time in minutes needed to reach home and be ready after school
    @Column(nullable = true)
    private Integer preparationTimeMinutes;

    @ManyToOne
    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    private Parent parent;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private List<Schedule> schedules;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private List<StudentSubject> studentSubjects;

    public Student() {
    }

    public Student(String cin, String firstName, String lastName, String email, String password,
            int age, String phoneNumber, Role role,
            int maxStudyDuration, String parentCin) {
        super(cin, firstName, lastName, email, password, age, phoneNumber, role);
        this.maxStudyDuration = maxStudyDuration;
        this.parentCin = parentCin;
    }

    // Getters & Setters
    public int getMaxStudyDuration() {
        return maxStudyDuration;
    }

    public void setMaxStudyDuration(int maxStudyDuration) {
        this.maxStudyDuration = maxStudyDuration;
    }

    public String getParentCin() {
        return parentCin;
    }

    public void setParentCin(String parentCin) {
        this.parentCin = parentCin;
    }

    public Parent getParent() {
        return parent;
    }

    public void setParent(Parent parent) {
        this.parent = parent;
    }

    public List<Schedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<Schedule> schedules) {
        this.schedules = schedules;
    }

    public List<StudentSubject> getStudentSubjects() {
        return studentSubjects;
    }

    public void setStudentSubjects(List<StudentSubject> studentSubjects) {
        this.studentSubjects = studentSubjects;
    }

    public Integer getPreparationTimeMinutes() {
        return preparationTimeMinutes;
    }

    public void setPreparationTimeMinutes(Integer preparationTimeMinutes) {
        this.preparationTimeMinutes = preparationTimeMinutes;
    }
}
