package com.example.mms_project;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class loginForm extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_form);
    }

    private boolean checkEmailFormat(String email){
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);
        if (email == null)
            return false;
        return pat.matcher(email).matches();
    }

    //Function to check if database knows email adress
    private boolean checkEmailDatabase(String email){
        //communicate with server
        if (email.equals("f@g.com"))
            return true;
        return false;
    }

    public void logInSend(View view){
        Log.d("STATE", "Hello there");
        String email = ((TextView)findViewById(R.id.textEmail)).getText().toString();
        if(!this.checkEmailFormat(email)){ //Not a valid email string
            this.showDialog("Error 01", email + " is an invalid email address. Please try again.");
        }
        else if(!this.checkEmailDatabase(email)){ //Email not known in DB
            this.showDialog("Error", "This Email address is unknown to us. Please check for mistakes or press the register button below.");
        }
        else { //All good
            //Continue to personalized screen with user data
            Log.d("STATE", "Lets go mapview");
            Intent intent = new Intent(this, map_activity.class);
            startActivity(intent);
        }
        return;
    }

    public void registerSend(View view){
        String email = findViewById(R.id.textEmail).toString();
        if(!this.checkEmailFormat(email)){ //Not a valid email string
            this.showDialog("Error", "This is an invalid email address. Please try again.");
        }
        else if(this.checkEmailDatabase(email)){ //Already exists! Can't register
            this.showDialog("Error", "This email is already registered in our Database. Please log in instead.");
        }
        else { //All good
            Intent intent = new Intent(this, loginForm.class);
            startActivity(intent);
            intent.putExtra("email-val", email);
            return;
        }
        return;
    }

    private void showDialog(String title, String msg){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialogue);
        ((TextView) dialog.findViewById(R.id.txtTitle)).setText(title);
        ((TextView) dialog.findViewById(R.id.txtDesc)).setText(msg);
        Button btnClose = dialog.findViewById(R.id.btn_ok);
        btnClose.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}