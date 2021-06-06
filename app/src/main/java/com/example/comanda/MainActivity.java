package com.example.comanda;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Random;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity {

    private Button ConnectButton;
    private TextView ConnectionStatus;

    private SoundPool mSoundPool;

    private TabOK tabOK ;
    private TabPendientes tabPendientes;
    private TabLayout tabLayout;

    private FloatingActionButton fabPend;
    private FloatingActionButton fabOK;

    private final String TAG = "debug";
    private ServerSocket server = null;
    public static final int TIMEOUT=10;
    private String lineRead = "";

    private int orderCounter = 0;
    private final boolean testMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tabOK = new TabOK();
        tabPendientes = new TabPendientes();

        fabPend = findViewById(R.id.fabPend);
        fabOK = findViewById(R.id.fabOK);

        fabPend.setOnClickListener(v -> Toast.makeText(MainActivity.this,"Pendientes",Toast.LENGTH_SHORT).show());
        fabOK.setOnClickListener(v -> {

            if (tabOK.returnEntregados()>0) {
                AlertDialog.Builder builder;
                builder = new AlertDialog.Builder(v.getContext(), R.style.MyDialogTheme);
                builder.setTitle("Se limpiará la lista")
                        .setMessage("¿Vaciar la lista de Entregados?")
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            tabOK.limpiarEntregados();
                            setEntregadosTab();
                            Toast.makeText(MainActivity.this, "Lista entregados vaciada", Toast.LENGTH_SHORT).show();

                        })
                        .setNegativeButton(android.R.string.no, (dialog, which) -> {

                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();

            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        ViewPager mViewPager = findViewById(R.id.container);
        mViewPager.setOffscreenPageLimit(2);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                showRightFab(position);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        ConnectButton = findViewById(R.id.init_connection);
        ConnectionStatus = findViewById(R.id.Connectionstatus);

        showRightFab(mViewPager.getCurrentItem());

        if (testMode){
            ConnectionStatus.setText(getResources().getString(R.string.test_mode));
        }else{
            ConnectionStatus.setText(getResources().getString(R.string.disconnected));
        }

        ConnectButton.setOnClickListener(v -> {
            if (testMode){
                testOrderGenerator();
            }
            else {
                ConnectButton.setEnabled(false);
                initializeConnectionThread();
            }
        });

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

        tabLayout.getTabAt(0).setText( getResources().getString(R.string.pending) + "(" + settings.getInt("pendingCount", 0) + ")");
        tabLayout.getTabAt(1).setText( getResources().getString(R.string.delivered) + "(" + settings.getInt("OKCount", 0) + ")");
    }

    public void showRightFab(int tab) {
        switch (tab) {
            case 0:
                fabOK.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                    @Override
                    public void onHidden(FloatingActionButton fab) {
                        fabPend.show();
                    }
                });
                break;

            case 1:
                fabPend.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                    @Override
                    public void onHidden(FloatingActionButton fab) {
                        fabOK.show();
                    }
                });
                break;

            default:
                fabPend.hide();
                fabOK.hide();
                break;
        }
    }

    public void testOrderGenerator(){

        String[] clients = { "Johnny" , "Lisa", "Denny", "Mark", "Doggy" };
        String[] orders = { "Sandwich" , "Tea", "Hamburger", "Soup", "Ice Cream" };

        orderCounter += 1;

        String inputOrder = orderCounter +
                ", " + clients[new Random().nextInt(5)] +
                ", #" + new Random().nextInt(999999) +
                ", " + orders[new Random().nextInt(5)];

        TabPendientes.addPendingData(inputOrder);
        setPendientesTab();
        // playAlertaComanda();

    }
    public void initializeConnectionThread() {

        Thread ConnectionThread = new Thread(() -> {

            Socket client=null;

            try {

                server = new ServerSocket(38300);
                server.setSoTimeout(TIMEOUT * 2000);
                runOnUiThread(() -> ConnectionStatus.setText(R.string.waiting_connection));
                Log.d(TAG, "Waiting for connection.");

                client = server.accept();
                runOnUiThread(() -> ConnectionStatus.setText(R.string.connection_accepted));
                Log.d(TAG, "Accepted");

                Globals.socketIn = new Scanner(client.getInputStream());
                Globals.socketOut = new PrintWriter(client.getOutputStream(), true);


            } catch (SocketTimeoutException e) {
                // print out TIMEOUT
                runOnUiThread(() -> ConnectionStatus.setText(R.string.connection_timeout));
                Log.d(TAG, "Connection timeout");
            } catch (IOException e) {
                Log.e(TAG, "" + e);
            } finally {
                //close the server socket
                try {
                    if (server!=null){
                        server.close();
                        runOnUiThread(() -> {
                            ConnectionStatus.setText(R.string.server_disconnected);
                            //ConnectButton.setEnabled(true);
                        });
                        Log.d(TAG, "Closing server");

                    }
                } catch (IOException ec) {
                    Log.e(TAG, "Cannot close server socket" + ec);
                }
            }

            if (client!=null) {

                Globals.connected = true;
                // print out success
                runOnUiThread(() -> ConnectionStatus.setText(R.string.connection_accepted));

                startReading();
            }

        });

        ConnectionThread.start();
    }

    public void startReading(){

        new Thread() {
            @Override
            public void run() {

                while(Globals.connected) {

                    try {
                        Globals.socketOut.write("XXX");
                        Globals.socketOut.flush();

                        lineRead = Globals.socketIn.nextLine();
                        if (!lineRead.equalsIgnoreCase("\n")){
                            Log.d(TAG, lineRead);
                            runOnUiThread(() -> {
                                TabPendientes.addPendingData(lineRead);
                                setPendientesTab();
                                playAlertaComanda();

                            });
                        }

                        // Thread.sleep(1000);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Globals.connected = false;
                        runOnUiThread(() -> {
                            ConnectionStatus.setText(R.string.connection_closed);
                            ConnectButton.setEnabled(true);
                        });

                    }

                }
            }
        }.start();

    }

    public void enviaEntregados(String numero, String nombre, String boleta, String elementos){

        tabOK.addOKData(numero,nombre,boleta,elementos);
        setPendientesTab();
        setEntregadosTab();

    }

    public void setPendientesTab(){

        tabLayout.getTabAt(0).setText(getResources().getString(R.string.pending) +  "(" + tabPendientes.returnPendientes() + ")");

    }

    public void setEntregadosTab(){

        tabLayout.getTabAt(1).setText(getResources().getString(R.string.delivered) + "(" + tabOK.returnEntregados() + ")");

    }

    public void playAlertaComanda(){

        final int sound_id = this.getResources().getIdentifier("bell", "raw", this.getPackageName());

        Thread audioThread = new Thread() {

            @Override
            public void run() {

                final int thisNote = mSoundPool.load(MainActivity.this, sound_id, 1);

                mSoundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> soundPool.play(thisNote, 2f, 2f, 1, 0, 2f));

            }
        };
        audioThread.start();

    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {

        }

        /*public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }*/

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = rootView.findViewById(R.id.section_label);
            return rootView;
        }
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            switch(position) {
                case 0:
                    return tabPendientes;
                case 1:
                    return tabOK;
            }

            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }


        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getResources().getString(R.string.pending);
                case 1:
                    return getResources().getString(R.string.delivered);
            }
            return null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        mSoundPool.release();

    }

    @Override
    protected void onResume() {
        super.onResume();

        createBeforeAPI21 ();

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void createBeforeAPI21 () {

        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mSoundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);

    }


}
