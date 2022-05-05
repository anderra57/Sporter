package com.anderpri.das_grupal.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.anderpri.das_grupal.R;
import com.anderpri.das_grupal.activities.login.LoginMain;
import com.anderpri.das_grupal.controllers.utils.Utils;

public class LauncherActivity extends AppCompatActivity {

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * 1) en sharedpref existe Cookie?
         * 2) si existe, conseguir
         * si no existe -> directamente a LoginMain
         * si existe -> mirar en la bbdd si esa sesion es valida
         * si es válida -> al dash
         * si no es válida -> a LoginMain
         *
         * */

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String cookie = preferences.getString("cookie","no_cookie");
        Log.d("cookie_launcher",cookie);

        if (cookie.equals("no_cookie")){
            openLogin();
        } else {
            if (cookieIsValid()){
                Intent i = new Intent(this, UnaActividad.class);
                startActivity(i);
                finish();
            } else {
                openLogin();
            }
        }
    }

    private boolean cookieIsValid() {
        return false;
    }

    private void openLogin() {
        Intent i = new Intent(this, LoginMain.class);
        startActivity(i);
        finish();
    }
}