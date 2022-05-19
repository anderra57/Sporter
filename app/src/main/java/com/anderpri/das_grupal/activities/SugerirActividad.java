package com.anderpri.das_grupal.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.DialogFragment;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.anderpri.das_grupal.R;
import com.anderpri.das_grupal.activities.login.LoginMain;
import com.anderpri.das_grupal.activities.settings.SettingsActivity;
import com.anderpri.das_grupal.controllers.utils.Utils;
import com.anderpri.das_grupal.controllers.webservices.SugerenciasWorker;
import com.anderpri.das_grupal.controllers.webservices.UsersWorker;
import com.anderpri.das_grupal.dialogs.ImagenDialog;
import com.anderpri.das_grupal.fragments.DatePickerFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SugerirActividad extends AppCompatActivity implements ImagenDialog.ListenerDialog{

    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;

    private File imgFichero;
    private Uri imagenUri;
    private static int CODIGO_GALERIA = 1;
    private static int CODIGO_FOTO_ARCHIVO = 2;
    private static int conseguirCoordenadas = 3;

    Button botonSugerir, botonImagen;
    EditText nombre, ciudad, fecha, explicacion, numeroParticipantes;
    TextView avisoImagen;
    private TextView nombreTextView, equipoTextView;
    private String nombreString, equipoString;
    private String cookie, ubicacion, latitude, longitude, imageName;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String str = preferences.getString("lang","es");
        Utils.getInstance().setLocale(str,getBaseContext());
        boolean dark = preferences.getBoolean("dark",false);
        Utils.getInstance().setTheme(dark);

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
        botonImagen = findViewById(R.id.sugerir_actividad_boton_imagen);
        nombre = findViewById(R.id.sugerir_actividad_nombre_text);
        ciudad = findViewById(R.id.sugerir_actividad_ciudad_text);
        fecha = findViewById(R.id.sugerir_actividad_fecha_text);
        explicacion = findViewById(R.id.sugerir_actividad_explicacion_text);
        numeroParticipantes = findViewById(R.id.sugerir_actividad_numero_text);
        avisoImagen = findViewById(R.id.sugerir_actividad_aviso_imagen);

        getCookie();
        setUsernameTeamname();
        fecha.setOnClickListener(view -> {
            switch (view.getId()){
                case R.id.sugerir_actividad_fecha_text:
                    showDatePickerDialog();
                    break;
            }
        });

        ciudad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentCoordenadas = new Intent(getApplicationContext(), Maps.class);
                if(ubicacion != null){
                    intentCoordenadas.putExtra("ubicacion", ubicacion);
                    intentCoordenadas.putExtra("latitude", latitude);
                    intentCoordenadas.putExtra("longitude", longitude);
                }
                startActivityForResult(intentCoordenadas, conseguirCoordenadas);
            }
        });

        botonSugerir.setOnClickListener(view -> {
            if ("".equals(nombre.getText().toString()) || "".equals(fecha.getText().toString()) || "".equals(explicacion.getText().toString()) || "".equals(numeroParticipantes.getText().toString())) {
                Toast.makeText(this, R.string.noCampoVacio, Toast.LENGTH_SHORT).show();
            }else if (!fechaCorrecta(fecha.getText().toString())) {// la fecha debe ser posterior a la fecha actual
                Toast.makeText(this, R.string.fechaActividadErronea, Toast.LENGTH_SHORT).show();
            }else{ // Todos los parámetros guay, llamamos al worker
                subirAFirebase();
                solicitudSugerir(nombre.getText().toString(), ciudad.getText().toString(), numeroParticipantes.getText().toString(), fecha.getText().toString(), explicacion.getText().toString(), latitude, longitude);
            }
        });

        botonImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment imagenDialog = new ImagenDialog();
                imagenDialog.show(getSupportFragmentManager(), "seleccionarImagen");
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == conseguirCoordenadas) {
            if(resultCode == Activity.RESULT_OK){
                latitude = data.getStringExtra("latitude");
                longitude = data.getStringExtra("longitude");
                ubicacion = data.getStringExtra("ubicacion");
                ciudad.setText(ubicacion);
            }
        } // En caso de coger la imagen desde la galería, ponerla en el imageview
        else if(requestCode == CODIGO_GALERIA && resultCode == RESULT_OK) {
            imagenUri = data.getData();
            imageName = new File(imagenUri.getPath()).getName();
            avisoImagen.setVisibility(View.VISIBLE);
        }else if(requestCode == CODIGO_FOTO_ARCHIVO && resultCode == RESULT_OK) {
            imageName = imgFichero.getName();
            avisoImagen.setVisibility(View.VISIBLE);
        }
    }

    private void solicitudSugerir(String nombre, String ciudad, String numero, String fecha, String explicacion, String latitud, String longitud) {

        Data solicitud = new Data.Builder()
                .putString("funcion", "sugerir")
                .putString("cookie", cookie)
                .putString("actividad", nombre)
                .putString("descripcion", explicacion)
                .putString("city", ciudad)
                .putString("imageName", imageName)
                .putString("fecha", fecha)
                .putString("latitude", latitud)
                .putString("longitude", longitud)
                .build();
        Constraints restricciones = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(SugerenciasWorker.class).setConstraints(restricciones).setInputData(solicitud).build();
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(req.getId()).observe(this, status -> {
            if (status != null && status.getState().isFinished()) {
                String result = status.getOutputData().getString("datos").trim();
                System.out.println(result);
                if (result.isEmpty()) { // Actividad creada correctamente
                    Toast.makeText(this, R.string.crearExito, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, ListaActividadesInscrito.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else if (result.equals("InvalidSession")) {
                    Toast.makeText(this, getString(R.string.invalidSession), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, LoginMain.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, getString(R.string.actividadExiste), Toast.LENGTH_SHORT).show();
                    borrarNombreActividad();
                }
            }
        });
        WorkManager.getInstance(this).enqueue(req);
    }

    public void borrarNombreActividad() {
        nombre.setText("");
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
            case R.id.nav_fourth_fragment:
                intent = new Intent(this, ActividadesMapa.class);
                finish();
                startActivity(intent);
                break;
            case R.id.settings:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
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
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
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

    @Override
    public void pulsarCamara(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            // Si el permiso no se ha pedido pedirlo
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)){
                // Aceptar el permiso
            }else{
                // El usuario ha decidido que no quiere aceptar el permiso
                Toast.makeText(this, getString(R.string.noPermisoCamara), Toast.LENGTH_SHORT).show();
            }
            // Pedir permiso al usuario
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
        }else { // El permiso ya estaba concedido
            // Definir donde almacenar las imágenes
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fichero = "imagen-"+timeStamp;
            File directorio = this.getFilesDir();
            // Crear archivo
            try {
                imgFichero = File.createTempFile(fichero, ".jpg", directorio);
                imagenUri = FileProvider.getUriForFile(this,  getApplicationContext().getPackageName() + ".provider", imgFichero);
            } catch (Exception e) {
                System.out.println("Error al crear el fichero");
            }
            // Abrir la cámara
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imagenUri);
            startActivityForResult(intent, CODIGO_FOTO_ARCHIVO);
        }
    }

    @Override
    public void pulsarGaleria(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, CODIGO_GALERIA);
    }

    private void subirAFirebase(){
        if(imagenUri != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference();
            StorageReference spaceReference = storageReference.child(imageName);
            spaceReference.putFile(imagenUri);
        }
    }

    private void setUsernameTeamname() {
        View headerView = nvDrawer.getHeaderView(0);
        nombreTextView = (TextView) headerView.findViewById(R.id.usuario);
        equipoTextView = (TextView) headerView.findViewById(R.id.equipo);

        nombreString = preferences.getString("username", "");
        nombreTextView.setText("@"+nombreString);
        equipoString = preferences.getString("teamname", "");
        equipoTextView.setText("#"+equipoString);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(ubicacion != null && longitude != null && latitude != null){
            outState.putString("ubicacion", ubicacion);
            outState.putString("longitude", longitude);
            outState.putString("latitude", latitude);
        }if(imageName != null && imagenUri != null){
            outState.putString("imageName", imageName);
            outState.putParcelable("imageUri", imagenUri);
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ubicacion = savedInstanceState.getString("ubicacion");
        longitude = savedInstanceState.getString("longitude");
        latitude = savedInstanceState.getString("latitude");
        imageName = savedInstanceState.getString("imageName");
        imagenUri = savedInstanceState.getParcelable("imageUri");
        if(imageName != null && imagenUri != null) {
            avisoImagen.setVisibility(View.VISIBLE);
        }
    }
}