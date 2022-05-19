package com.anderpri.das_grupal.controllers.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import com.anderpri.das_grupal.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Widget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            actualizarWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void actualizarWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
        Calendar calendario = Calendar.getInstance();
        SimpleDateFormat formato = new SimpleDateFormat("HH:mm:ss");
        String horaconformato = formato.format(calendario.getTime());
        views.setTextViewText(R.id.etiqueta, horaconformato);
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

}