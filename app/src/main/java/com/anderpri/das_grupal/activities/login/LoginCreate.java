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
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.anderpri.das_grupal.R;
import com.anderpri.das_grupal.controllers.utils.Utils;
import com.anderpri.das_grupal.controllers.webservices.UsersWorker;

public class LoginCreate extends AppCompatActivity {

    EditText user, pass;
    TextView registro, warning;
    ImageView imagen;
    Button botonRegistro;
    static boolean existeUsuario;
    static String token;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String str = preferences.getString("lang","es");
        Utils.getInstance().setLocale(str,getBaseContext());
        boolean dark = preferences.getBoolean("dark",false);
        Utils.getInstance().setTheme(dark);


        //AuxiliarColores.elegirColor(this);
        setContentView(R.layout.activity_login_create);
        getItems();
        if(dark) imagen.setImageResource(R.drawable.sporter_text_alt);

        // TODO comprobar si tiene equipo o no
    }

    private void getItems(){
        user = findViewById(R.id.login_create_txt_user);
        pass = findViewById(R.id.login_create_txt_pass);
        imagen = findViewById(R.id.login_create_banner);
        botonRegistro = findViewById(R.id.login_create_btn);
        registro = findViewById(R.id.login_create_txt_title);
        warning = findViewById(R.id.login_create_txt_warning);

        registro.setText(R.string.login_register_here);
        warning.setVisibility(View.INVISIBLE);
        botonRegistro.setText(R.string.login_register);
    }

    // M??todo para gestionar click en login

    public void onRegister(View v) {
        String username = user.getText().toString();
        String password = pass.getText().toString();

        // Los campos del login no pueden estar vacios
        if(username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, getString(R.string.login_no_empty_field), Toast.LENGTH_SHORT).show();
            warning.setText(getString(R.string.login_no_empty_field));
            warning.setVisibility(View.VISIBLE);
            warning.setTextColor(Color.RED);
        } else {
            // login al usuario en la aplicaci??n
            try {
                // Preparar los datos para enviar al backend
                Data logindata = new Data.Builder()
                        .putString("funcion", "register")
                        .putString("username", username)
                        .putString("password", password)
                        .build();

                // Tiene que existir conexi??n a internet
                Constraints restricciones = new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build();

                // Preparar la petici??n
                OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(UsersWorker.class)
                        .setConstraints(restricciones)
                        .setInputData(logindata)
                        .build();

                // Lanzar la petici??n
                WorkManager.getInstance(this).getWorkInfoByIdLiveData(req.getId())
                        .observe(this, status -> {
                            if (status != null && status.getState().isFinished()) {
                                String id_user = status.getOutputData().getString("datos").trim();
                                if(id_user.isEmpty()) {
                                    // a??adir el token del dispositivo a la base de datos
                                    //addFirebasetoken(username);
                                    // Avanzar a la siguiente actividad (MainActivity)

                                    Toast.makeText(this, getString(R.string.login_registered_correctly), Toast.LENGTH_SHORT).show();
                                    finish();

                                } else {
                                    Toast.makeText(this, getString(R.string.login_user_in_use), Toast.LENGTH_SHORT).show();
                                    warning.setText(getString(R.string.login_user_in_use));
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
    public void onRegister(View v) {
        String username = this.user.getText().toString();
        String password = this.password.getText().toString();
        // Uso de Toast para evitar un nombre repetido, un nombre vac??o, o un nombre gen??rico.
        if(username.isEmpty() || password.isEmpty()){
            Toast.makeText(this, getString(R.string.login_no_empty_field), Toast.LENGTH_SHORT).show();
            warning.setText(getString(R.string.login_no_empty_field));
            warning.setVisibility(View.VISIBLE);
            warning.setTextColor(Color.RED);
        }
        else if(password.length() < 6) {
            Toast.makeText(this, getString(R.string.login_pass_requirements), Toast.LENGTH_SHORT).show();
            warning.setText(getString(R.string.login_no_empty_field));
            warning.setVisibility(View.VISIBLE);
            warning.setTextColor(Color.RED);
        }
        else{
            jugadorExisteRegistro(username, password);
        }
    }

    */


    // comprobar si existe el usuario
    private void jugadorExisteRegistro(String username, String password) {
        Data datos = new Data.Builder().putString("usuario", username).build();
        OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(CheckUserExistsWebService.class).setInputData(datos).build();
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(req.getId()).observe(this, status -> {
            if(status != null && status.getState().isFinished()) {
                existeUsuario = status.getOutputData().getBoolean("existe", false);
                existeUsuario = testExiste(username);
                if(existeUsuario){
                    Toast.makeText(this, getString(R.string.login_user_in_use), Toast.LENGTH_SHORT).show();
                    warning.setText(getString(R.string.login_user_in_use));
                    warning.setVisibility(View.VISIBLE);
                    warning.setTextColor(Color.RED);
                }
                else{
                    registrar(username, password);
                }
            }
        });
        WorkManager.getInstance(this).enqueue(req);
    }

    private boolean testExiste(String username) {
        return username.equals("a");
    }

    // registrar usuario
    private void registrar(String username, String password) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toast.makeText(this, getString(R.string.no_internet), Toast.LENGTH_SHORT).show();
        }
        //FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(instanceIdResult -> token = instanceIdResult.getToken());
        Data datos = new Data.Builder().putString("usuario", username).putString("password", password).putString("token", token).build();
        Constraints restricciones = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(AddUserWebService.class).setInputData(datos).setConstraints(restricciones).build();
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(req.getId()).observe(this, status -> {
            if(status != null && status.getState().isFinished()) {
                boolean okey = status.getOutputData().getBoolean("OK", false);
                if(okey){
                    /*Intent intentMenuOnline = new Intent(this, OnlineMenuActivity.class);
                    intentMenuOnline.putExtra("user", username);
                    setResult(RESULT_OK, intentMenuOnline);
                    startActivity(intentMenuOnline);*/
                    Toast.makeText(this, getString(R.string.login_registered_correctly), Toast.LENGTH_LONG).show();
                    Intent i = new Intent(this, LoginTeamButtons.class);
                    startActivity(i);
                    finish();
                }

            }
        });
        WorkManager.getInstance(this).enqueue(req);
    }

    public static class CheckUserExistsWebService extends Worker {

        public CheckUserExistsWebService(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }

        @NonNull
        @Override
        public ListenableWorker.Result doWork() {
            Data datos = new Data.Builder().putBoolean("existe", true).build();
            return Result.success(datos);
        }
    }

    public static class AddUserWebService extends Worker {

        public AddUserWebService(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }

        @NonNull
        @Override
        public ListenableWorker.Result doWork() {
            Data datos = new Data.Builder().putBoolean("OK", true).build();
            return Result.success(datos);
        }
    }

}