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
import android.widget.TextView;
import android.widget.Toast;

import com.anderpri.das_grupal.R;
import com.anderpri.das_grupal.activities.login.LoginMain;
import com.anderpri.das_grupal.activities.settings.SettingsActivity;
import com.anderpri.das_grupal.adapters.AdapterActividades;
import com.anderpri.das_grupal.controllers.utils.Utils;
import com.anderpri.das_grupal.controllers.webservices.ActivitiesAdminWorker;
import com.anderpri.das_grupal.controllers.webservices.ActivitiesWorker;
import com.anderpri.das_grupal.controllers.webservices.CrearActividadWorker;
import com.anderpri.das_grupal.controllers.webservices.SolicitudesWorker;
import com.anderpri.das_grupal.controllers.webservices.SugerenciasWorker;
import com.anderpri.das_grupal.controllers.webservices.UsersWorker;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class ActividadesMapa extends AppCompatActivity implements OnMapReadyCallback {
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;
    private String cookie;
    private SharedPreferences preferences;
    private TextView nombreTextView, equipoTextView;
    private String nombreString, equipoString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String str = preferences.getString("lang","no_lang");
        Utils.getInstance().setLocale(str,getBaseContext());
        boolean dark = preferences.getBoolean("dark",false);
        Utils.getInstance().setTheme(dark);

        setContentView(R.layout.activity_actividades_mapa);

        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // This will display an Up icon (<-), we will replace it with hamburger later
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.actividades_mapa_drawer_layout);
        nvDrawer = (NavigationView) findViewById(R.id.navigation_view);
        // Setup drawer view
        setupDrawerContent(nvDrawer);

        SupportMapFragment mapa = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentoActividadesMapa);
        mapa.getMapAsync(this);

        getCookie();

        setUsernameTeamname();
    }

    private void getCookie() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        cookie = preferences.getString("cookie","");
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
                intent = new Intent(this, SugerirActividad.class);
                finish();
                startActivity(intent);
                break;
            case R.id.nav_fourth_fragment:
                mDrawer.closeDrawer(GravityCompat.START);
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
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        cargarMarcadores(googleMap);

    }

    private void cargarMarcadores(GoogleMap googleMap) {
        try {
            // Preparar los datos para enviar al backend
            Data data = new Data.Builder()
                    .putString("funcion", "getCoordinates")
                    .putString("cookie", cookie)
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
                            try {
                                JSONArray miArray = new JSONArray(status.getOutputData().getString("datos"));
                                for(int i = 0; i<miArray.length(); i++){ //asi es, no se hacer un foreach en java
                                    JSONObject miJson = new JSONObject(miArray.get(i).toString());
                                    String name = miJson.getString("name");
                                    String latitud = miJson.getString("latitude");
                                    String longitud = miJson.getString("longitude");

                                    if(!latitud.equals("null") && !longitud.equals("null")){
                                        Double latitude = Double.valueOf(latitud);
                                        Double longitude = Double.valueOf(longitud);
                                        googleMap.addMarker(new MarkerOptions()
                                                .position(new LatLng(latitude, longitude))
                                                .title(name));
                                    }
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


    private void setUsernameTeamname() {
        View headerView = nvDrawer.getHeaderView(0);
        nombreTextView = (TextView) headerView.findViewById(R.id.usuario);
        equipoTextView = (TextView) headerView.findViewById(R.id.equipo);

        nombreString = preferences.getString("username", "");
        nombreTextView.setText("@"+nombreString);
        equipoString = preferences.getString("teamname", "");
        equipoTextView.setText("#"+equipoString);
    }
}