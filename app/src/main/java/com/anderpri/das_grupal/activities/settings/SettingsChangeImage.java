package com.anderpri.das_grupal.activities.settings;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.anderpri.das_grupal.R;
import com.anderpri.das_grupal.controllers.utils.Utils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SettingsChangeImage extends AppCompatActivity {

    Button btn, btn_x;
    ImageView img;
    EditText nameText,passText;
    TextView warning;
    int SELECT_PICTURE = 200;
    String imageName;
    SharedPreferences preferences;
    Bitmap originalImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String str = preferences.getString("lang","no_lang");
        Utils.getInstance().setLocale(str,getBaseContext());

        setContentView(R.layout.activity_settings_change_image);

        btn = findViewById(R.id.settings_change_image_btn_img);
        img = findViewById(R.id.settings_change_image_img);

        btn_x = findViewById(R.id.settings_change_image_btn_x);
        btn_x.setVisibility(View.GONE);

        Picasso.get().load(R.drawable.loading).into(img);

        getImage();
    }

    private void getImage() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String teamName = preferences.getString("teamname",null);
        imageName = teamName + ".png";
        getDefaultImage();
    }

    private void getDefaultImage() {
        ImageView img = (ImageView) findViewById(R.id.settings_change_image_img);
        // Conseguimos la imagen de firebase
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        StorageReference path = storageReference.child(imageName);
        path.getDownloadUrl().addOnSuccessListener(uri -> Picasso.get().load(uri.toString()).into(img, new Callback() {
            @Override public void onSuccess() { originalImage = ((BitmapDrawable) img.getDrawable()).getBitmap(); }
            @Override public void onError(Exception e) { e.printStackTrace(); }
        }));
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
        img.setImageBitmap(originalImage);
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

    public void onDefault(View view) {
        img.setImageResource(R.drawable.default_icon_group);
        btn_x.setVisibility(View.VISIBLE);
    }

    public void onChangeBtn(View view) {
        subirAFirebase();
    }

    private void subirAFirebase(){
        byte[] image = getBytes(img);
        if(image != null) {
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReference();
            StorageReference spaceReference = storageReference.child(imageName);
            spaceReference.putBytes(image);
            Toast.makeText(this, getString(R.string.settings_change_image_changed), Toast.LENGTH_SHORT).show();
            originalImage = ((BitmapDrawable) img.getDrawable()).getBitmap();
            btn_x.setVisibility(View.GONE);
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