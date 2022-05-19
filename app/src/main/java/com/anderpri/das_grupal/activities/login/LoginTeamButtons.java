package com.anderpri.das_grupal.activities.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ImageView;

import com.anderpri.das_grupal.R;
import com.anderpri.das_grupal.controllers.utils.Utils;

public class LoginTeamButtons extends AppCompatActivity {

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String str = preferences.getString("lang","es");
        Utils.getInstance().setLocale(str,getBaseContext());
        boolean dark = preferences.getBoolean("dark",false);
        Utils.getInstance().setTheme(dark);

        setContentView(R.layout.activity_login_team_buttons);

        ImageView imagen = findViewById(R.id.login_team_buttons_banner);
        if(dark) imagen.setImageResource(R.drawable.sporter_text_alt);
    }

    public void createBtn(View v) {
        Intent i = new Intent(this, LoginCreateTeam.class);
        startActivity(i);
    }

    public void findBtn(View v) {
        Intent i = new Intent(this, LoginFindTeam.class);
        startActivity(i);
    }
}