package com.example.user.progetto_ids;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView;
import android.widget.Toast;
import android.content.BroadcastReceiver;


import java.util.ArrayList;

import app_library.MainApplication;
import app_library.maps.components.Floor;
import app_library.maps.components.Node;
import app_library.sharedstorage.Data;

/**
 * Created by User on 25/06/2018.
 */

// activity che permette di scegliere la destinazione nella modalità ricerca e se non presenti le informazioni sulla posizione attuale
public class MapSettingActivity  extends AppCompatActivity {

    // piano di destinazione
    private Floor destinationFloor;

    // stanza di destinazione
    private Node destinationRoom;

    // piano currente in assenza di bluetooth
    private Floor currPosFloor;

    // stanza currente in assenza di bluetooth
    private Node currPosRoom;

    // elementi grafici
    private Button buttonStartSearch;
    private Spinner spinnerDestinationFloor;
    private Spinner spinnerDestinationRoom;
    private Spinner spinnerCurrPosFloor;
    private Spinner spinnerCurrPosRoom;
    private TextView textViewCurrPos;
    private ImageView imageViewCurrPos;

    //insieme dei nomi di piano che compaiono nello spinner
    private ArrayList<String> floorsNameKey;

    //identificativo del messaggio che si può ricevere
    //private static final String STARTMAPS = "STARTMAPS";

