package com.example.mms_project.data;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.mms_project.R;
import com.example.mms_project.UserMap;
import com.example.mms_project.map_activity;

public class registerForm extends AppCompatActivity {

    String email = null;
    UserMap register = new UserMap();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_form);

        Bundle b = getIntent().getExtras();
        email = b.getString("email-value");
        ((TextView)findViewById(R.id.textEmail)).setText(email);
        //((TextView)findViewById(R.id.textEmail)).isInEditMode()

    }

    public void addIcon(View view){


    }

    private boolean checkRegistrationValid(){

    }

    public void onClickRegister(){
        if(!checkRegistrationValid()){

            return;
        }

        //Communicate with server and wait for response

        //Go to the mapview
        Intent intent = new Intent(this, map_activity.class);
        startActivity(intent);
    }
}