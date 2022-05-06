package com.anderpri.das_grupal.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.anderpri.das_grupal.R;

import java.util.List;
public class Lista_Actividades_Recycler_View_Adapter extends RecyclerView.Adapter<Lista_Actividades_Recycler_View_Adapter.ViewHolder> {

    private List<Actividad> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    //metodo constructor estandar
    Lista_Actividades_Recycler_View_Adapter(Context context, List<Actividad> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    //"infla" el layout cuando es necesario
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.lista_actividades_recycler_view_row, parent, false);
        return new ViewHolder(view);
    }

    //Une la informacion al textview correspondiente de su linea (position)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Actividad actual = mData.get(position);
        holder.miNombre.setText(actual.name);
        holder.miDescripcion.setText(actual.description);
    }

    // Numero total de lineas (elementos)
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // Guarda y recicla las views a medida que se hace scroll por la aplicacion
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView miNombre;
        TextView miDescripcion;
        ViewHolder(View itemView) {
            super(itemView);
            miNombre = itemView.findViewById(R.id.lista_actividades_recycler_view_row_cardview_texto);
            miDescripcion = itemView.findViewById(R.id.lista_actividades_recycler_view_row_cardview_descripcion);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    //Con esto logramos la informacion de la linea id
    Actividad getItem(int id) {
        return mData.get(id);
    }

    // añadimos el listener
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    //se implementara este metodo para poder detectar y responder a los clics
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }




}