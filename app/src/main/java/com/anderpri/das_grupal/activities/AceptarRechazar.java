package com.anderpri.das_grupal.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.anderpri.das_grupal.R;
import com.anderpri.das_grupal.activities.login.LoginMain;
import com.anderpri.das_grupal.controllers.utils.Utils;
import com.anderpri.das_grupal.controllers.webservices.ActivitiesAdminWorker;
import com.anderpri.das_grupal.controllers.webservices.InscripcionesWorker;
import com.anderpri.das_grupal.controllers.webservices.SolicitudesWorker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class AceptarRechazar extends AppCompatActivity {

    private TextView titulo;
    private TextView descripcion;
    private TextView fecha;
    private TextView ciudad;
    private ImageView imagen;

    private String cookie;
    private SharedPreferences preferences;

    private String actividad;
    private String desc;
    private String data;
    private String city;
    String imgName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String str = preferences.getString("lang","no_lang");
        Utils.getInstance().setLocale(str,getBaseContext());

        setContentView(R.layout.aceptar_rechazar);

        titulo = findViewById(R.id.titulo_1);
        descripcion = findViewById(R.id.descripcion_1);
        fecha = findViewById(R.id.fecha_1);
        ciudad = findViewById(R.id.ciudad_1);
        imagen = findViewById(R.id.imagen_actividad_1);

        actividad = getIntent().getExtras().getString("titulo");
        desc = getIntent().getExtras().getString("descripcion");
        data = getIntent().getExtras().getString("fecha");
        city = getIntent().getExtras().getString("ciudad");
        imgName = getIntent().getExtras().getString("imagen");

        titulo.setText(actividad);
        descripcion.setText(desc);
        fecha.setText("Fecha: " + data);
        ciudad.setText("Ciudad: " + city);

        // Cargar la imagen
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        StorageReference path = storageReference.child(imgName);
        path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri.toString()).into(imagen);
            }
        });

        path.getDownloadUrl().addOnSuccessListener(uri -> Picasso.get().load(uri.toString()).into(imagen)).addOnFailureListener(e -> imagen.setImageResource(R.drawable.default_activity));


        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        cookie = preferences.getString("cookie","");

    }

    private void aceptar_solicitud() {
        try {
            // Preparar los datos para enviar al backend
            Data data = new Data.Builder()
                    .putString("funcion", "aceptar")
                    .putString("cookie", cookie)
                    .putString("actividad", actividad)
                    .build();

            // Tiene que existir conexión a internet
            Constraints restricciones = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            // Preparar la petición
            OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(SolicitudesWorker.class)
                    .setConstraints(restricciones)
                    .setInputData(data)
                    .build();

            // Lanzar la petición
            WorkManager.getInstance(this).getWorkInfoByIdLiveData(req.getId())
                    .observe(this, status -> {
                        if (status != null && status.getState().isFinished()) {
                            String result = status.getOutputData().getString("datos").trim();
                            if(result.isEmpty()) { // La petición se ha realizado correctamente
                                // Usamos las flags porque queremos que la lista de actividades se actualice
                                Toast.makeText(this, getString(R.string.aceptada), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(this, ListaActividadesAdmin.class);
                                Log.d("prueba", "finish");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else { // La sesión es invalida, vuelta al login
                                Toast.makeText(this, getString(R.string.invalidSession), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(this, LoginMain.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                            finish();
                        }
                    });

            WorkManager.getInstance(this).enqueue(req);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void rechazar_solicitud() {
        try {
            // Preparar los datos para enviar al backend
            Data data = new Data.Builder()
                    .putString("funcion", "rechazar")
                    .putString("cookie", cookie)
                    .putString("actividad", actividad)
                    .build();

            // Tiene que existir conexión a internet
            Constraints restricciones = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            // Preparar la petición
            OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(SolicitudesWorker.class)
                    .setConstraints(restricciones)
                    .setInputData(data)
                    .build();

            // Lanzar la petición
            WorkManager.getInstance(this).getWorkInfoByIdLiveData(req.getId())
                    .observe(this, status -> {
                        if (status != null && status.getState().isFinished()) {
                            String result = status.getOutputData().getString("datos").trim();
                            if(result.isEmpty()) { // La petición se ha realizado correctamente
                                // Usamos las flags porque queremos que la lista de actividades se actualice
                                Toast.makeText(this, getString(R.string.rechazada), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(this, ListaActividadesAdmin.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else { // La sesión es invalida, vuelta al login
                                Toast.makeText(this, getString(R.string.invalidSession), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(this, LoginMain.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                            finish();
                        }
                    });

            WorkManager.getInstance(this).enqueue(req);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Aceptar la sugerencia del grupo
    public void aceptar(View v) {
        aceptar_solicitud();
    }

    // Rechazar la sugerencia del grupo
    public void rechazar(View v) {
        rechazar_solicitud();
    }
}
