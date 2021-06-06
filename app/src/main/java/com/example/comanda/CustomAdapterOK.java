package com.example.comanda;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CustomAdapterOK extends RecyclerView.Adapter<CustomAdapterOK.MyViewHolder> {
    private ArrayList<DataModel> dataSet;


    public CustomAdapterOK(ArrayList<DataModel> data) {

        this.dataSet = data;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_comanda_entregada, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);

        return myViewHolder;
    }


    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        TextView textBoletaNumero = holder.textBoletaNumero;
        TextView textNombreCliente = holder.textNombreCliente;
        TextView textElementos = holder.textElementos;

        textBoletaNumero.setText(dataSet.get(position).getBoleta());
        textNombreCliente.setText(dataSet.get(position).getNombre());
        textElementos.setText(dataSet.get(position).getElementos());

    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder{

        //TextView textComandaNumero;

        TextView textBoletaNumero;
        TextView textNombreCliente;
        TextView textElementos;

        public MyViewHolder(View itemView) {
            super(itemView);

            this.textBoletaNumero= (TextView) itemView.findViewById(R.id.boleta_numero_ok);
            this.textNombreCliente = (TextView) itemView.findViewById(R.id.nombre_cliente_ok);
            this.textElementos = (TextView) itemView.findViewById(R.id.elementos_comanda_ok);

        }

    }



}