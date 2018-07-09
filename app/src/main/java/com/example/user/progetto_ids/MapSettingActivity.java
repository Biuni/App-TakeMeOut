package com.example.user.progetto_ids;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import app_library.MainApplication;
import app_library.maps.components.Floor;
import app_library.maps.components.Node;
import app_library.sharedstorage.Data;
import app_library.utility.CSVHandler;

/**
 * Created by User on 25/06/2018.
 */

public class MapSettingActivity  extends AppCompatActivity {

    // piano di destinazione
    private Floor destinationFloor;

    // stanza di destinazione
    private Node destinationRoom;

    // piano currente in assenza di bluetooth
    private Floor currPosFloor;

    // stanza currente in assenza di bluetooth
    private Node currPosRoom;

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
    private static final String STARTMAPS = "STARTMAPS";

    //messaggio impacchettato nell'intent per passare informazioni alla creazione di FullScreenMap
    private String mapExtraInformationDestination;
    private String mapExtraInformationCurrPos;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map_setting);

        floorsNameKey = MainApplication.obtainFloorsName();

        getSupportActionBar().setTitle("Modalità ricerca");

        //destinationRoom = new Node(null,null, null, null, 0);

        spinnerDestinationFloor = (Spinner) findViewById(R.id.spinnerMapSettDestinationFloor);
        spinnerDestinationRoom = (Spinner) findViewById(R.id.spinnerMapSettDestinationRoom);
        spinnerCurrPosFloor = (Spinner) findViewById(R.id.spinnerMapSettCurrPosFloor);
        spinnerCurrPosRoom = (Spinner) findViewById(R.id.spinnerMapSettCurrPosRoom);
        textViewCurrPos = (TextView) findViewById(R.id.textViewMapSettCurrPos);
        imageViewCurrPos = (ImageView) findViewById(R.id.imageViewMapSettCurrPos);
        buttonStartSearch = (Button) findViewById(R.id.buttonMapSettStartSearch);

        initializeViewEvent();
    }


    protected void onStart() {
        super.onStart();

        MainApplication.setCurrentActivity(this);

        //inizializzato filtro per i messaggi
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(STARTMAPS);

        //registrato il receiver nell'activity
        getBaseContext().registerReceiver(broadcastReceiver,intentFilter);
    }

    protected void onResume() {
        super.onResume();
        Log.i("back","onresume");
        MainApplication.setVisible(true);
    }


    protected void onPause() {
        super.onPause();
        MainApplication.setVisible(false);

        //cancellata la registrazione del receiver
        if(broadcastReceiver != null)
            getBaseContext().unregisterReceiver(broadcastReceiver);
    }


    private void initializeViewEvent()
    {
        destinationFloor = MainApplication.getFloors().get(floorsNameKey.get(0));
        destinationRoom = MainApplication.getFloors().get(floorsNameKey.get(0)).getNodes().get(MainApplication.getFloors().get(floorsNameKey.get(0)).getListNameRoomOrBeacon(true).get(0));

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                MainApplication.obtainFloorsName()
        );

        spinnerDestinationFloor.setAdapter(adapter);

        //controlla che cosa è stato selezionato sullo spinner del piano
        spinnerDestinationFloor.setOnItemSelectedListener(new OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {

                if (arg2 < floorsNameKey.size())
                    destinationFloor = MainApplication.getFloors().get(floorsNameKey.get(arg2));

                ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(
                        MapSettingActivity.this,
                        android.R.layout.simple_spinner_item,
                        destinationFloor.getListNameRoomOrBeacon(true)
                );

                spinnerDestinationRoom.setAdapter(adapter2);

                spinnerDestinationRoom.setOnItemSelectedListener(new OnItemSelectedListener()
                {
                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1,
                                               int arg2, long arg3) {

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


        boolean permBluetoothGranted = controlPermissionForBluetoothGranted();
        boolean enableViewCurrPos;

        if (permBluetoothGranted)
        {
            if (MainApplication.controlBluetooth() && Data.getUserPosition().getFloor() != null)
                enableViewCurrPos = false;
            else
                enableViewCurrPos = true;
        }
        else
            enableViewCurrPos = true;

        if(enableViewCurrPos)
        {
            //currPosRoom = new Node(null,null, null, null, 0);

            currPosFloor = MainApplication.getFloors().get(floorsNameKey.get(0));
            currPosRoom = MainApplication.getFloors().get(floorsNameKey.get(0)).getNodes().get(MainApplication.getFloors().get(floorsNameKey.get(0)).getListNameRoomOrBeacon(true).get(0));

            ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_spinner_item,
                    MainApplication.obtainFloorsName()
            );

            spinnerCurrPosFloor.setAdapter(adapter3);

            //controlla che cosa è stato selezionato sullo spinner del piano
            spinnerCurrPosFloor.setOnItemSelectedListener(new OnItemSelectedListener()
            {
                @Override
                public void onItemSelected(AdapterView<?> arg0, View arg1,
                                           int arg2, long arg3) {

                    if (arg2 < floorsNameKey.size())
                        currPosFloor = MainApplication.getFloors().get(floorsNameKey.get(arg2));

                    ArrayAdapter<String> adapter4 = new ArrayAdapter<String>(
                            MapSettingActivity.this,
                            android.R.layout.simple_spinner_item,
                            currPosFloor.getListNameRoomOrBeacon(true)
                    );

                    spinnerCurrPosRoom.setAdapter(adapter4);

                    spinnerCurrPosRoom.setOnItemSelectedListener(new OnItemSelectedListener()
                    {
                        @Override
                        public void onItemSelected(AdapterView<?> arg0, View arg1,
                                                   int arg2, long arg3) {

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


        buttonStartSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String destFloorName = destinationFloor.getFloorName();
                String destRoomCod = destinationRoom.getRoomCod();

                mapExtraInformationDestination = destFloorName.concat(";").concat(destRoomCod);

                /*if (currPosFloor != null)
                {
                    String currPosFloorName = currPosFloor.getFloorName();
                    String currPosRoomCod = currPosRoom.getRoomCod();

                    if (currPosFloorName.equals(destFloorName) && currPosRoomCod.equals(destRoomCod))
                    {
                        mapExtraInformationDestination = "";
                        Toast.makeText(MapSettingActivity.this, "Hai già raggiunto la destinazione", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        mapExtraInformationCurrPos = currPosFloorName.concat(";").concat(currPosRoomCod);
                        startActivityMap();
                    }
                }
                else
                    startActivityMap();*/

                boolean openMap = true;

                if (currPosFloor != null)
                {
                    String currPosFloorName = currPosFloor.getFloorName();
                    String currPosRoomCod = currPosRoom.getRoomCod();

                    if (currPosFloorName.equals(destFloorName) && currPosRoomCod.equals(destRoomCod))
                    {
                        mapExtraInformationDestination = "";
                        openMap = false;
                        Toast.makeText(MapSettingActivity.this, "Hai già raggiunto la destinazione", Toast.LENGTH_LONG).show();
                    }
                    else
                        mapExtraInformationCurrPos = currPosFloorName.concat(";").concat(currPosRoomCod);
                }
                else
                {
                    if (Data.getUserPosition().getFloor().equals(destFloorName) && Data.getUserPosition().getPosition()[0] == destinationRoom.getCoords()[0] && Data.getUserPosition().getPosition()[1] == destinationRoom.getCoords()[1])
                    {
                        openMap = false;
                        Toast.makeText(MapSettingActivity.this, "Hai già raggiunto la destinazione", Toast.LENGTH_LONG).show();
                    }
                }

                if (openMap)
                {
                    if(MainApplication.controlBluetooth())
                        getApplicationContext().sendBroadcast(new Intent("SuspendScan"));
                    else
                        startActivityMap();
                }
            }
        });
    }


    private void startActivityMap()
    {
        Intent intentMap = new Intent (this.getApplicationContext(), MapActivity.class);

        intentMap.putExtra("map_info_dest", mapExtraInformationDestination);

        if (currPosFloor != null)
            intentMap.putExtra("map_info_curr_pos", mapExtraInformationCurrPos);

        this.startActivity(intentMap);
    }

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
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.i("ACTIVTY MAPS","ricevuto broadcast: " + intent.getAction());

            //questo messaggio viene ricevuto quando si deve passare alla FullScreenMaps
            if(intent.getAction().equals(STARTMAPS)) {

                startActivityMap();
            }
        }
    };

}
