package com.anderpri.das_grupal.activities.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.anderpri.das_grupal.R;
import com.anderpri.das_grupal.activities.UnaActividad;

public class LoginFindTeam extends AppCompatActivity {

    EditText pinText, teamText, passText;
    TextView warning;
    String pin, team, pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_find_team);
        pinText = findViewById(R.id.text_pin_find);
        teamText = findViewById(R.id.text_pass_find);
        passText = findViewById(R.id.text_name_find);
        warning = findViewById(R.id.warning_find);

    }

    public void onFind(View v) {
        //pin = this.pinText.getText().toString();
        team = this.teamText.getText().toString();
        pass = this.passText.getText().toString();

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

    private boolean pinExists(String pin) {
        return pin.equals("5555");
    }

    private void enterTeam() {
        //TODO REGISTRAR USUARIO EN EQUIPO EN BBDD

        // Una vez registrado, cargamos el main
        Intent i = new Intent(this, UnaActividad.class);
        startActivity(i);
        finish();
    }

    public static class TeamExistsWebService extends Worker {

        public TeamExistsWebService(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }

        @NonNull
        @Override
        public ListenableWorker.Result doWork() {

            String result = "1";
            Data datos = null;
            if(result.equals("1")){
                datos = new Data.Builder()
                        .putBoolean("exists", true)
                        .build();
            }else{
                datos = new Data.Builder()
                        .putBoolean("exists", false)
                        .build();
            }
            return Result.success(datos);
        }
    }
}