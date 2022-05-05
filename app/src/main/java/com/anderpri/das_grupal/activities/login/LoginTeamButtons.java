package com.anderpri.das_grupal.activities.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.anderpri.das_grupal.R;

public class LoginTeamButtons extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_team_buttons);
    }

    public void createBtn(View v) {
        Intent i = new Intent(this, LoginCreateTeam.class);
        startActivity(i);
        finish();
    }

    public void findBtn(View v) {
        Intent i = new Intent(this, LoginFindTeam.class);
        startActivity(i);
        finish();
    }
}