package com.anderpri.das_grupal.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.anderpri.das_grupal.activities.login.LoginMain;
import com.anderpri.das_grupal.activities.login.LoginTeamButtons;
import com.anderpri.das_grupal.controllers.webservices.TeamsWorker;
import com.anderpri.das_grupal.controllers.webservices.UsersWorker;

public class LauncherActivity extends AppCompatActivity {

    SharedPreferences preferences;
    String cookie;

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
        cookie = preferences.getString("cookie","no_cookie");
        Log.d("cookie_launcher",cookie);

        if (cookie.equals("no_cookie")){ // No hay cookoe guradado, por lo que accedemos al login
            openLogin();
        } else {
            cookieIsValid();
        }
    }

    private void cookieIsValid() {
        try {
            // Preparar los datos para enviar al backend
            Data logindata = new Data.Builder()
                    .putString("funcion", "verifysession")
                    .putString("cookie", cookie)
                    .build();

            // Tiene que existir conexión a internet
            Constraints restricciones = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            // Preparar la petición
            OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(UsersWorker.class)
                    .setConstraints(restricciones)
                    .setInputData(logindata)
                    .build();

            // Lanzar la petición
            WorkManager.getInstance(this).getWorkInfoByIdLiveData(req.getId())
                    .observe(this, status -> {
                        if (status != null && status.getState().isFinished()) {
                            //Log.d("sout_wmana",status.getOutputData().getString("datos"));
                            //String cookieIsValid = status.getOutputData().getString("datos").trim();
                            String result = status.getOutputData().getString("datos").trim();
                            System.out.println("Res " + result);
                            if (!result.isEmpty()) {
                                openLogin();
                            } else {
                                Intent i = new Intent(this, UnaActividad.class);
                                startActivity(i);
                                finish();
                            }
                        }
                    });

            WorkManager.getInstance(this).enqueue(req);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openLogin() {
        Intent i = new Intent(this, LoginMain.class);
        startActivity(i);
        finish();
    }
}