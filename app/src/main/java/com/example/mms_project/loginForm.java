package com.example.mms_project;

import androidx.annotation.NonNull;
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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class loginForm extends AppCompatActivity {
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
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


    public void registerSend(View view){
        String email = ((TextView)findViewById(R.id.textEmail)).getText().toString();
        if(!this.checkEmailFormat(email)){ //Not a valid email string
            this.showDialog("Error 03", "This is an invalid email address. Please try again.");
        }
        else if(this.checkEmailDatabase(email)){ //Already exists! Can't register
            this.showDialog("Error 04", "This email is already registered in our Database. Please log in instead.");
        }
        else { //All good
            Intent intent = new Intent(this, registerForm.class);
            intent.putExtra("email-val", email);
            startActivity(intent);
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

    public void logInSend(View view) {
        Log.d("STATE", "Hello there");
        String email = ((TextView)findViewById(R.id.textEmail)).getText().toString();
        String password = ((TextView)findViewById(R.id.textPassword)).getText().toString();
        if(!this.checkEmailFormat(email)){ //Not a valid email string
            this.showDialog("Invalid email", "Please enter a valid email adress");
        }

        mAuth.signInWithEmailAndPassword(email, password);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null && email.equals(currentUser.getEmail())){
            this.showDialog("Loged in with email:", currentUser.getEmail());
            Log.d("STATE", "Lets go mapview");
            Intent intent = new Intent(this, map_activity.class);
            startActivity(intent);
        } else {
            this.showDialog("Incorrect login", "Try again or register");
        }
    }
}