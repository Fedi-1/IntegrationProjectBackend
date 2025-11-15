package com.example.IntegrationProjectBackend.models;

import jakarta.persistence.*;
import java.util.List;

@Entity
@DiscriminatorValue("Parent")
@Table(name = "parents")
public class Parent extends User {

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Student> children;

    public Parent() {
        super();
    }

    public Parent(String cin, String firstName, String lastName, String email, String password,
            int age, String phoneNumber, Role role) {
        super(cin, firstName, lastName, email, password, age, phoneNumber, role);
    }

    public List<Student> getChildren() {
        return children;
    }

    public void setChildren(List<Student> children) {
        this.children = children;
    }
}
