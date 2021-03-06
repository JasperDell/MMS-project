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


    public void registerSend(View view){
        String email = ((TextView)findViewById(R.id.textEmail)).getText().toString();
        if(!this.checkEmailFormat(email)){ //Not a valid email string
            this.showDialog("Invalid email", "Please enter a valid email adress");
        } else { //All good
            Intent intent = new Intent(this, registerForm.class);
            intent.putExtra("email-val", email);
            startActivity(intent);
            return;
        }
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
        String email = ((TextView)findViewById(R.id.textEmail)).getText().toString();
        String password = ((TextView)findViewById(R.id.textPassword)).getText().toString();
        if(!this.checkEmailFormat(email)){ //Not a valid email string
            this.showDialog("Invalid email", "Please enter a valid email address");
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password);

        System.out.println("Checking user.............");
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null && email.equals(currentUser.getEmail())){
            this.showDialog("Logged in with email:", currentUser.getEmail());
            Log.d("STATE", "Lets go mapview");
            Intent intent = new Intent(this, map_activity.class);
            startActivity(intent);
        } else {
            this.showDialog("Incorrect login", "Try again or register");
        }
    }
}