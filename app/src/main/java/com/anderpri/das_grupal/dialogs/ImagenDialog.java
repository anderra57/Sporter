package com.anderpri.das_grupal.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.anderpri.das_grupal.R;

public class ImagenDialog extends DialogFragment {

    ListenerDialog listener;

    public interface ListenerDialog{
        void pulsarCamara();
        void pulsarGaleria();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState){
        super.onCreateDialog(savedInstanceState);
        listener = (ListenerDialog) getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.seleccionarImagen));
        CharSequence[] opciones = {getString(R.string.camara), getString(R.string.galeria)};
        builder.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i == 0){
                    listener.pulsarCamara();
                }else{
                    listener.pulsarGaleria();
                }
            }
        });
        return builder.create();
    }
}

