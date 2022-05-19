package com.anderpri.das_grupal.activities.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.anderpri.das_grupal.R;
import com.anderpri.das_grupal.controllers.utils.Utils;

import java.util.Locale;

import okhttp3.internal.Util;

public class SettingsFragment extends PreferenceFragmentCompat {

    String rootKeyGlobal;
    SharedPreferences preferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String str = preferences.getString("lang","no_lang");
        Utils.getInstance().setLocale(str,getActivity());
        boolean dark = preferences.getBoolean("dark",false);
        Utils.getInstance().setTheme(dark);

        setPreferencesFromResource(R.xml.root_preferences, rootKey);
        rootKeyGlobal = rootKey;

        String role = preferences.getString("role","");
        if (!role.equals("user")) findPreference("settings_change_info").setEnabled(false);

        findPreference("settings_version").setEnabled(false);
        findPreference("settings_version").setShouldDisableView(false);
        findPreference("settings_devs").setEnabled(false);
        findPreference("settings_devs").setShouldDisableView(false);

        String lang = preferences.getString("lang","es");
        ListPreference listPreference = findPreference("settings_lang");
        if(!lang.equals("en")) listPreference.setValue("es");
        else listPreference.setValue("en");
        findPreference("settings_lang").setOnPreferenceChangeListener((preference, newValue) -> {
            Log.d("changed",preference.getTitle().toString());
            Log.d("changed",newValue.toString());
            setLocaleToSP(newValue.toString());
            Utils.getInstance().setLocale(newValue.toString(),getActivity());
            reloadActivity();
            return true;
        });

        SwitchPreferenceCompat switchPreferenceCompat = findPreference("settings_theme");
        switchPreferenceCompat.setChecked(dark);
        switchPreferenceCompat.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean darkt = (Boolean) newValue;
            Log.d("changed",preference.getTitle().toString());
            Log.d("changed",newValue.toString());
            setThemeToSP(darkt);
            Utils.getInstance().setTheme(darkt);
            /*
            setLocaleToSP(newValue.toString());
            ;*/
            reloadActivity();
            return true;
        });

    }

    private void setThemeToSP(boolean selected) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("dark",selected);
        editor.apply();
    }

    private void setLocaleToSP(String lang) {
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
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