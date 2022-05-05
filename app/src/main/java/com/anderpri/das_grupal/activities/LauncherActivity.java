package com.anderpri.das_grupal.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.anderpri.das_grupal.R;
import com.anderpri.das_grupal.activities.login.LoginMain;
import com.anderpri.das_grupal.controllers.utils.Utils;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Intent intentOnline = new Intent(this, LoginMain.class);
        Utils.getInstance().setApplicationContext(this.getApplicationContext());

        // TODO comprobar que está logged
        boolean logged = false;

        Intent i;
        if (logged){
            // TODO pasarle la información
            i = new Intent(this, UnaActividad.class);
        } else {
            i = new Intent(this, LoginMain.class);
        }
        startActivity(i);
        finish();
    }
}