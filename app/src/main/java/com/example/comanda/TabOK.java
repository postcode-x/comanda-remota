package com.example.comanda;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class TabOK extends Fragment {

    private static final String TAG = "TabOK";

    private static RecyclerView.Adapter adapter;
    private static RecyclerView recyclerView;
    private static ArrayList<DataModel> data;

    private static boolean pendingElements = false;

    private static int contadorEntregados = 0;

    private SharedPreferences settings;

    private SQLiteSolver mSQLiteSolver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSQLiteSolver = new SQLiteSolver(getActivity());

        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        pendingElements=settings.getBoolean("pendingElementsOK", false);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.tab_ok, container, false);

        rootView.setTag(TAG);

        recyclerView = (RecyclerView)  rootView.findViewById(R.id.recycler_view_ok);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        data = new ArrayList<>();

        if(pendingElements)
            restoreFromSQL();

        adapter = new CustomAdapterOK(data);
        recyclerView.setAdapter(adapter);

        return rootView;
    }

    public void addOKData(String numero, String nombre, String boleta, String elementos){

        boolean state = mSQLiteSolver.addData(numero,nombre,boleta, elementos,"TABLAOK");
        Log.i("debug", "addOKDATA?? " + String.valueOf(state));

        data.add(contadorEntregados,new DataModel(numero, nombre, boleta, elementos));
        adapter.notifyItemInserted(contadorEntregados);
        adapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(contadorEntregados);
        contadorEntregados++;


    }

    public void restoreFromSQL(){

        contadorEntregados = 0;
        Cursor dataCursor= mSQLiteSolver.getData("TABLAOK");
        while(dataCursor.moveToNext()){
            data.add(new DataModel(dataCursor.getString(1),dataCursor.getString(2),dataCursor.getString(3),dataCursor.getString(4)));
            contadorEntregados++;
        }

    }

    public void limpiarEntregados(){


        contadorEntregados=0;
        data.clear();
        adapter.notifyDataSetChanged();
        mSQLiteSolver.cleanTable("TABLAOK");

    }

    public int returnEntregados(){

        return contadorEntregados;
    }

    @Override
    public void onPause() {
        super.onPause();

        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("OKCount",contadorEntregados);

        if (contadorEntregados>0){
            editor.putBoolean("pendingElementsOK",true);
        }else
            editor.putBoolean("pendingElementsOK",false);

        editor.apply();

    }
}

