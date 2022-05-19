package com.anderpri.das_grupal.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.anderpri.das_grupal.R;
import com.anderpri.das_grupal.controllers.utils.Utils;
import com.anderpri.das_grupal.controllers.webservices.ActivitiesAdminWorker;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class Maps extends AppCompatActivity implements OnMapReadyCallback {

    private Button boton;
    private double latitude, longitude;
    private String ubicacion, cookie;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String str = preferences.getString("lang","no_lang");
        Utils.getInstance().setLocale(str,getBaseContext());
        boolean dark = preferences.getBoolean("dark",false);
        Utils.getInstance().setTheme(dark);

        Bundle extras = getIntent().getExtras();
        if(extras != null){
            ubicacion = extras.getString("ubicacion");
            latitude = Double.valueOf(extras.getString("latitude"));
            longitude = Double.valueOf(extras.getString("longitude"));
        }
        setContentView(R.layout.activity_maps);
        getCookie();
        boton = findViewById(R.id.btn_mapa);

        boton.setText(R.string.aceptar);
        SupportMapFragment mapa = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentoMapa);
        mapa.getMapAsync(this);

        Places.initialize(getApplicationContext(), getString(R.string.apiKey));
        PlacesClient placesClient = Places.createClient(this);

        AutocompleteSupportFragment autoCompleteSupportFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentoBarra);
        autoCompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS));
        autoCompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onError(@NonNull Status status) {
                Log.i("prueba", "An error occurred: " + status);
            }

            @Override
            public void onPlaceSelected(@NonNull Place place) {
                latitude = place.getLatLng().latitude;
                longitude = place.getLatLng().longitude;
                ubicacion = place.getAddress();
                Intent returnIntent = new Intent();
                returnIntent.putExtra("latitude", String.valueOf(latitude));
                returnIntent.putExtra("longitude", String.valueOf(longitude));
                returnIntent.putExtra("ubicacion", String.valueOf(ubicacion));
                Toast.makeText(getApplicationContext(), getText(R.string.ubicacionCorrecta), Toast.LENGTH_SHORT).show();
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
        boton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ubicacion != null){
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("latitude", String.valueOf(latitude));
                    returnIntent.putExtra("longitude", String.valueOf(longitude));
                    returnIntent.putExtra("ubicacion", String.valueOf(ubicacion));
                    Toast.makeText(getApplicationContext(), getText(R.string.ubicacionCorrecta), Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }else{
                    Intent returnIntent = new Intent();
                    setResult(Activity.RESULT_CANCELED, returnIntent);
                    Toast.makeText(getApplicationContext(), getText(R.string.ubicacionFallida), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    private void getCookie() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        cookie = preferences.getString("cookie","");
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if(ubicacion != null) cargarMarcador(googleMap);
    }

    private void cargarMarcador(GoogleMap map){
        map.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title(ubicacion));
    }
}