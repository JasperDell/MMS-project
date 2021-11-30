package com.example.mms_project;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.PersistableBundle;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;



// Implement OnMapReadyCallback.
public class map_activity extends AppCompatActivity implements OnMapReadyCallback {

    //Session info
    MapView mapView;
    GoogleMap googleMap;
    String markerSelection = "";

    //User information
    Location currentLocation;
    UserMap person_inf;
    List<UserMap> local_inf = new ArrayList<UserMap>();

    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101;
    private static final int BMAP_DIM = 256;
    private static final float MAP_ZOOM = 19.25F;



    //Test function to fake user info with fake intel
    private void set_defaults(UserMap curr){

        if(curr != null){
            person_inf = curr;
            person_inf.setLastLoc(52.08545962113923, 4.33562457561493);
        }
        else {
            //set user
            person_inf = new UserMap();
            person_inf.setLastLoc(52.08545962113923, 4.33562457561493);
            //person_inf.setIcon(R.drawable.default_man);
            person_inf.icon = ((BitmapDrawable) getDrawable(R.drawable.default_man)).getBitmap();
            person_inf.firstName = "Me";
        }

        //set neighbourhood
        UserMap temp_inf = new UserMap();
        temp_inf.setLastLoc(52.08566069844681, 4.335941076278687);
        temp_inf.icon = ((BitmapDrawable) getDrawable(R.drawable.default_woman)).getBitmap();
        temp_inf.firstName = "Other";
        temp_inf.markerTagId = local_inf.size(); //set marker index to local_inf index
        temp_inf.pers_available = true;
        temp_inf.pers_nudgeable = false;
        temp_inf.subtitle = "General person";
        temp_inf.age = 50;
        temp_inf.bio = "At vero eos et accusamus et iusto odio dignissimos ducimus qui blanditiis praesentium voluptatum deleniti atque corrupti quos dolores et quas molestias excepturi sint occaecati cupiditate non provident, similique sunt in culpa qui officia deserunt mollitia animi, id est laborum et dolorum fuga. Et harum quidem rerum facilis est et expedita distinctio. Nam libero tempore, cum soluta nobis est eligendi optio cumque nihil impedit quo minus id quod maxime placeat facere possimus, omnis voluptas assumenda est, omnis dolor repellendus. Temporibus autem quibusdam et aut officiis debitis aut rerum necessitatibus saepe eveniet ut et voluptates repudiandae sint et molestiae non recusandae. Itaque earum rerum hic tenetur a sapiente delectus, ut aut reiciendis voluptatibus maiores alias consequatur aut perferendis doloribus asperiores repellat";
        local_inf.add(temp_inf);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("Starting up map");

        //UserMap reg = (UserMap) getIntent().getSerializableExtra("usr");
        Boolean reg = (boolean) getIntent().getExtras().get("reg");
        if(reg != null && reg){
            UserMap register = new UserMap();

            register.firstName = (String) getIntent().getExtras().get("fname");
            register.lastName = (String) getIntent().getExtras().get("lname");
            register.bio = (String) getIntent().getExtras().get("bio");
            register.age = (int) getIntent().getExtras().get("age");
            register.icon = (Bitmap) getIntent().getExtras().get("img");
            set_defaults(register);
        }
        else {
            set_defaults(null);
        }

        super.onCreate(savedInstanceState);
        //Set up location services
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLocation();

        // Set the layout file as the content view.
        setContentView(R.layout.activity_map);


        mapView = findViewById(R.id.mapView);
        System.out.println(mapView);

        // Get a handle to the fragment and register the callback.
        //SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
         //       .findFragmentById(R.id.mapView);
        //mapFragment.getMapAsync(this);

        System.out.println("Lets hit it");
        if(checkGooglePlayServices()){
            System.out.println("We got sooo faaaar");
            mapView.getMapAsync(this);
            mapView.onCreate(savedInstanceState);
        }
        else {
            Toast.makeText(this, "Google Play Services not available.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }
        System.out.println("Getting location!");
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    //Change code to update the user's information
                    currentLocation = location;
                    //dropMarker(currentLocation, person_inf, true);
                    dropMarker(person_inf, true);
                    dropNeighbourhood();
                }
            }
        });
    }

    private boolean checkGooglePlayServices(){
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int result = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (result == ConnectionResult.SUCCESS){
            return true;
        }
        else if (googleApiAvailability.isUserResolvableError(result)){
            Dialog dialog = googleApiAvailability.getErrorDialog(this, result, 201, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Toast.makeText(map_activity.this, "User Cancelled Dialog", Toast.LENGTH_SHORT).show();
                }
            });
            dialog.show();
        }
        return false;
    }

    // Get a handle to the GoogleMap object and display marker.
    @Override
    public void onMapReady(GoogleMap gMap) {
        googleMap = gMap;
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                System.out.println(marker.getId());
                System.out.println(markerSelection);
                if(!((UserMap)marker.getTag()).clickable){ //Selected current user
                    markerSelection = "";
                    return false;
                }
                if (markerSelection.equals(marker.getId())){ //Double clicked marker, show info
                    System.out.println("Time to show user info");
                    UserMap user = ((UserMap)marker.getTag());
                    showUserInfo(user);
                }
                else { //Selected different marker, update variable
                    markerSelection = marker.getId();
                }

                return false;
            }
        });
    }

    public void showUserInfo(@NonNull UserMap user){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.user_map_info);
        String title = user.firstName + " (" + String.valueOf(user.age) + ')';
        ((TextView) dialog.findViewById(R.id.txtTitle)).setText(title);
        ((TextView) dialog.findViewById(R.id.txtDesc)).setText(user.subtitle);
        ((TextView) dialog.findViewById(R.id.txtDescLarge)).setText(user.bio);

        Button btnNudge = dialog.findViewById(R.id.btn_nudge);

        if (user.pers_nudgeable){
            btnNudge.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    //send msg to server
                }
            });
        }
        else {
            btnNudge.setBackgroundColor(Color.GRAY);
            btnNudge.setEnabled(false);
        }

        Button btnClose = dialog.findViewById(R.id.btn_ok);
        btnClose.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void fetchNeighbourhood(){

    }

    public void dropNeighbourhood(){
        for(int i = 0; i < local_inf.size(); i++){
            UserMap nb = local_inf.get(i);
            if (nb.pers_available) {
                dropMarker(nb, false);
            }
        }
    }

    public void dropMarker(@NonNull UserMap user, boolean focus) {
        if (googleMap == null)
            return;
        LatLng latLng = new LatLng(user.lastLoc.getLatitude(), user.lastLoc.getLongitude());
        Bitmap bmap = user.icon;
        BitmapDescriptor res = BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bmap,BMAP_DIM,BMAP_DIM, false));
        Marker m = googleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title(user.firstName)
                .icon(res)
        );
        m.setTag(user);

        if(focus){
            //googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_ZOOM)); //zoom is in range [2.0, 21.0]
        }
    }

    //Function to set personal_info
    private boolean getUserInfo(){

        return true;
    }

    //Function to get people near you, returns the quantity
    private int getLocalInfo(){
        int num_people = 0;

        return num_people;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchLocation();
                }
                break;
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mapView.onResume();

    }

    @Override
    protected void onPause(){
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop(){
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState){
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory(){
        super.onLowMemory();
        mapView.onLowMemory();
    }
}