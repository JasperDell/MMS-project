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
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;



// Implement OnMapReadyCallback.
public class map_activity extends AppCompatActivity implements OnMapReadyCallback {
    private FirebaseAuth mAuth;
    //Session info
    MapView mapView;
    GoogleMap googleMap;
    String markerSelection = "";

    UserMap person_info;
    //User information

    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101;
    private static final int BMAP_DIM = 256;
    private static final float MAP_ZOOM = 19.25F;


    private Bitmap getRoundedCroppedBitmap(Bitmap bitmap) {
        bitmap = Bitmap.createScaledBitmap(bitmap, BMAP_DIM, BMAP_DIM, false);
        int widthLight = bitmap.getWidth();
        int heightLight = bitmap.getHeight();
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paintColor = new Paint();
        paintColor.setFlags(Paint.ANTI_ALIAS_FLAG);
        RectF rectF = new RectF(new Rect(0, 0, widthLight, heightLight));
        canvas.drawRoundRect(rectF, widthLight / 2, heightLight / 2, paintColor);
        Paint paintImage = new Paint();
        paintImage.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        canvas.drawBitmap(bitmap, 0, 0, paintImage);
        return output;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        person_info = new UserMap();
        System.out.println("Starting up map");
        super.onCreate(savedInstanceState);
        //Set up location services
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            DatabaseReference mDatabase = FirebaseDatabase.getInstance("https://mms-project-a6f37-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
            mDatabase.child("users").child(currentUser.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.isSuccessful()) {
                        System.out.println("Failed to get user string info");
                        person_info.firstName = (String) getIntent().getExtras().get("fname");
                        person_info.lastName = (String) getIntent().getExtras().get("lname");
                        person_info.bio = (String) getIntent().getExtras().get("bio");
                        person_info.age = (int) getIntent().getExtras().get("age");
                    }
                    else {
                        System.out.println("Got user string info!");
                        HashMap<String, Object> result = (HashMap<String, Object>) task.getResult().getValue();
                        person_info.firstName = (String) result.get("first_name");
                        person_info.lastName = (String) result.get("last_name");
                        person_info.bio = (String) result.get("bio");
                        person_info.age = ((Long) result.get("age")).intValue();
                    }
                }
            });

            FirebaseStorage storage = FirebaseStorage.getInstance("gs://mms-project-a6f37.appspot.com/");
            StorageReference storageRef = storage.getReference();
            StorageReference imagesRef = storageRef.child("images");
            StorageReference fileRef = imagesRef.child(currentUser.getUid());
            final long ONE_MEGABYTE = 1024 * 1024;
            fileRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    System.out.println("Got the bitmap");
                    person_info.icon = bitmap;
                    fetchLocation();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    person_info.icon = ((BitmapDrawable) getDrawable(R.drawable.default_man)).getBitmap();
                }
            });
        }
        // Set the layout file as the content view.
        setContentView(R.layout.activity_map);
        mapView = findViewById(R.id.mapView);
        System.out.println(mapView);

        if(checkGooglePlayServices()){
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
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    //Change code to update the user's information
                    person_info.lastLoc = location;
                    dropMarker(person_info, true);
                    fetchNeighbourhood();
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
        List<UserMap> local_inf = new ArrayList<UserMap>();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance("https://mms-project-a6f37-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        mDatabase.child("users").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    System.out.println("Failed to get users");
                } else {
                    Iterator<DataSnapshot> x  = task.getResult().getChildren().iterator();
                    for (Iterator<DataSnapshot> it = x; it.hasNext(); ) {
                        UserMap person_inf = new UserMap();
                        DataSnapshot i = it.next();
                        HashMap<String, Object> result = (HashMap<String, Object>) i.getValue();
                        person_inf.uID = i.getKey();
                        person_inf.firstName = (String) result.get("first_name");
                        person_inf.lastName = (String) result.get("last_name");
                        person_inf.bio = (String) result.get("bio");
                        person_inf.age = ((Long) result.get("age")).intValue();
                        person_inf.lastLoc.setLatitude(Double.parseDouble((String)result.get("lat")));
                        person_inf.lastLoc.setLongitude(Double.parseDouble((String)result.get("lon")));
                        if (person_inf.uID != mAuth.getCurrentUser().getUid()) {
                            dropMarker(person_inf, false);
                        }
                    }
                }
            }
        });
    }

    public void dropMarker(@NonNull UserMap user, boolean focus) {
        if (googleMap == null)
            return;
        LatLng latLng = new LatLng(user.lastLoc.getLatitude(), user.lastLoc.getLongitude());
        if (!(user.uID.equals(""))) {
            FirebaseStorage storage = FirebaseStorage.getInstance("gs://mms-project-a6f37.appspot.com/");
            StorageReference storageRef = storage.getReference();
            StorageReference imagesRef = storageRef.child("images");
            StorageReference fileRef = imagesRef.child(user.uID);
            final long ONE_MEGABYTE = 1024 * 1024;
            fileRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    System.out.println("Cropping Bitmap");
                    bitmap = getRoundedCroppedBitmap(bitmap);
                    BitmapDescriptor res = BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bitmap, BMAP_DIM, BMAP_DIM, false));
                    Marker m = googleMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(user.firstName)
                            .icon(res)
                    );
                    m.setTag(user);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Bitmap bmap = ((BitmapDrawable) getDrawable(R.drawable.default_man)).getBitmap();
                    BitmapDescriptor res = BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bmap, BMAP_DIM, BMAP_DIM, false));
                    Marker m = googleMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(user.firstName)
                            .icon(res)
                    );
                    m.setTag(user);
                }
            });
        } else{
            Bitmap bmap = getRoundedCroppedBitmap(user.icon);
            BitmapDescriptor res = BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bmap,BMAP_DIM,BMAP_DIM, false));
            Marker m = googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(user.firstName)
                    .icon(res)
            );
            m.setTag(user);
        }

        if(focus){
            //googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_ZOOM)); //zoom is in range [2.0, 21.0]
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