    //messaggio impacchettato nell'intent per passare informazioni alla creazione della mappa a pieno schermo
    private String mapExtraInformationDestination;
    private String mapExtraInformationCurrPos;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map_setting);

        // si ottengono i nomi dei piani
        floorsNameKey = MainApplication.obtainFloorsName();

        // impostazione del titolo dell'activity
        getSupportActionBar().setTitle("Modalità ricerca");

        // recupero riferimenti elementi grafici
        spinnerDestinationFloor = (Spinner) findViewById(R.id.spinnerMapSettDestinationFloor);
        spinnerDestinationRoom = (Spinner) findViewById(R.id.spinnerMapSettDestinationRoom);
        spinnerCurrPosFloor = (Spinner) findViewById(R.id.spinnerMapSettCurrPosFloor);
        spinnerCurrPosRoom = (Spinner) findViewById(R.id.spinnerMapSettCurrPosRoom);
        textViewCurrPos = (TextView) findViewById(R.id.textViewMapSettCurrPos);
        imageViewCurrPos = (ImageView) findViewById(R.id.imageViewMapSettCurrPos);
        buttonStartSearch = (Button) findViewById(R.id.buttonMapSettStartSearch);

        // inizializzazione degli eventi alla pressione degli elementi grafici
        initializeViewEvent();
    }


    protected void onStart() {
        super.onStart();

        // si imposta l'activity corrente
        MainApplication.setCurrentActivity(this);

        //inizializzato filtro per i messaggi
        /*IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(STARTMAPS);

        //registrato il receiver nell'activity
        getBaseContext().registerReceiver(broadcastReceiver,intentFilter);*/
    }

    protected void onResume() {
        super.onResume();
        Log.i("back","onresume");

        // activity visibile
        MainApplication.setVisible(true);

        // se nella riapertura dell'app è stata lanciata una notifica di emergenza si apre l'activity della mappa
        MainApplication.openMapActivityEmergencyNotification();
    }


    protected void onPause() {
        super.onPause();

        // activity non visibile
        MainApplication.setVisible(false);

        /*try {
            //cancellata la registrazione del receiver
            if(broadcastReceiver != null)
                getBaseContext().unregisterReceiver(broadcastReceiver);
        }
        catch (IllegalArgumentException e)
        {

        }*/
    }


    // metodo per inizializzare gli eventi alla pressione degli elementi grafici
    private void initializeViewEvent()
    {
        // elementi di default per il piano e la stanza di destinazione
        destinationFloor = MainApplication.getFloors().get(floorsNameKey.get(0));
        destinationRoom = MainApplication.getFloors().get(floorsNameKey.get(0)).getNodes().get(MainApplication.getFloors().get(floorsNameKey.get(0)).getListNameRoomOrBeacon(true).get(0));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                MainApplication.obtainFloorsName()
        );

        spinnerDestinationFloor.setAdapter(adapter);

        //controlla che cosa è stato selezionato sullo spinner del piano di destinazione
        spinnerDestinationFloor.setOnItemSelectedListener(new OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {

                // si recupera il piano di destinazione selezionato
                if (arg2 < floorsNameKey.size())
                    destinationFloor = MainApplication.getFloors().get(floorsNameKey.get(arg2));

                // inizializzaione delle stanze di destinazione in base al piano di destinazione
                ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(
                        MapSettingActivity.this,
                        android.R.layout.simple_spinner_item,
                        destinationFloor.getListNameRoomOrBeacon(true)
                );

                spinnerDestinationRoom.setAdapter(adapter2);

                //controlla che cosa è stato selezionato sullo spinner della stanza di destinazione
                spinnerDestinationRoom.setOnItemSelectedListener(new OnItemSelectedListener()
                {
                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1,
                                               int arg2, long arg3) {

                        // si recupera la stanza di destinazione selezionata
                        if (arg2 < destinationFloor.getListNameRoomOrBeacon(true).size())
                            destinationRoom = MainApplication.getFloors().get(destinationFloor.getFloorName()).getNodes().get(MainApplication.getFloors().get(destinationFloor.getFloorName()).getListNameRoomOrBeacon(true).get(arg2));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0)
                    { }
                });

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            { }
        });


        // booleano per indicare se sono necessari i permessi di localizzazione per utilizzare il bluetooth
        boolean permBluetoothGranted = controlPermissionForBluetoothGranted();
        boolean enableViewCurrPos;

        // ho i permessi necessari
        if (permBluetoothGranted)
        {
            // se il bluetooth è attivo e ho le informazioni sulla posizione corrente dell'utente disabilito gli elementi grafici per selezionare sia il piano che la stanza corrente
            if (MainApplication.controlBluetooth() && Data.getUserPosition().getFloor() != null)
                enableViewCurrPos = false;
            else
                enableViewCurrPos = true;
        }
        else
            enableViewCurrPos = true;

        // se devo ottenere dall'utente le informazioni sul piano e la stanza corrente
        if(enableViewCurrPos)
        {
            // elementi di default per il piano e la stanza corrente
            currPosFloor = MainApplication.getFloors().get(floorsNameKey.get(0));
            currPosRoom = MainApplication.getFloors().get(floorsNameKey.get(0)).getNodes().get(MainApplication.getFloors().get(floorsNameKey.get(0)).getListNameRoomOrBeacon(true).get(0));

            ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_spinner_item,
                    MainApplication.obtainFloorsName()
            );

            spinnerCurrPosFloor.setAdapter(adapter3);

            //controlla che cosa è stato selezionato sullo spinner del piano corrente
            spinnerCurrPosFloor.setOnItemSelectedListener(new OnItemSelectedListener()
            {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1,
                                           int arg2, long arg3) {

                    // si recupera il piano corrente selezionato
                    if (arg2 < floorsNameKey.size())
                        currPosFloor = MainApplication.getFloors().get(floorsNameKey.get(arg2));

                    // inizializzaione delle stanze correnti in base al piano corrente
                    ArrayAdapter<String> adapter4 = new ArrayAdapter<String>(
                            MapSettingActivity.this,
                            android.R.layout.simple_spinner_item,
                            currPosFloor.getListNameRoomOrBeacon(true)
                    );

                    spinnerCurrPosRoom.setAdapter(adapter4);

                    //controlla che cosa è stato selezionato sullo spinner della stanza corrente
                    spinnerCurrPosRoom.setOnItemSelectedListener(new OnItemSelectedListener()
                    {
                        @Override
                        public void onItemSelected(AdapterView<?> arg0, View arg1,
                                                   int arg2, long arg3) {

                            // si recupera la stanza corrente selezionata
                            if (arg2 < currPosFloor.getListNameRoomOrBeacon(true).size())
                                currPosRoom = MainApplication.getFloors().get(currPosFloor.getFloorName()).getNodes().get(MainApplication.getFloors().get(currPosFloor.getFloorName()).getListNameRoomOrBeacon(true).get(arg2));
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> arg0)
                        { }
                    });

                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0)
                { }
            });
        }
        // si disabilitano gli elemnti grafici sul piano e la stanza corrente ottenendo automaticamente tali informazioni
        else
        {
            spinnerCurrPosFloor.setVisibility(View.INVISIBLE);
            spinnerCurrPosFloor.setEnabled(false);

            spinnerCurrPosRoom.setVisibility(View.INVISIBLE);
            spinnerCurrPosRoom.setEnabled(false);

            textViewCurrPos.setVisibility(View.INVISIBLE);
            textViewCurrPos.setEnabled(false);

            imageViewCurrPos.setVisibility(View.INVISIBLE);
            imageViewCurrPos.setEnabled(false);
        }


        // evento che si attiva al click del pulsante per l'avvio della ricerca
        buttonStartSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // recupero piano e stanza di destinazione
                String destFloorName = destinationFloor.getFloorName();
                String destRoomCod = destinationRoom.getRoomCod();

                // messaggio extra per la destinazione da passare alla mappa a pieno schermo
                mapExtraInformationDestination = destFloorName.concat(";").concat(destRoomCod);

                // booleano per indicare se aprire la mappa a pieno schermo
                boolean openMap = true;

                // la losizione corrente non viene acquisita automaticamente
                if (currPosFloor != null)
                {
                    // recupero piano e stanza corrente
                    String currPosFloorName = currPosFloor.getFloorName();
                    String currPosRoomCod = currPosRoom.getRoomCod();

                    // piano e stanza correnti e destinazione coincidono
                    if (currPosFloorName.equals(destFloorName) && currPosRoomCod.equals(destRoomCod))
                    {
                        mapExtraInformationDestination = "";
                        openMap = false;

                        // si indica all'utente che a già raggiunto la destinazione
                        Toast.makeText(MapSettingActivity.this, "Hai già raggiunto la destinazione", Toast.LENGTH_LONG).show();
                    }
                    // messaggio extra per la posizione corrente da passare alla mappa a pieno schermo
                    else
                        mapExtraInformationCurrPos = currPosFloorName.concat(";").concat(currPosRoomCod);
                }
                else
                {
                    // piano e stanza correnti automaticamente e destinazione coincidono
                    if (Data.getUserPosition().getFloor().equals(destFloorName) && Data.getUserPosition().getPosition()[0] == destinationRoom.getCoords()[0] && Data.getUserPosition().getPosition()[1] == destinationRoom.getCoords()[1])
                    {
                        openMap = false;
                        Toast.makeText(MapSettingActivity.this, "Hai già raggiunto la destinazione", Toast.LENGTH_LONG).show();
                    }
                }

                // se i controlli precedenti sono stati superati
                if (openMap)
                {
                    // se la posizione corrente viene acquisita automaticamente con il bluetooth si lancia l'evento per sospendere la scansione che aprirà la mappa a pieno schermo
                    /*if(currPosFloor == null)
                        getApplicationContext().sendBroadcast(new Intent("SuspendScan"));
                    // si apre la mappa a pieno schermo
                    else
                        startActivityMap();*/

                    startActivityMap();
                }
            }
        });
    }


    // metodo che apre l'activity per la mappa a pieno schermo
    private void startActivityMap()
    {
        Intent intentMap = new Intent (this.getApplicationContext(), MapActivity.class);

        // si passa il messaggio extra per la destinazione
        intentMap.putExtra("map_info_dest", mapExtraInformationDestination);

        // si passa il messaggio extra per la posizione corrente senza bluetooth
        if (currPosFloor != null)
            intentMap.putExtra("map_info_curr_pos", mapExtraInformationCurrPos);

        this.startActivity(intentMap);
    }

    // metodo che controlla se si dispongono dei permessi necessari per utilizzare il bluetooth
    private boolean controlPermissionForBluetoothGranted()
    {
        boolean permissionGranted = false;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                permissionGranted = true;
        }
        else
            permissionGranted = true;

        return permissionGranted;
    }

    //il broadcast receiver deputato alla ricezione dei messaggi
    /*private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.i("ACTIVTY MAPS","ricevuto broadcast: " + intent.getAction());

            //questo messaggio viene ricevuto quando si deve passare alla mappa a pieno schermo
            if(intent.getAction().equals(STARTMAPS)) {

                startActivityMap();
            }
        }
    };*/

}
