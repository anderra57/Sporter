package com.anderpri.das_grupal.activities.login;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
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

import com.anderpri.das_grupal.R;
import com.anderpri.das_grupal.activities.UnaActividad;
import com.anderpri.das_grupal.controllers.LoginController;


public class LoginMain extends AppCompatActivity {

    EditText user, password;
    TextView registro, warning;
    ImageView imagen;
    Button botonLogin;
    static boolean userExists;
    static String token;
    String username;

    LoginController loginController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //AuxiliarColores.elegirColor(this);
        setContentView(R.layout.activity_login_main);
        //defineReceiver();

        user = findViewById(R.id.textUser);
        password = findViewById(R.id.textPassword);
        imagen = findViewById(R.id.imagenLogin);
        botonLogin = findViewById(R.id.botonLogin);
        registro = findViewById(R.id.textRegistro);
        warning = findViewById(R.id.textWarning);

        registro.setText(R.string.registrar);
        warning.setVisibility(View.INVISIBLE);
        botonLogin.setText(R.string.iniciarSesion);
        //FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(instanceIdResult -> token = instanceIdResult.getToken());

        loginController = LoginController.getInstance();
    }

    /// GESTIONAR LOGIN ///

    public void onLogin(View v) {
        System.out.println("login");
        username = this.user.getText().toString();
        String password = this.password.getText().toString();
        // Uso de Toast para evitar un nombre repetido, un nombre vacío, o un nombre genérico.
        if(username.isEmpty() || password.isEmpty()){
            Toast.makeText(this, getString(R.string.ningunCampoVacio), Toast.LENGTH_SHORT).show();
            warning.setText(getString(R.string.ningunCampoVacio));
            warning.setVisibility(View.VISIBLE);
            warning.setTextColor(Color.RED);
        }
        // El jugador no exíste
        else{
            userExists(username, password);
        }

    }

    private void userExists(String username, String password) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toast.makeText(this, getString(R.string.noInternet), Toast.LENGTH_SHORT).show();
        }
        Data datos = new Data.Builder().putString("usuario", username).putString("password", password).putString("token", token).build();
        Constraints restricciones = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(UserExistsWebService.class).setInputData(datos).setConstraints(restricciones).build();
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(req.getId()).observe(this, status -> {
            if(status != null && status.getState().isFinished()) {
                userExists = status.getOutputData().getBoolean("existe", false);
                if (username.equals("a") && password.equals("a")) userExists = true; // temporal
                if(!userExists){
                    Toast.makeText(this, getString(R.string.jugadorNoExisteInvalidPassword), Toast.LENGTH_SHORT).show();
                    warning.setText(getString(R.string.jugadorNoExisteInvalidPassword));
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
    }

    public static class UserExistsWebService extends Worker {

        public UserExistsWebService(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }

        @NonNull
        @Override
        public ListenableWorker.Result doWork() {
            Data datos = new Data.Builder().putBoolean("existe", false).build();
            return Result.success(datos);
        }
    }

    /// GESTIONAR CLICK EN REGISTRARSE ///

    public void onRegister(View v){
        System.out.println("register");
        Intent intent = new Intent(this, LoginCreate.class);
        startActivity(intent);
        finish();
    }

}