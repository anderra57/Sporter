package com.anderpri.das_grupal.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
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
import com.anderpri.das_grupal.controllers.webservices.ActivitiesAdminWorker;
import com.anderpri.das_grupal.controllers.webservices.ActivitiesWorker;
import com.anderpri.das_grupal.controllers.webservices.InscripcionesWorker;

public class VisualizarInfoActividad extends AppCompatActivity {

    private TextView titulo;
    private TextView descripcion;
    private TextView fecha;
    private TextView ciudad;
    private ImageView imagen;
    private Button btn;

    private String funcion;
    private String cookie;
    private SharedPreferences preferences;

    private String actividad;
    private String desc;
    private String data;
    private String city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_info);

        titulo = findViewById(R.id.titulo);
        descripcion = findViewById(R.id.descripcion);
        fecha = findViewById(R.id.fecha);
        ciudad = findViewById(R.id.ciudad);

        actividad = getIntent().getExtras().getString("titulo");
        desc = getIntent().getExtras().getString("descripcion");
        data = getIntent().getExtras().getString("fecha");
        city = getIntent().getExtras().getString("ciudad");

        titulo.setText(actividad);
        descripcion.setText(desc);
        fecha.setText("Fecha: " + data);
        ciudad.setText("Ciudad: " + city);

        // Depende de qué actividad venimos, se hará una cosa u otra
        funcion = getIntent().getExtras().getString("funcion");
        imagen = findViewById(R.id.imagen_actividad);
        btn = findViewById(R.id.btn_actividad);

        if(funcion.equals("lista_admin")) { // Borrar actividad de la base de datos
            btn.setText(getString(R.string.borrar));
        } else if(funcion.equals("lista_noinscritos")) { // Inscribir a un grupo en una actividad
            btn.setText(getString(R.string.inscribirse));
        } else { // Desapuntar a un grupo de una actividad
            btn.setText(getString(R.string.desapuntarse));
        }

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        cookie = preferences.getString("cookie","");

    }

    private void inscribirse() {
        try {
            // Preparar los datos para enviar al backend
            Data data = new Data.Builder()
                    .putString("funcion", "inscribirse")
                    .putString("cookie", cookie)
                    .putString("actividad", actividad)
                    .build();

            // Tiene que existir conexión a internet
            Constraints restricciones = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            // Preparar la petición
            OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(InscripcionesWorker.class)
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
                                Toast.makeText(this, getString(R.string.inscripcion), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(this, ListaActividadesInscrito.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } else { // La sesión es invalida, vuelta al login
                                Toast.makeText(this, getString(R.string.invalidSession), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(this, LoginMain.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });

            WorkManager.getInstance(this).enqueue(req);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void borrar() {
        try {
            // Preparar los datos para enviar al backend
            Data data = new Data.Builder()
                    .putString("funcion", "borrar")
                    .putString("cookie", cookie)
                    .putString("actividad", actividad)
                    .build();

            // Tiene que existir conexión a internet
            Constraints restricciones = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            // Preparar la petición
            OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(ActivitiesAdminWorker.class)
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
                                Toast.makeText(this, getString(R.string.borrado), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(this, ListaActividadesAdmin.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } else { // La sesión es invalida, vuelta al login
                                Toast.makeText(this, getString(R.string.invalidSession), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(this, LoginMain.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });

            WorkManager.getInstance(this).enqueue(req);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void desapuntarse() {
        try {
            // Preparar los datos para enviar al backend
            Data data = new Data.Builder()
                    .putString("funcion", "desapuntar")
                    .putString("cookie", cookie)
                    .putString("actividad", actividad)
                    .build();

            // Tiene que existir conexión a internet
            Constraints restricciones = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            // Preparar la petición
            OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(InscripcionesWorker.class)
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
                                Toast.makeText(this, getString(R.string.desapuntar), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(this, ListaActividadesInscrito.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } else { // La sesión es invalida, vuelta al login
                                Toast.makeText(this, getString(R.string.invalidSession), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(this, LoginMain.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        }
                    });

            WorkManager.getInstance(this).enqueue(req);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // En este método se verificarán las credenciales de los usuarios
    public void doAction(View v) {
        if(funcion.equals("lista_admin")) { // Borrar actividad de la base de datos
            borrar();
        } else if(funcion.equals("lista_noinscritos")) { // Inscribir a un grupo en una actividad
            inscribirse();
        } else { // Desapuntar a un grupo de una actividad
            desapuntarse();
        }
    }
}
