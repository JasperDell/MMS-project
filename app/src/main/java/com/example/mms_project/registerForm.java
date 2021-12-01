package com.example.mms_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Context;
import com.example.mms_project.R;
import com.example.mms_project.UserMap;
import com.example.mms_project.map_activity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.Date;

public class registerForm extends AppCompatActivity {
    private FirebaseAuth mAuth;

    String email = null;
    UserMap register = new UserMap();

    private static final int BMAP_DIM = 256;
    public static final int PICK_IMAGE = 1;
    private static Context context;
    DateFormat sdf = new SimpleDateFormat("DD-MM-YYYY");

    ServerConnector server = new ServerConnector();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_register_form);
        context = getApplicationContext();

    }

    public void addIcon(View view){
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");
        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");
        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});
        startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && data != null) {
            //TODO: action
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(data.getData());
                Bitmap b = BitmapFactory.decodeStream(inputStream);
                b = getRoundedCroppedBitmap(b);
                register.icon = b;
                ((ImageView)findViewById(R.id.imageViewIcon)).setImageBitmap(b);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private Bitmap getRoundedCroppedBitmap(Bitmap bitmap) {
        bitmap = Bitmap.createScaledBitmap(bitmap,BMAP_DIM,BMAP_DIM, false);
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

    private boolean checkRegistrationValid(){
        if (((TextView)findViewById(R.id.textFirstName)).getText().toString().isEmpty() || //check first name
                ((TextView)findViewById(R.id.textLastName)).getText().toString().isEmpty() || //check last name
                ((TextView)findViewById(R.id.textBio)).getText().toString().isEmpty()) //check biography
            return false;

        String birthdate = ((TextView)findViewById(R.id.editTextBirthDay)).getText().toString();
        try {
            Date bd = sdf.parse(birthdate);

            if (bd.before(sdf.parse("01-01-1901")) || bd.after(sdf.parse("01-01-2011")))
                return false;
            register.age = Period.between(LocalDate.now(), bd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()).getYears();

        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    private void sendUserToServer(String userID, String first_name, String last_name, String email, String bio, int age){
        DatabaseReference mDatabase = FirebaseDatabase.getInstance("https://mms-project-a6f37-default-rtdb.europe-west1.firebasedatabase.app").getReference("users");
        DataBaseUser dbUser = new DataBaseUser(first_name, last_name, email, bio, age);
        mDatabase.child(userID).setValue(dbUser);
    }

    public void onClickRegister(View view) {
        if(!checkRegistrationValid()){
            return;
        }

        register.firstName = ((TextView)findViewById(R.id.textFirstName)).getText().toString();
        register.lastName =  ((TextView)findViewById(R.id.textLastName)).getText().toString();
        register.bio = ((TextView)findViewById(R.id.textBio)).getText().toString();

        String password = ((TextView)findViewById(R.id.textPassword)).getText().toString();
        String email = ((TextView)findViewById(R.id.textEmail)).getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    sendUserToServer(user.getUid(), register.firstName, register.lastName, email, register.bio, register.age);

                    FirebaseStorage storage = FirebaseStorage.getInstance("gs://mms-project-a6f37.appspot.com/");
                    StorageReference storageRef = storage.getReference();
                    StorageReference imagesRef = storageRef.child("images");
                    StorageReference fileRef = imagesRef.child(user.getUid());

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    register.icon.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] imageData = baos.toByteArray();
                    UploadTask uploadTask = fileRef.putBytes(imageData);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            System.out.println("couldn't upload image");
                        }
                    });
                } else {
                    return;
                }
            }
        });

        //Go to the mapview
        System.out.println("Making the intent map");
        Intent intent = new Intent(this, map_activity.class);
        //intent.putExtra("usr", register);
        intent.putExtra("reg", true);
        intent.putExtra("fname", register.firstName);
        intent.putExtra("lname", register.lastName);
        intent.putExtra("bio", register.bio);
        intent.putExtra("img", register.icon);
        intent.putExtra("age", register.age);
        startActivity(intent);
        System.out.println("Going to the map after registration");
    }
}