package com.anderpri.das_grupal.activities;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.anderpri.das_grupal.R;
import com.anderpri.das_grupal.activities.login.LoginMain;
import com.anderpri.das_grupal.fragments.DatePickerFragment;
import com.google.android.material.navigation.NavigationView;

public class CrearActividad extends AppCompatActivity {

    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;

    Button botonCrear;
    EditText nombre, ciudad, fecha, explicacion, numeroParticipantes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_actividad);

        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // This will display an Up icon (<-), we will replace it with hamburger later
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.crear_actividad_drawer_layout);
        nvDrawer = (NavigationView) findViewById(R.id.ina_actividad_navigation_view);
        // Setup drawer view
        setupDrawerContent(nvDrawer);

        botonCrear = findViewById(R.id.crear_actividad_boton);
        nombre = findViewById(R.id.crear_actividad_nombre_text);
        ciudad = findViewById(R.id.crear_actividad_ciudad_text);
        fecha = findViewById(R.id.crear_actividad_fecha_text);
        explicacion = findViewById(R.id.crear_actividad_explicacion_text);
        numeroParticipantes = findViewById(R.id.crear_actividad_numero_text);

        fecha.setOnClickListener(view -> {
            switch (view.getId()){
                case R.id.crear_actividad_fecha_text:
                    showDatePickerDialog();
                    break;
            }
        });

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
                mDrawer.closeDrawer(GravityCompat.START);
                break;
            case R.id.nav_third_fragment:
                break;
            case R.id.nav_cuarto:
                intent = new Intent(this, CrearActividad.class);
                finish();
                startActivity(intent);
                break;
            case R.id.nav_quinto:
                break;
            case R.id.logout:
                intent = new Intent(this, LoginMain.class);
                startActivity(intent);
                finish();
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
                final String selectedDate = day + " / " + (month+1) + " / " + year;
                fecha.setText(selectedDate);
            }
        });

        newFragment.show(getSupportFragmentManager(), "datePicker");
    }
}