package com.example.comanda;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CustomAdapterPend extends RecyclerView.Adapter<CustomAdapterPend.MyViewHolder> {
    private ArrayList<DataModel> dataSet;


    public CustomAdapterPend(ArrayList<DataModel> data) {

        this.dataSet = data;

    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_comanda_pendiente, parent, false);
        view.setOnLongClickListener(TabPendientes.myOnClickListener);

        return new MyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        TextView textComandaNumero = holder.textComandaNumero;
        TextView textNombreCliente = holder.textNombreCliente;
        TextView textElementos = holder.textElementos;

        textComandaNumero.setText(dataSet.get(position).getNumero());
        textNombreCliente.setText(dataSet.get(position).getNombre());
        textElementos.setText(dataSet.get(position).getElementos());

    }



    @Override
    public int getItemCount() {
        return dataSet.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView textComandaNumero;
        TextView textNombreCliente;
        TextView textElementos;

        public MyViewHolder(View itemView) {
            super(itemView);

            this.textComandaNumero= itemView.findViewById(R.id.comanda_numero_pend);
            this.textNombreCliente = itemView.findViewById(R.id.nombre_cliente_pend);
            this.textElementos = itemView.findViewById(R.id.elementos_comanda_pend);

        }

    }



}