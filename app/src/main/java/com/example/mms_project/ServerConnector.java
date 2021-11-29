package com.example.mms_project;

import android.location.Location;

public class ServerConnector {

    public ServerConnector (){

    }

    //Request information for a specific user
    public UserMap getUserInfo(String userId){
        UserMap user = new UserMap();

        return user;
    }

    public boolean emailKnown(String email){

        return true;
    }

    public boolean registerUser(String email, UserMap user){

        return true;
    }

    public boolean sendLatLong(String email, double lat, double lng){

        return true;
    }

    public Location getLatLong(String email){
        Location loc = new Location("");

        return loc;
    }
}
