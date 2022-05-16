package com.anderpri.das_grupal.activities.login;

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
import android.widget.TextView;
import android.widget.Toast;

import com.anderpri.das_grupal.R;
import com.anderpri.das_grupal.activities.ListaActividadesInscrito;
import com.anderpri.das_grupal.controllers.webservices.TeamsWorker;

public class LoginFindTeam extends AppCompatActivity {

    EditText teamText, passText;
    TextView warning;
    String pin, team, pass;
    String cookie;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_find_team);
        teamText = findViewById(R.id.login_find_team_txt_name);
        passText = findViewById(R.id.login_find_team_txt_pass);
        warning = findViewById(R.id.login_find_team_txt_warning);

        getCookie();
        Log.d("cookie_join",cookie);
    }

    private void getCookie() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        cookie = preferences.getString("cookie","");
    }

    public void onFind(View v) {
        String name = teamText.getText().toString();
        String password = passText.getText().toString();

        // Los campos del login no pueden estar vacios
        if(name.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.login_no_empty_field), Toast.LENGTH_SHORT).show();
            warning.setText(getString(R.string.login_no_empty_field));
            warning.setVisibility(View.VISIBLE);
            warning.setTextColor(Color.RED);
        } else {
            // login al usuario en la aplicación
            try {
                // Preparar los datos para enviar al backend
                Data logindata = new Data.Builder()
                        .putString("funcion", "login")
                        .putString("teamname", name)
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
                                    // añadir el token del dispositivo a la base de datos
                                    //addFirebasetoken(username);
                                    // Avanzar a la siguiente actividad (MainActivity)

                                    Intent intent = new Intent(this, ListaActividadesInscrito.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(this, getString(R.string.login_wrong_team_pass), Toast.LENGTH_SHORT).show();
                                    warning.setText(getString(R.string.login_wrong_team_pass));
                                    warning.setVisibility(View.VISIBLE);
                                    warning.setTextColor(Color.RED);
                                }
                            }
                        });

                WorkManager.getInstance(this).enqueue(req);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
/*
    public void onFind1(View v) {
        //pin = this.pinText.getText().toString();
        team = this.teamText.getText().toString();
        pass = this.passText.getText().toString();

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
        }
        Data data = new Data.Builder().putString("pin", pin).build();
        Constraints restricciones = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(TeamExistsWebService.class).setInputData(data).setConstraints(restricciones).build();
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(req.getId()).observe(this, status -> {
            if(status != null && status.getState().isFinished()) {
                boolean teamExists = teamExists(team,pass);

                if(pass.length() != 4 || team.isEmpty()){
                    // TODO cambiar texto
                    Toast.makeText(this, "Invalid PIN", Toast.LENGTH_SHORT).show();
                    warning.setText("Invalid creds");
                    warning.setVisibility(View.VISIBLE);
                    warning.setTextColor(Color.RED);
                } else if (!teamExists){
                    // TODO cambiar texto
                    Toast.makeText(this, "No existe el equipo", Toast.LENGTH_SHORT).show();
                    warning.setText("No existe el equipo");
                    warning.setVisibility(View.VISIBLE);
                    warning.setTextColor(Color.RED);
                } else{
                    // entra en el equipo y en el main
                    enterTeam();
                }
            }
        });
        WorkManager.getInstance(this).enqueue(req);

    }

    private boolean teamExists(String team, String pass) {
        return team.equals("a") && pass.equals("5555");
    }




    /*
    public void onFind(View v) {
        pin = this.pinText.getText().toString();

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toast.makeText(this, getString(R.string.noInternet), Toast.LENGTH_SHORT).show();
        }
        Data data = new Data.Builder().putString("pin", pin).build();
        Constraints restricciones = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(TeamExistsWebService.class).setInputData(data).setConstraints(restricciones).build();
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(req.getId()).observe(this, status -> {
            if(status != null && status.getState().isFinished()) {
                boolean pinExists = pinExists(pin);

                if(pin.length() != 4){
                    // TODO cambiar texto
                    Toast.makeText(this, "Invalid PIN", Toast.LENGTH_SHORT).show();
                    warning.setText("Invalid PIN");
                    warning.setVisibility(View.VISIBLE);
                    warning.setTextColor(Color.RED);
                } else if (!pinExists){
                    // TODO cambiar texto
                    Toast.makeText(this, "No existe el equipo", Toast.LENGTH_SHORT).show();
                    warning.setText("No existe el equipo");
                    warning.setVisibility(View.VISIBLE);
                    warning.setTextColor(Color.RED);
                } else{
                    // entra en el equipo y en el main
                    enterTeam();
                }
            }
        });
        WorkManager.getInstance(this).enqueue(req);

    }
    */
/*
    private boolean pinExists(String pin) {
        return pin.equals("5555");
    }
*/



}