package com.anderpri.das_grupal.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.anderpri.das_grupal.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class AdapterActividades extends BaseAdapter {

    private Context context;
    private LayoutInflater layoutInflater;
    private String[] titulos;
    private String[] imagenes;

    public AdapterActividades(Context pContext, String[] pTitulos, String[] pImagenes) {
        context = pContext;
        titulos = pTitulos;
        imagenes = pImagenes;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return titulos.length;
    }

    @Override
    public Object getItem(int i) {
        return titulos[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        // fila personalizada para mostrar en la lista
        view = layoutInflater.inflate(R.layout.lista_actividades_recycler_view_row, null);
        // Inicializar valores de la fila
        TextView nombre = (TextView) view.findViewById(R.id.lista_actividades_recycler_view_row_cardview_texto);
        // Añadirlos a la lista
        nombre.setText(titulos[i]);

        ImageView img = (ImageView) view.findViewById(R.id.fow_imageview);
        // Conseguimos la imagen de firebase
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference();
        StorageReference path = storageReference.child(imagenes[i]);
        path.getDownloadUrl().addOnSuccessListener(uri -> Picasso.get().load(uri.toString()).into(img)).addOnFailureListener(e -> img.setImageResource(R.drawable.default_activity));
        img.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

        return view;
    }
}
