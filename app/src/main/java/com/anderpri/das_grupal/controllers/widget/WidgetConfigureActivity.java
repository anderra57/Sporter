package com.anderpri.das_grupal.controllers.widget;

import androidx.appcompat.app.AppCompatActivity;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.anderpri.das_grupal.R;

public class WidgetConfigureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_configure);
        setResult(RESULT_CANCELED);
        int idWidgetAConfigurar = 0;
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            idWidgetAConfigurar = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // Hacer las peticiones oportunas
        Log.d("widget", "paso por aqui");
        Intent resultadoConfig = new Intent();
        resultadoConfig.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, idWidgetAConfigurar);
        setResult(RESULT_OK, resultadoConfig);
        AppWidgetManager manager = AppWidgetManager.getInstance(getApplicationContext());
        Widget.actualizarWidget(getApplicationContext(), manager, idWidgetAConfigurar);
        finish();

    }
}