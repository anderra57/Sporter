package com.anderpri.das_grupal.activities.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ListenableWorker;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.anderpri.das_grupal.R;
import com.anderpri.das_grupal.activities.UnaActividad;

import java.io.IOException;

public class LoginCreateTeam extends AppCompatActivity {


    Button btn, btn_x;
    ImageView img;
    EditText name;
    TextView warning;
    int SELECT_PICTURE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_create_team);

        btn = findViewById(R.id.login_create_team_btn_img);
        img = findViewById(R.id.login_create_team_img);
        name = findViewById(R.id.login_create_team_txt_name);
        warning = findViewById(R.id.login_create_team_txt_warning);

        btn_x = findViewById(R.id.login_create_team_btn_x);
        btn_x.setVisibility(View.GONE);

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == SELECT_PICTURE && null != data.getData()) {
            try { setPfpImage(MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData())); }
            catch (IOException e) { e.printStackTrace(); }
        }
    }

    private void setPfpImage(Bitmap bitmap) {
        Bitmap newBitmap = cropToSquare(bitmap);
        img.setImageBitmap(newBitmap);
        btn_x.setVisibility(View.VISIBLE);
    }


    public void openGallery(View view) {
        // create an instance of the
        // intent of the type image
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }


    public void clickX(View view) {
        img.setImageResource(R.drawable.default_icon_group);
        btn_x.setVisibility(View.GONE);
    }



    private Bitmap cropToSquare(Bitmap bitmap){
        int width  = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = Math.min(height, width);
        int newHeight = (height > width)? height - ( height - width) : height;
        int cropW = (width - height) / 2;
        cropW = Math.max(cropW, 0);
        int cropH = (height - width) / 2;
        cropH = Math.max(cropH, 0);

        return Bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newHeight);
    }

    public void onRegister(View view) {
        String teamName = this.name.getText().toString();
        if(teamName.isEmpty()){
            Toast.makeText(this, getString(R.string.ningunCampoVacio), Toast.LENGTH_SHORT).show();
            warning.setText(getString(R.string.ningunCampoVacio));
            warning.setVisibility(View.VISIBLE);
            warning.setTextColor(Color.RED);
        }
        else{
            teamExists(teamName);
        }
    }

    private void teamExists(String team) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            Toast.makeText(this, getString(R.string.noInternet), Toast.LENGTH_SHORT).show();
        }
        Data datos = new Data.Builder().putString("team", team).build();
        Constraints restricciones = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
        OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(TeamExistsWebService.class).setInputData(datos).setConstraints(restricciones).build();
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(req.getId()).observe(this, status -> {
            if(status != null && status.getState().isFinished()) {
                boolean teamExists = status.getOutputData().getBoolean("existe", false);
                if (team.equals("a")) teamExists = true; // temporal
                if(teamExists){
                    Toast.makeText(this, "Ya existe un equipo con ese nombre. Escoge otro", Toast.LENGTH_SHORT).show();
                    warning.setText("Ya existe un equipo con ese nombre. Escoge otro");
                    warning.setVisibility(View.VISIBLE);
                    warning.setTextColor(Color.RED);
                }
                else{
                    Intent i = new Intent(this, UnaActividad.class);
                    startActivity(i);
                    finish();
                }
            }
        });
        WorkManager.getInstance(this).enqueue(req);
    }

    public static class TeamExistsWebService extends Worker {

        public TeamExistsWebService(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
        }

        @NonNull
        @Override
        public ListenableWorker.Result doWork() {
            Data datos = new Data.Builder().putBoolean("existe", false).build();
            return Result.success(datos);
        }
    }

}