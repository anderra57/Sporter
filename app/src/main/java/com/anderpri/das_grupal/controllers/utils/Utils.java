package com.anderpri.das_grupal.controllers.utils;


import android.content.Context;
import android.content.res.Configuration;

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
        config.locale = locale;
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
    }

}
