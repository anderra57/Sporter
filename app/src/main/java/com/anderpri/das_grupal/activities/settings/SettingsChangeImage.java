package com.anderpri.das_grupal.activities.settings;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.anderpri.das_grupal.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.IOException;

public class SettingsChangeImage extends AppCompatActivity {

    Button btn, btn_x;
    ImageView img;
    EditText nameText,passText;
    TextView warning;
    int SELECT_PICTURE = 200;
    String cookie;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_change_image);

        btn = findViewById(R.id.settings_change_image_btn_img);
        img = findViewById(R.id.settings_change_image_img);

        btn_x = findViewById(R.id.settings_change_image_btn_x);
        btn_x.setVisibility(View.GONE);

        getDefaultImage();

    }

    private void getDefaultImage() {
        ImageView img = (ImageView) findViewById(R.id.settings_change_image_img);
        // Conseguimos la imagen de firebase
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        String imgpath = "";
        StorageReference path = storageReference.child(imgpath);
        path.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri.toString()).into(img);
            }
        });
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

    // TODO image update

}