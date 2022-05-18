package com.anderpri.das_grupal.activities.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.preference.PreferenceFragmentCompat;

import com.anderpri.das_grupal.R;
import com.anderpri.das_grupal.controllers.utils.Utils;

import java.util.Locale;

import okhttp3.internal.Util;

public class SettingsFragment extends PreferenceFragmentCompat {

    String rootKeyGlobal;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String str = preferences.getString("lang","no_lang");
        Utils.getInstance().setLocale(str,getActivity());

        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        rootKeyGlobal = rootKey;

        String role = preferences.getString("role","");
        if (!role.equals("user")) findPreference("settings_change_info").setEnabled(false);

        findPreference("settings_version").setEnabled(false);
        findPreference("settings_version").setShouldDisableView(false);
        findPreference("settings_devs").setEnabled(false);
        findPreference("settings_devs").setShouldDisableView(false);

        String lang = preferences.getString("lang","es");
        findPreference("settings_lang").setDefaultValue(lang);
        findPreference("settings_lang").setOnPreferenceChangeListener((preference, newValue) -> {
            Log.d("changed",preference.getTitle().toString());
            Log.d("changed",newValue.toString());
            setLocaleToSP(newValue.toString());
            Utils.getInstance().setLocale(newValue.toString(),getActivity());
            reloadActivity();
            return true;
        });

        /*
        findPreference("settings_theme").setOnPreferenceChangeListener((preference, newValue) -> {
            Log.d("changed",preference.getTitle().toString());
            Log.d("changed",newValue.toString());
            return true;
        });*/
    }

    private void setLocaleToSP(String lang) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("lang", lang);
        editor.apply();
    }

    private void reloadActivity() {

        Intent intent = new Intent(getContext(), SettingsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

    }

/*
    private void reloadActivity() {
        Intent i = new Intent(this, Ajustes.class);
        finish();
        overridePendingTransition(0, 0);
        startActivity(i);
        overridePendingTransition(0, 0);
    }*/


}