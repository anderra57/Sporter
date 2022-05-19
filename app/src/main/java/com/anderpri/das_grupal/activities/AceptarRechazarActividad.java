package com.anderpri.das_grupal.activities;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.anderpri.das_grupal.R;
import com.anderpri.das_grupal.activities.login.LoginMain;
import com.anderpri.das_grupal.adapters.AdapterActividades;
import com.anderpri.das_grupal.controllers.utils.Utils;
import com.anderpri.das_grupal.controllers.webservices.ActivitiesAdminWorker;
import com.anderpri.das_grupal.controllers.webservices.CrearActividadWorker;
import com.anderpri.das_grupal.controllers.webservices.SolicitudesWorker;
import com.anderpri.das_grupal.controllers.webservices.SugerenciasWorker;
import com.anderpri.das_grupal.controllers.webservices.UsersWorker;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class AceptarRechazarActividad extends AppCompatActivity {
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;
    private String cookie;
    private SharedPreferences preferences;

    private AdapterActividades adapterActividades;
    private ArrayList<Actividad> listaActividades;
    private ListView list_actividades;

    public AceptarRechazarActividad() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String str = preferences.getString("lang","no_lang");
        Utils.getInstance().setLocale(str,getBaseContext());
        boolean dark = preferences.getBoolean("dark",false);
        Utils.getInstance().setTheme(dark);

        setContentView(R.layout.activity_aceptar_rechazar_actividad);

        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // This will display an Up icon (<-), we will replace it with hamburger later
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.aceptar_rechazar_actividad_drawer_layout);
        nvDrawer = (NavigationView) findViewById(R.id.navigation_view);
        // Setup drawer view
        setupDrawerContent(nvDrawer);


        // Inicalizar la lista de actividades
        listaActividades = new ArrayList<Actividad>();
        list_actividades = (ListView) findViewById(R.id.lista_actividades_recycler_view);

        getCookie();
        getActivities();

        list_actividades.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                visualizarActividad(listaActividades.get(i));
            }
        });

    }
    private void visualizarActividad(Actividad actividad) {
        Intent intent = new Intent(this, AceptarRechazar.class);
        intent.putExtra("titulo", actividad.name);
        intent.putExtra("descripcion", actividad.description);
        intent.putExtra("fecha", actividad.fecha);
        intent.putExtra("ciudad", actividad.city);
        intent.putExtra("imagen", actividad.image);
        startActivity(intent);
    }

    private void listarActividades() {
        if (listaActividades.size() != 0) {
            adapterActividades = new AdapterActividades(this, getTitles(listaActividades), getImageNames(listaActividades));
            list_actividades.setAdapter(adapterActividades);
        } else {
            Toast.makeText(this, getString(R.string.noActividades), Toast.LENGTH_SHORT).show();
        }
    }

    // Este método dado una lista de actividades, devolerá un array con los nombres de las imágenes
    private String[] getImageNames(ArrayList<Actividad> lista) {
        Iterator<Actividad> itr = lista.iterator();
        Actividad act;
        String[] images = new String[lista.size()];
        int i = 0;
        while(itr.hasNext()) {
            act = itr.next();
            images[i] = act.image;
            i++;
        }
        return images;
    }

    // Este método dado una lista de actividades, devolverá un array con los títulos
    private String[] getTitles(ArrayList<Actividad> lista) {
        Iterator<Actividad> itr = lista.iterator();
        Actividad act;
        String[] titles = new String[lista.size()];
        int i = 0;
        while(itr.hasNext()) {
            act = itr.next();
            titles[i] = act.name;
            i++;
        }
        return titles;
    }

    private void getCookie() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        cookie = preferences.getString("cookie","");
    }

    private void getActivities() {

        try {
            // Preparar los datos para enviar al backend
            Data data = new Data.Builder()
                    .putString("funcion", "mostrarSolicitudes")
                    .putString("cookie", cookie)
                    .build();

            // Tiene que existir conexión a internet
            Constraints restricciones = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();

            // Preparar la petición
            OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(SugerenciasWorker.class)
                    .setConstraints(restricciones)
                    .setInputData(data)
                    .build();

            // Lanzar la petición
            WorkManager.getInstance(this).getWorkInfoByIdLiveData(req.getId())
                    .observe(this, status -> {
                        if (status != null && status.getState().isFinished()) {
                            try {
                                JSONArray miArray = new JSONArray(status.getOutputData().getString("datos"));
                                for(int i = 0; i<miArray.length(); i++){ //asi es, no se hacer un foreach en java
                                    JSONObject miJson = new JSONObject(miArray.get(i).toString());
                                    String city = miJson.getString("city");
                                    if(city.equals("null")){
                                        city = getString(R.string.sinEspecificar);
                                    }
                                    Actividad actual= new Actividad(miJson.getString("actividad"),miJson.getString("description"),miJson.getString("fecha"),city,miJson.getString("imageName"),miJson.getString("latitude"),miJson.getString("longitude"));
                                    listaActividades.add(actual);
                                }
                                listarActividades();
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
                intent = new Intent(this, ListaActividadesAdmin.class);
                finish();
                startActivity(intent);
                break;
            case R.id.nav_second_fragment:
                intent = new Intent(this, CrearActividad.class);
                finish();
                startActivity(intent);
                break;
            case R.id.nav_third_fragment:
                mDrawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.nav_fourth_fragment:
                intent = new Intent(this, ActividadesMapaAdmin.class);
                finish();
                startActivity(intent);
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

    /*@Override
    public void onItemClick(View view, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.acerptarRechazarActividad));
        builder.setMessage(getString(R.string.aceptarActividad));
        builder.setPositiveButton(getString(R.string.si), (dialogInterface, i) -> {
            aceptarActividad(listaActividades.get(position).name);
        });
        builder.setNegativeButton(getString(R.string.no), (dialogInterface, i) -> {
            rechazarActividad(listaActividades.get(position).name);
        });
        builder.show();
    }

    private void aceptarActividad(String actividad){

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
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(req.getId()).observe(this, status -> {
                    if (status != null && status.getState().isFinished()) {
                        Toast.makeText(this, R.string.activiadadAceptadaExito, Toast.LENGTH_SHORT).show();
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                });

        WorkManager.getInstance(this).enqueue(req);
    }

    private void rechazarActividad(String actividad){
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
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(req.getId()).observe(this, status -> {
            if (status != null && status.getState().isFinished()) {
                Toast.makeText(this, R.string.actividadRechazadaExito, Toast.LENGTH_SHORT).show();
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });

        WorkManager.getInstance(this).enqueue(req);
    }*/
}