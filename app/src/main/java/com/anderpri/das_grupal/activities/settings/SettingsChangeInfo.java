package com.anderpri.das_grupal.activities.settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.anderpri.das_grupal.R;
import com.anderpri.das_grupal.activities.ListaActividadesInscrito;
import com.anderpri.das_grupal.controllers.utils.Utils;
import com.anderpri.das_grupal.controllers.webservices.TeamsWorker;
import com.anderpri.das_grupal.controllers.webservices.UsersWorker;

public class SettingsChangeInfo extends AppCompatActivity {

    EditText pass_team_old, pass_team_new, pass_user_old, pass_user_new;
    String token,username,teamname,cookie;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String str = preferences.getString("lang","es");
        Utils.getInstance().setLocale(str,getBaseContext());
        boolean dark = preferences.getBoolean("dark",false);
        Utils.getInstance().setTheme(dark);

        setContentView(R.layout.activity_settings_change_info);
        pass_user_new = findViewById(R.id.settings_change_pass_user_txt_new);
        pass_user_old = findViewById(R.id.settings_change_pass_user_txt_old);
        pass_team_new = findViewById(R.id.settings_change_pass_team_txt_new);
        pass_team_old = findViewById(R.id.settings_change_pass_team_txt_old);

        token = preferences.getString("token",null);
        username = preferences.getString("username",null);
        cookie = preferences.getString("cookie",null);
        getTeamName();

    }

    // USERNAME CHANGE

    public void onUpdateUser(View view) {
        checkPassUser();
    }

    private void checkPassUser() {

        String password = pass_user_old.getText().toString();

        // Los campos del login no pueden estar vacios
        if(username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.login_no_empty_field), Toast.LENGTH_SHORT).show();
        } else {
            // login al usuario en la aplicación
            try {
                // Preparar los datos para enviar al backend
                Data logindata = new Data.Builder()
                        .putString("funcion", "login")
                        .putString("username", username)
                        .putString("password", password)
                        .putString("token", token)
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
                                String id_user = status.getOutputData().getString("datos").trim();
                                if(!id_user.isEmpty()) {
                                    // login correcto
                                    Log.d("debug_pass","login correcto");
                                    changePassUser();
                                } else {
                                    Toast.makeText(this, getString(R.string.settings_wrong_pass), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                WorkManager.getInstance(this).enqueue(req);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void changePassUser() {

        String password = pass_user_new.getText().toString();

        // Los campos del login no pueden estar vacios
        if(username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.login_no_empty_field), Toast.LENGTH_SHORT).show();
        } else {
            // login al usuario en la aplicación
            try {
                // Preparar los datos para enviar al backend
                Data logindata = new Data.Builder()
                        .putString("funcion", "updatepass")
                        .putString("username", username)
                        .putString("password", password)
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
                                String id_user = status.getOutputData().getString("datos").trim();
                                if(id_user.isEmpty()) {
                                    Toast.makeText(this, getString(R.string.settings_pass_updated), Toast.LENGTH_SHORT).show();
                                    Log.d("debug_pass","updated");
                                } else {
                                    Log.d("debug_pass","not updated");
                                }
                            }
                        });

                WorkManager.getInstance(this).enqueue(req);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // TEAMNAME CHANGE

    public void onUpdateTeam(View view) {
        checkPassTeam();
    }

    public void checkPassTeam() {

        String password = pass_team_old.getText().toString();

        // Los campos del login no pueden estar vacios
        if(teamname.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.login_no_empty_field), Toast.LENGTH_SHORT).show();
        } else {
            // login al usuario en la aplicación
            try {
                // Preparar los datos para enviar al backend
                Data logindata = new Data.Builder()
                        .putString("funcion", "login")
                        .putString("teamname", teamname)
                        .putString("teampass", password)
                        .putString("cookie", cookie)
                        .build();

                // Tiene que existir conexión a internet
                Constraints restricciones = new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build();

                // Preparar la petición
                OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(TeamsWorker.class)
                        .setConstraints(restricciones)
                        .setInputData(logindata)
                        .build();

                // Lanzar la petición
                WorkManager.getInstance(this).getWorkInfoByIdLiveData(req.getId())
                        .observe(this, status -> {
                            if (status != null && status.getState().isFinished()) {
                                String id_team = status.getOutputData().getString("datos").trim();
                                if(id_team.isEmpty()) {
                                    Log.d("debug_pass","login correcto");
                                    changePassTeam();
                                } else {
                                    Toast.makeText(this, getString(R.string.settings_wrong_pass), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                WorkManager.getInstance(this).enqueue(req);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void getTeamName() {
        try {
            // Preparar los datos para enviar al backend
            Data logindata = new Data.Builder()
                    .putString("funcion", "getteamname")
                    .putString("username", username)
                    .putString("cookie", cookie)
                    .build();

            // Tiene que existir conexión a internet
            Constraints restricciones = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            // Preparar la petición
            OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(TeamsWorker.class)
                    .setConstraints(restricciones)
                    .setInputData(logindata)
                    .build();

            // Lanzar la petición
            WorkManager.getInstance(this).getWorkInfoByIdLiveData(req.getId())
                    .observe(this, status -> {
                        if (status != null && status.getState().isFinished()) {
                            String name = status.getOutputData().getString("datos").trim();
                            Log.d("debug_name",name);
                            if(!name.isEmpty()) {
                                teamname = name;
                                Log.d("debug_name","got it");
                                Log.d("debug_name",name);
                            } else {
                                Log.d("debug_name","not got");
                            }
                        }
                    });

            WorkManager.getInstance(this).enqueue(req);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void changePassTeam() {

        String pass = pass_team_new.getText().toString();

        // Los campos del login no pueden estar vacios
        if(teamname.isEmpty()) {
            Toast.makeText(this, getString(R.string.login_no_empty_field), Toast.LENGTH_SHORT).show();
        } else {
            // login al usuario en la aplicación
            try {
                // Preparar los datos para enviar al backend
                Data logindata = new Data.Builder()
                        .putString("funcion", "updatepass")
                        .putString("teamname", teamname)
                        .putString("teampass", pass)
                        .putString("cookie", cookie)
                        .build();

                // Tiene que existir conexión a internet
                Constraints restricciones = new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build();

                // Preparar la petición
                OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(TeamsWorker.class)
                        .setConstraints(restricciones)
                        .setInputData(logindata)
                        .build();

                // Lanzar la petición
                WorkManager.getInstance(this).getWorkInfoByIdLiveData(req.getId())
                        .observe(this, status -> {
                            if (status != null && status.getState().isFinished()) {
                                String id_user = status.getOutputData().getString("datos").trim();
                                if(id_user.isEmpty()) {
                                    Toast.makeText(this, getString(R.string.settings_pass_updated), Toast.LENGTH_SHORT).show();
                                    Log.d("debug_pass","updated");
                                } else {
                                    Log.d("debug_pass","not updated");
                                }
                            }
                        });

                WorkManager.getInstance(this).enqueue(req);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}