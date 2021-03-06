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
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import com.anderpri.das_grupal.R;
import com.anderpri.das_grupal.adapters.AdapterActividades;
import com.anderpri.das_grupal.controllers.utils.Utils;
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
    private ArrayList<Equipo> listaEquipos;
    private ListView list_equipos;
    private SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String str = preferences.getString("lang","es");
        Utils.getInstance().setLocale(str,getBaseContext());
        boolean dark = preferences.getBoolean("dark",false);
        Utils.getInstance().setTheme(dark);

        setContentView(R.layout.activity_lista_equipos_inscritos);

        // Inicalizar la lista de actividades
        listaEquipos = new ArrayList<Equipo>();
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

            // Tiene que existir conexi??n a internet
            Constraints restricciones = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            // Preparar la petici??n
            OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(TeamsWorker.class)
                    .setConstraints(restricciones)
                    .setInputData(logindata)
                    .build();

            // Lanzar la petici??n
            WorkManager.getInstance(this).getWorkInfoByIdLiveData(req.getId())
                    .observe(this, status -> {
                        if (status != null && status.getState().isFinished()) {
                            try {
                                if(status.getOutputData().getString("datos").length() != 1){ // Si hay equipos inscritos
                                    JSONArray miArray = new JSONArray(status.getOutputData().getString("datos"));
                                    for (int i = 0; i < miArray.length(); i++) { //asi es, no se hacer un foreach en java
                                        JSONObject miJson = new JSONObject(miArray.get(i).toString());
                                        Equipo equipo = new Equipo(miJson.getString("name"), miJson.getString("imageName"));
                                        listaEquipos.add(equipo);
                                    }
                                    listarEquipos();
                                }else{ // No hay equipos inscritos
                                    Toast.makeText(this, getString(R.string.noEquipos), Toast.LENGTH_SHORT).show();
                                }
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
            adapterActividades = new AdapterActividades(this, getNames(listaEquipos), getImageNames(listaEquipos));
            list_equipos.setAdapter(adapterActividades);
    }

    // Este m??todo dado una lista de actividades, devoler?? un array con los nombres de las im??genes
    private String[] getImageNames(ArrayList<Equipo> lista) {
        Iterator<Equipo> itr = lista.iterator();
        Equipo act;
        String[] images = new String[lista.size()];
        int i = 0;
        while(itr.hasNext()) {
            act = itr.next();
            images[i] = act.imagen;
            i++;
        }
        return images;
    }


    // Este m??todo dado una lista de nombres, devolver?? un array con los nombres
    private String[] getNames(ArrayList<Equipo> lista) {
        Iterator<Equipo> itr = lista.iterator();
        Equipo equipo;
        String[] nombres = new String[lista.size()];
        int i = 0;
        while(itr.hasNext()) {
            equipo = itr.next();
            nombres[i] = equipo.nombre;
            i++;
        }
        return nombres;
    }
}