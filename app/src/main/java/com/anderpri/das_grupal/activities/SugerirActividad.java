package com.anderpri.das_grupal.activities;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.anderpri.das_grupal.R;
import com.anderpri.das_grupal.activities.login.LoginMain;
import com.anderpri.das_grupal.controllers.webservices.SugerenciasWorker;
import com.anderpri.das_grupal.controllers.webservices.UsersWorker;
import com.anderpri.das_grupal.fragments.DatePickerFragment;
import com.google.android.material.navigation.NavigationView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SugerirActividad extends AppCompatActivity {

    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;

    Button botonSugerir;
    EditText nombre, ciudad, fecha, explicacion, numeroParticipantes;

    private String cookie;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sugerir_actividad);

        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // This will display an Up icon (<-), we will replace it with hamburger later
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.sugerir_actividad_drawer_layout);
        nvDrawer = (NavigationView) findViewById(R.id.navigation_view);
        // Setup drawer view
        setupDrawerContent(nvDrawer);

        botonSugerir = findViewById(R.id.sugerir_actividad_boton);
        nombre = findViewById(R.id.sugerir_actividad_nombre_text);
        ciudad = findViewById(R.id.sugerir_actividad_ciudad_text);
        fecha = findViewById(R.id.sugerir_actividad_fecha_text);
        explicacion = findViewById(R.id.sugerir_actividad_explicacion_text);
        numeroParticipantes = findViewById(R.id.sugerir_actividad_numero_text);

        getCookie();

        fecha.setOnClickListener(view -> {
            switch (view.getId()){
                case R.id.sugerir_actividad_fecha_text:
                    showDatePickerDialog();
                    break;
            }
        });

        botonSugerir.setOnClickListener(view -> {
            if ("".equals(nombre.getText().toString()) || "".equals(ciudad.getText().toString()) || "".equals(fecha.getText().toString()) || "".equals(explicacion.getText().toString()) || "".equals(numeroParticipantes.getText().toString())) {
                Toast.makeText(this, R.string.noCampoVacio, Toast.LENGTH_SHORT).show();
            }else if (!fechaCorrecta(fecha.getText().toString())) {// la fecha debe ser posterior a la fecha actual
                Toast.makeText(this, R.string.fechaActividadErronea, Toast.LENGTH_SHORT).show();
            }else{ // Todos los parámetros guay, llamamos al worker
                solicitudSugerir(nombre.getText().toString(), ciudad.getText().toString(), numeroParticipantes.getText().toString(), fecha.getText().toString(), explicacion.getText().toString());
            }
        });

    }

    private void solicitudSugerir(String nombre, String ciudad, String numero, String fecha, String explicacion) {
        Data solicitud = new Data.Builder()
                .putString("funcion", "sugerir")
                .putString("cookie", cookie)
                .putString("actividad", nombre)
                .putString("descripcion", explicacion)
                .putString("city", ciudad)
                .putString("fecha", fecha)
                .build();
        Constraints restricciones = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(SugerenciasWorker.class).setConstraints(restricciones).setInputData(solicitud).build();
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(req.getId()).observe(this, status -> {
            if (status != null && status.getState().isFinished()) {
                Toast.makeText(this, R.string.sugerirExito, Toast.LENGTH_SHORT).show();
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });
        WorkManager.getInstance(this).enqueue(req);
    }

    private boolean fechaCorrecta(String fechaUsuario) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        final String selectedDate = year + "-" + (month+1) + "-" + day;
        Date parametro = null;
        try {
            parametro = new SimpleDateFormat("yyyy-MM-dd").parse(fechaUsuario);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date actual = null;
        try {
            actual = new SimpleDateFormat("yyyy-MM-dd").parse(selectedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return parametro.after(actual);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        Intent intent;
        switch(menuItem.getItemId()) {
            case R.id.nav_first_fragment:
                intent = new Intent(this, ListaActividadesInscrito.class);
                finish();
                startActivity(intent);
                break;
            case R.id.nav_second_fragment:
                intent = new Intent(this, ListaActividadesNoInscrito.class);
                finish();
                startActivity(intent);
                break;
            case R.id.nav_third_fragment:
                mDrawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.settings:
                break;
            case R.id.logout:
                logout();
                break;
            default:
                break;
        }


    }

    private void showDatePickerDialog() {
        DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                // +1 because January is zero
                final String selectedDate = year + "-" + (month+1) + "-" + day;
                fecha.setText(selectedDate);
            }
        });

        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    private void getCookie() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        cookie = preferences.getString("cookie","");
    }
    private void logout() {
        // borrar de SP
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        String cookie = preferences.getString("cookie","");
        editor.remove("cookie");
        editor.apply();

        // llamada al servidor
        try {
            Data logout = new Data.Builder()
                    .putString("funcion", "logout")
                    .putString("cookie", cookie)
                    .build();
            Constraints restricciones = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
            OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(UsersWorker.class).setConstraints(restricciones).setInputData(logout).build();
            WorkManager.getInstance(this).getWorkInfoByIdLiveData(req.getId()).observe(this, status -> {
                if (status != null && status.getState().isFinished()) {
                    Intent intent = new Intent(this, LoginMain.class);
                    startActivity(intent);
                    finish();
                }
            });
            WorkManager.getInstance(this).enqueue(req);
        } catch (Exception e) {  e.printStackTrace();  }

    }
}