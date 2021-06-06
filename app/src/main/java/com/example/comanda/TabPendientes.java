package com.example.comanda;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TabPendientes extends Fragment {

    private static final String TAG = "TabPendientes";

    private static RecyclerView.Adapter adapter;
    private static RecyclerView recyclerView;
    private static ArrayList<DataModel> data;

    private int dataSize0 = 0;

    public static View.OnLongClickListener myOnClickListener;

    private static boolean pendingElements = false;

    private static int contadorPendientes = 0;

    private SharedPreferences settings;

    private SQLiteSolver mSQLiteSolver;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSQLiteSolver = new SQLiteSolver(getActivity());

        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        pendingElements=settings.getBoolean("pendingElementsPND", false);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_pendientes, container, false);

        rootView.setTag(TAG);

        myOnClickListener = new MyOnClickListener(getActivity());

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view_pend);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        data = new ArrayList<>();

        if(pendingElements)
            restoreFromSQL();

        adapter = new CustomAdapterPend(data);
        recyclerView.setAdapter(adapter);

        return rootView;
    }


    public static void addPendingData(String text) {

        List<String> items = Arrays.asList(text.split("\\s*,\\s*"));

        String tmpOrden = "";

        for (int i = 3; i < items.size() - 1; i++) {
            tmpOrden = tmpOrden + String.valueOf(i - 2) + ") " + items.get(i) + "\n";
        }

        if (!items.get(items.size() - 1).equalsIgnoreCase("S/C"))
            tmpOrden = tmpOrden + "Comentario: " + items.get(items.size() - 1);

        Log.i("debug",items.get(items.size()-1));

        data.add(contadorPendientes, new DataModel("N° " + items.get(0), "Nombre: " + items.get(1), items.get(2), tmpOrden));
        adapter.notifyItemInserted(contadorPendientes);
        adapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(contadorPendientes);

        contadorPendientes++;

    }

    public void restoreFromSQL(){

        contadorPendientes = 0;
        Cursor dataCursor= mSQLiteSolver.getData("TABLAPEND");
        while(dataCursor.moveToNext()){
            data.add(new DataModel(dataCursor.getString(1),dataCursor.getString(2),dataCursor.getString(3),dataCursor.getString(4)));
            contadorPendientes++;
        }

    }

    public int returnPendientes() {

        return contadorPendientes;
    }

    private class MyOnClickListener implements View.OnLongClickListener {

        private final Context context;

        private MyOnClickListener(Context context) {
            this.context = context;

        }

        @Override
        public boolean onLongClick(final View v) {

            dataSize0 = data.size();

            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(context, R.style.MyDialogTheme);
            } else {
                builder = new AlertDialog.Builder(context, R.style.MyDialogTheme);
            }
            builder.setTitle("Finalizar Comanda")
                    .setMessage("¿Comanda Entregada?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        if (dataSize0 == data.size()){
                            removeItem(v);
                            Toast.makeText(v.getContext(),"Comanda finalizada exitosamente.",Toast.LENGTH_SHORT).show();
                        }else
                            Toast.makeText(v.getContext(),"No se logró finalizar la comanda porque la base de datos cambió de tamaño.",Toast.LENGTH_SHORT).show();

                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();

            return false;
        }

        private void removeItem(View v) {

            contadorPendientes--;

            int selectedItemPosition = recyclerView.getChildAdapterPosition(v);
            MainActivity activity = (MainActivity) getActivity();

            activity.enviaEntregados(
                    data.get(selectedItemPosition).getNumero(),
                    data.get(selectedItemPosition).getNombre(),
                    data.get(selectedItemPosition).getBoleta(),
                    data.get(selectedItemPosition).getElementos());

            data.remove(selectedItemPosition);
            adapter.notifyItemRemoved(selectedItemPosition);

        }

    }

    @Override
    public void onPause() {
        super.onPause();

        mSQLiteSolver.cleanTable("TABLAPEND");

        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("pendingCount",contadorPendientes);

        if (contadorPendientes>0){
            editor.putBoolean("pendingElementsPND",true);


            for(int i=0;i<data.size();i++){
                mSQLiteSolver.addData(data.get(i).getNumero(),
                        data.get(i).getNombre(),
                        data.get(i).getBoleta(),
                        data.get(i).getElementos(),"TABLAPEND");
            }
        }else
            editor.putBoolean("pendingElementsPND",false);

        editor.apply();


    }
}
