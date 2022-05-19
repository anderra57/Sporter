package com.anderpri.das_grupal.activities.login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.anderpri.das_grupal.R;
import com.anderpri.das_grupal.activities.ListaActividadesInscrito;
import com.anderpri.das_grupal.controllers.utils.Utils;
import com.anderpri.das_grupal.controllers.webservices.TeamsWorker;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class LoginCreateTeam extends AppCompatActivity {


    Button btn, btn_x;
    ImageView img;
    EditText nameText,passText;
    TextView warning;
    private static int SELECT_PICTURE = 200;
    String cookie, imageName;
    SharedPreferences preferences;
    private File imgFichero;
    private Uri imagenUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String str = preferences.getString("lang","es");
        Utils.getInstance().setLocale(str,getBaseContext());
        boolean dark = preferences.getBoolean("dark",false);
        Utils.getInstance().setTheme(dark);

        setContentView(R.layout.activity_login_create_team);

        btn = findViewById(R.id.login_create_team_btn_img);
        img = findViewById(R.id.login_create_team_img);
        nameText = findViewById(R.id.login_create_team_txt_name);
        passText = findViewById(R.id.login_create_team_txt_pass);
        warning = findViewById(R.id.login_create_team_txt_warning);
        ImageView imagen = findViewById(R.id.login_create_team_banner);
        if(dark) imagen.setImageResource(R.drawable.sporter_text_alt);

        btn_x = findViewById(R.id.login_create_team_btn_x);
        btn_x.setVisibility(View.GONE);

        getCookie();
        Log.d("cookie_create",cookie);
    }

    private void getCookie() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        cookie = preferences.getString("cookie","");
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == SELECT_PICTURE) {
            try {
                setPfpImage(MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData()));
            } catch (IOException e) {
                e.printStackTrace();
            }

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
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, SELECT_PICTURE);
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

    public void onRegister(View v) {
        String name = nameText.getText().toString();
        String pass = passText.getText().toString();
        imageName = nameText.getText().toString()+".png";

        // Los campos del login no pueden estar vacios
        if(name.isEmpty()) {
            Toast.makeText(this, getString(R.string.login_no_empty_field), Toast.LENGTH_SHORT).show();
            warning.setText(getString(R.string.login_no_empty_field));
            warning.setVisibility(View.VISIBLE);
            warning.setTextColor(Color.RED);
        } else {
            // login al usuario en la aplicación
            try {
                // Preparar los datos para enviar al backend
                Data logindata = new Data.Builder()
                        .putString("funcion", "register")
                        .putString("teamname", name)
                        .putString("teampass", pass)
                        .putString("imageName", imageName)
                        .putString("cookie", cookie)
                        .build();

                // Tiene que existir conexión a internet
                Constraints restricciones = new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build();

                // Preparar la petición
                OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(TeamsWorker.class)
                        .setConstraints(restricciones)
                        .setInputData(logindata)
                        .build();

                // Lanzar la petición
                WorkManager.getInstance(this).getWorkInfoByIdLiveData(req.getId())
                        .observe(this, status -> {
                            if (status != null && status.getState().isFinished()) {
                                String id_user = status.getOutputData().getString("datos").trim();
                                if(id_user.isEmpty()) {
                                    // añadir el token del dispositivo a la base de datos
                                    //addFirebasetoken(username);
                                    // Avanzar a la siguiente actividad (MainActivity)

                                    imageName = nameText.getText().toString()+".png";
                                    subirAFirebase();

                                    Intent intent = new Intent(this, ListaActividadesInscrito.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();

                                } else {
                                    Toast.makeText(this, getString(R.string.login_team_in_use), Toast.LENGTH_SHORT).show();
                                    warning.setText(getString(R.string.login_team_in_use));
                                    warning.setVisibility(View.VISIBLE);
                                    warning.setTextColor(Color.RED);
                                }
                            }
                        });

                WorkManager.getInstance(this).enqueue(req);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void subirAFirebase(){
        byte[] image = getBytes(img);
        if(image != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference();
            StorageReference spaceReference = storageReference.child(imageName);
            spaceReference.putBytes(image);
        }
    }
    private byte[] getBytes(ImageView imageView) {
        try {
            Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] bytesData = stream.toByteArray();
            stream.close();
            return bytesData;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}