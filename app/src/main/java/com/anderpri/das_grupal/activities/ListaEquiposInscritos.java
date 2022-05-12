package com.anderpri.das_grupal.activities;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.anderpri.das_grupal.R;
import com.anderpri.das_grupal.adapters.AdapterActividades;
import com.anderpri.das_grupal.controllers.webservices.ActivitiesWorker;
import com.anderpri.das_grupal.controllers.webservices.TeamsWorker;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class ListaEquiposInscritos extends AppCompatActivity {

    private String cookie;
    private String actividad;

    private AdapterActividades adapterActividades;
    private ArrayList<String> listaEquipos;
    private ListView list_equipos;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_equipos_inscritos);

        // Inicalizar la lista de actividades
        listaEquipos = new ArrayList<String>();
        list_equipos = (ListView) findViewById(R.id.lista_equipos_inscritos_recycler_view);

        cookie = getIntent().getExtras().getString("sesion");
        actividad = getIntent().getExtras().getString("actividad");

        solicitud();

    }

    private void solicitud() {

        try {
            // Preparar los datos para enviar al backend
            Data logindata = new Data.Builder()
                    .putString("funcion", "mostrarGruposActividad")
                    .putString("cookie", cookie)
                    .putString("actividad", actividad)
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
                            try {
                               // Si hay equipos inscritos
                                JSONArray miArray = new JSONArray(status.getOutputData().getString("datos"));
                                for (int i = 0; i < miArray.length(); i++) { //asi es, no se hacer un foreach en java
                                    JSONObject miJson = new JSONObject(miArray.get(i).toString());
                                    String name = miJson.getString("name");
                                    listaEquipos.add(name);
                                }
                                listarEquipos();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

            WorkManager.getInstance(this).enqueue(req);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listarEquipos() {
        adapterActividades = new AdapterActividades(this, getNames(listaEquipos));
        list_equipos.setAdapter(adapterActividades);
    }


    // Este método dado una lista de nombres, devolverá un array con los nombres
    private String[] getNames(ArrayList<String> lista) {
        Iterator<String> itr = lista.iterator();
        String nombre;
        String[] nombres = new String[lista.size()];
        int i = 0;
        while(itr.hasNext()) {
            nombre = itr.next();
            nombres[i] = nombre;
            i++;
        }
        return nombres;
    }
}