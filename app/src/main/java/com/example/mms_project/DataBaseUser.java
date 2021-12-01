package com.example.mms_project;

public class DataBaseUser {

    public String first_name;
    public String last_name;
    public String email;
    public String bio;
    public int age;
    public String lat;
    public String lon;
    public DataBaseUser() {
    }

    public DataBaseUser(String lat, String lon, String first_name, String last_name, String email, String bio, int age) {
        this.lat = lat;
        this.lon = lon;
        this.first_name = first_name;
        this.last_name = last_name;
        this.email = email;
        this.bio = bio;
        this.age = age;
    }
}
