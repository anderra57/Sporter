package com.anderpri.das_grupal.controllers.utils;


import android.content.Context;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatDelegate;

import java.util.Locale;

public class Utils {

    private static Utils INSTANCE;

    public static Utils getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Utils();
        }
        return INSTANCE;
    }

    public void setLocale(String lang, Context context) {
        Configuration config = context.getResources().getConfiguration();
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        config.setLocale(locale);
        config.setLayoutDirection(locale);
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }

    public void setTheme(boolean selected) {
        if (selected) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }
}
