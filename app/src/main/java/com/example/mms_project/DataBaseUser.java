package com.example.mms_project;

public class DataBaseUser {

    public String first_name;
    public String last_name;
    public String email;
    public String bio;
    public int age;
    public DataBaseUser() {
    }

    public DataBaseUser(String first_name, String last_name, String email, String bio, int age) {
        this.first_name = first_name;
        this.last_name = last_name;
        this.email = email;
        this.bio = bio;
        this.age = age;
    }
}
