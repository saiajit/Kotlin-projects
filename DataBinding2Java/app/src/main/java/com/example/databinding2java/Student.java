package com.example.databinding2java;

public class Student {
    private String name;
    private String email;

    public Student(String name, String email) {
        this.name=name;
        this.email=email;
    }
    public Student(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
