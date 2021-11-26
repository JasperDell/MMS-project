package com.example.mms_project;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;

import java.io.File;

public class UserMap {

    //Basic map information
    public Location lastLoc = new Location("");
    Bitmap icon;
    boolean clickable = true;
    int markerTagId = -1;

    //Profile view information
    public String firstName = "";
    public String lastName = "";
    public int age;
    public boolean pers_available; //Ready to meet?
    public boolean pers_nudgeable; //Can the user request a meeting?
    public String bio = ""; //Detailed profile description made by user


    public UserMap(){

    }

    public void setLastLoc(double lat, double lng){
        lastLoc.setLatitude(lat);
        lastLoc.setLongitude(lng);
    }

    public void setIcon(String file){
        File imgFile = new  File("C:\\Users\\Thijs\\Documents\\GitHub\\MMS-project\\app\\src\\main\\res\\drawable\\" + file);
        String workingDir = System.getProperty("user.dir");
        System.out.println("Current working directory : " + workingDir);
        if(imgFile.exists()){
            icon = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        }
    }

}