package com.anderpri.das_grupal.controllers.utils;


import android.content.Context;

public class Utils {

    private static Utils INSTANCE;
    private static Context APPCONTEXT;

    public static Utils getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Utils();
        }
        return INSTANCE;
    }

    public static Context getApplicationContext() {
        return APPCONTEXT;
    }

    public void setApplicationContext(Context context){
        APPCONTEXT = context;
    }
}
