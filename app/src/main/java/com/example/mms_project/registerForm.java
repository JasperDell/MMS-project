package com.example.mms_project;

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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class registerForm extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

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
        mDatabase = FirebaseDatabase.getInstance().getReference();
        setContentView(R.layout.activity_register_form);

        Bundle b = getIntent().getExtras();
        email = b.getString("email-value");
        ((TextView)findViewById(R.id.textViewEmail)).setText(email);

        context = getApplicationContext();

    }

    private void showDialog(String title, String msg){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialogue);
        ((TextView) dialog.findViewById(R.id.txtTitle)).setText(title);
        ((TextView) dialog.findViewById(R.id.txtDesc)).setText(msg);
        Button btnClose = dialog.findViewById(R.id.btn_ok);
        btnClose.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
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

        } catch (ParseException e) {
            return false;
        }

        return true;
    }

    private void sendUserToServer(){

    }

    public void onClickRegister(View view) {
        if(!checkRegistrationValid()){
            this.showDialog("Error 11", "register not valid");
            return;
        }
        String password = "test123";
        if (email == null){
            this.showDialog("Error 11", "email=null");
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            this.showDialog("Error 11", "Can't login firebase");
            return;
        }

        register.firstName = ((TextView)findViewById(R.id.textFirstName)).getText().toString();
        register.lastName =  ((TextView)findViewById(R.id.textLastName)).getText().toString();
        register.bio = ((TextView)findViewById(R.id.textBio)).getText().toString();


        //Communicate with server and wait for response
        sendUserToServer();

        //mDatabase.child("users").child(currentUser.getUid()).setValue(register);
        //UserMap test = (UserMap) mDatabase.child("users").child(currentUser.getUid()).get().getResult().getValue();

        //Go to the mapview
        Intent intent = new Intent(this, map_activity.class);
        startActivity(intent);
    }
}