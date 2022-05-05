package com.anderpri.das_grupal.controllers;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.anderpri.das_grupal.activities.login.LoginMain;
import com.anderpri.das_grupal.R;
import com.anderpri.das_grupal.controllers.utils.Utils;

public class LoginController extends AppCompatActivity {

    private static LoginController INSTANCE;

    public static LoginController getInstance() {
        if (INSTANCE == null) INSTANCE = new LoginController();
        return INSTANCE;
    }

}
