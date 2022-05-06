package com.anderpri.das_grupal.activities.login;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import androidx.work.impl.model.Preference;

import com.anderpri.das_grupal.R;
import com.anderpri.das_grupal.activities.UnaActividad;
import com.anderpri.das_grupal.controllers.LoginController;
import com.anderpri.das_grupal.controllers.webservices.UsersWorker;
/*import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;*/

import java.sql.SQLOutput;


public class LoginMain extends AppCompatActivity {

    EditText user, pass;
    TextView registro, warning;
    ImageView imagen;
    Button botonLogin;
    static boolean userExists;
    static String token;
    //String username, password;
    private SharedPreferences preferences;

    LoginController loginController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //AuxiliarColores.elegirColor(this);
        setContentView(R.layout.activity_login_main);
        //defineReceiver();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        user = findViewById(R.id.login_main_txt_user);
        pass = findViewById(R.id.login_main_txt_pass);
        imagen = findViewById(R.id.login_main_banner);
        botonLogin = findViewById(R.id.login_main_btn);
        registro = findViewById(R.id.login_main_txt_title);
        warning = findViewById(R.id.login_main_txt_warning);

        registro.setText(R.string.login_register_here);
        warning.setVisibility(View.INVISIBLE);
        botonLogin.setText(R.string.login_log_in);
        //FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(instanceIdResult -> token = instanceIdResult.getToken());

        loginController = LoginController.getInstance();
    }

    /// GESTIONAR LOGIN ///

    public void onLogin(View v) {
        String username = user.getText().toString();
        String password = pass.getText().toString();
        //getFirebaseToken();
        token = "test"; // provisional

        // Los campos del login no pueden estar vacios
        if(username.isEmpty() || password.isEmpty()) {
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
                                String cookie = status.getOutputData().getString("cookie").trim();
                                if(!id_user.isEmpty()) {
                                    // añadir el token del dispositivo a la base de datos
                                    //addFirebasetoken(username);
                                    // Avanzar a la siguiente actividad (MainActivity)

                                    saveCookie(cookie);
                                    manageIdUser(id_user);
                                    /*
                                    Intent intent = new Intent(this, MainActivity.class);
                                    intent.putExtra("username", username);
                                    startActivity(intent);
                                    finish();*/
                                } else {
                                    Toast.makeText(this, getString(R.string.login_wrong_user_pass), Toast.LENGTH_SHORT).show();
                                    warning.setText(getString(R.string.login_wrong_user_pass));
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

    private void saveCookie(String cookie) {
        Log.d("cookie_loginmain",cookie);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("cookie",cookie);
        editor.commit();
    }

    private void manageIdUser(String id_user) {
        if (id_user.equals("administrator")){
            Intent intent = new Intent(this, UnaActividad.class);
            startActivity(intent);
            finish();
        } else if (id_user.equals("nogroup")){ // user normal SIN grupo
            Intent intent = new Intent(this, LoginTeamButtons.class);
            startActivity(intent);
            finish();
        } else { // user normal con grupo
            Intent intent = new Intent(this, UnaActividad.class);
            startActivity(intent);
            finish();
        }
    }

    /*// Conseguimos el token de firebase
    // El script del backend está configurado tal que si el token ya existiera, no se haría nada,
    // en caso de no existir, se inserta en la base de datos
    private void getFirebaseToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.d("firebase_error", "Firebase error: " + task.getException().toString());
                            return;
                        }
                        String token = task.getResult().getToken();
                        Log.d("token_firebase", "Token: " + token);
                        setToken(token);
                    }
                });
    }

    private void setToken(String tok) {
        token = tok;
    }*/

    /*
    public void onLogin(View v) {
        username = this.user.getText().toString();
        String password = this.password.getText().toString();
        // Uso de Toast para evitar un nombre repetido, un nombre vacío, o un nombre genérico.
        if(username.isEmpty() || password.isEmpty()){
            Toast.makeText(this, getString(R.string.login_no_empty_field), Toast.LENGTH_SHORT).show();
            warning.setText(getString(R.string.login_no_empty_field));
            warning.setVisibility(View.VISIBLE);
            warning.setTextColor(Color.RED);
        }
        // El jugador no exíste
        else{
            userExists(username, password);
        }

    }*/

    /*
    private void userExists(String username, String password) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
        }

        Data datos = new Data.Builder().putString("usuario", username).putString("password", password).putString("token", token).build();
        Constraints restricciones = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(UserExistsWebService.class).setInputData(datos).setConstraints(restricciones).build();
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(req.getId()).observe(this, status -> {
            if(status != null && status.getState().isFinished()) {
                userExists = status.getOutputData().getBoolean("existe", false);
                if (username.equals("a") && password.equals("a")) userExists = true; // temporal
                if(!userExists){
                    Toast.makeText(this, getString(R.string.login_wrong_user_pass), Toast.LENGTH_SHORT).show();
                    warning.setText(getString(R.string.login_wrong_user_pass));
                    warning.setVisibility(View.VISIBLE);
                    warning.setTextColor(Color.RED);
                }
                else{
                    SharedPreferences preferences = getSharedPreferences("credenciales", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString("username", username);
                    editor.commit();
                    Intent i = new Intent(this, UnaActividad.class);
                    i.putExtra("user", username);
                    startActivity(i);
                    finish();
                }
            }
        });
        WorkManager.getInstance(this).enqueue(req);
    }*/

    /// GESTIONAR CLICK EN REGISTRARSE ///


    public void onRegister(View v){
        Intent intent = new Intent(this, LoginCreate.class);
        startActivity(intent);
        //finish();
    }

}