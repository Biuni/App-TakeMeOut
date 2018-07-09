package com.example.user.progetto_ids;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import app_library.MainApplication;
import app_library.comunication.ServerComunication;
import app_library.dijkstra.Dijkstra;
import app_library.maps.components.Floor;
import app_library.maps.grid.TouchImageView;
import app_library.sharedstorage.Data;
import app_library.sharedstorage.DataListener;
import app_library.utility.CSVHandler;

/**
 * Created by User on 26/06/2018.
 */

public class MapActivity  extends AppCompatActivity implements DataListener {

    private TouchImageView touchImageViewMapImage;

    private String destinationFloor;
    private String destinationRoom;

    private String userCurrentFloor;
    private String userCurrentRoom;

    private boolean menuItemChangeFloorDisabled;

    //flag per permettere di capire quando ci si trova in uno stato di emergenza e l'app viene messa in background
    private boolean backgroundEmergency;

    private Bitmap bitmapUserCurrentPosition;
    private Bitmap bitmapDestination;
    private Bitmap bitmapNode;

    private static final String EXIT_MAPS = "EXIT_MAPS";

    private String[] nodePathStartEndArray;
    private boolean firstRetrive;
    private boolean emergencyCompleted;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setTitle("Mappa");

        //Carico le differenti immagini da visualizzare sulla mappa
        bitmapUserCurrentPosition = BitmapFactory.decodeResource(getResources(), R.drawable.map_user_pos);
        bitmapDestination = BitmapFactory.decodeResource(getResources(), R.drawable.map_dest_pos);
        bitmapNode = BitmapFactory.decodeResource(getResources(), R.drawable.map_node_pos);

        touchImageViewMapImage = new TouchImageView(this);
        touchImageViewMapImage.setMaxZoom(4f);

        Bundle intentExtras = getIntent().getExtras();

        if(!MainApplication.getEmergency())
        {
            String[] destinationExtraSplit = intentExtras.getString("map_info_dest").split(";");
            destinationFloor = destinationExtraSplit[0];
            destinationRoom = destinationExtraSplit[1];
        }

        menuItemChangeFloorDisabled = true;

        if (MainApplication.controlBluetooth())
        {
            if (!intentExtras.getString("map_info_curr_pos", "").equals(""))
            {
                String[] userCurrentExtraSplit = intentExtras.getString("map_info_curr_pos").split(";");
                userCurrentFloor = userCurrentExtraSplit[0];
                userCurrentRoom = userCurrentExtraSplit[1];
            }
            else
            {
                userCurrentFloor = Data.getUserPosition().getFloor();
                userCurrentRoom = Data.getUserPosition().getRoomCod();
            }

            //Registro la classe all'interno della struttura dati
            //in modo tale viene richiamato il suo metodo retrive al cambio della posizione dell'utente
            if(!Data.getUserPosition().getListeners().contains(this)){
                Data.getUserPosition().addDataListener(this);
            }

            if(MainApplication.getEmergency())
                MainApplication.initializeScanner(this,"EMERGENCY");
            else
                MainApplication.initializeScanner(this,"SEARCHING");

        }
        // assenza di bluetooth
        else
        {
            String[] userCurrentExtraSplit = intentExtras.getString("map_info_curr_pos").split(";");
            userCurrentFloor = userCurrentExtraSplit[0];

            //String userCurrentRoom = userCurrentExtraSplit[1];
            userCurrentRoom = userCurrentExtraSplit[1];
            //userCurrentPosition = MainApplication.getFloors().get(userCurrentFloor).getNodes().get(userCurrentRoom).getCoords();


            /*boolean pathOnlineCompleted = false;
            String beaconIdStart = MainApplication.getFloors().get(userCurrentFloor).getNodes().get(userCurrentRoom).getBeaconId();
            String beaconIdEnd = MainApplication.getFloors().get(destinationFloor).getNodes().get(destinationRoom).getBeaconId();

            if (MainApplication.getOnlineMode())
            {
                if (ServerComunication.handShake(ServerComunication.getIP()))
                {
                    nodePathStartEndArray = ServerComunication.getShortestPathFromSourceToTarget(beaconIdStart, beaconIdEnd);

                    if (nodePathStartEndArray != null)
                        pathOnlineCompleted = true;
                }
            }

            if (!pathOnlineCompleted || !MainApplication.getOnlineMode())
                nodePathStartEndArray = Dijkstra.getShortestPathOfflineFromTo(userCurrentRoom, destinationRoom, Dijkstra.getHashMapsNodeIndex(), CSVHandler.readCSV(CSVHandler.FILE_ROUTE, this));

            if (!userCurrentFloor.equals(destinationFloor))
                menuItemChangeFloorDisabled = false;

            retrive();*/

            if (!userCurrentFloor.equals(destinationFloor))
                menuItemChangeFloorDisabled = false;
        }

        if (!MainApplication.getEmergency())
        {
            boolean pathOnlineCompleted = false;
            String beaconIdStart = MainApplication.getFloors().get(userCurrentFloor).getNodes().get(userCurrentRoom).getBeaconId();
            String beaconIdEnd = MainApplication.getFloors().get(destinationFloor).getNodes().get(destinationRoom).getBeaconId();

            if (MainApplication.getOnlineMode())
            {
                if (ServerComunication.handShake(ServerComunication.getIP()))
                {
                    nodePathStartEndArray = ServerComunication.getShortestPathFromSourceToTarget(beaconIdStart, beaconIdEnd);

                    if (nodePathStartEndArray != null)
                        pathOnlineCompleted = true;
                }
            }

            if (!MainApplication.getOnlineMode() || !pathOnlineCompleted)
                nodePathStartEndArray = Dijkstra.getShortestPathOfflineFromTo(userCurrentRoom, destinationRoom, Dijkstra.getHashMapsNodeIndex(), CSVHandler.readCSV(CSVHandler.FILE_ROUTE, this));
        }

        this.invalidateOptionsMenu();

        firstRetrive = true;
        emergencyCompleted = false;

        //retrive();




        /*String[] destinationExtraSplit = intentExtras.getString("map_info_dest").split(";");
        destinationFloor = destinationExtraSplit[0];
        destinationRoom = destinationExtraSplit[1];

        automaticUserPosition = intentExtras.getString("map_info_curr_pos").equals("");

        userCurrentPosition = new int[]{30,30};

        menuItemChangeFloorDisabled = true;

        if (!automaticUserPosition)
        {
            String[] userCurrentExtraSplit = intentExtras.getString("map_info_curr_pos").split(";");
            userCurrentFloor = userCurrentExtraSplit[0];

            String userCurrentRoom = userCurrentExtraSplit[1];
            userCurrentPosition = MainApplication.getFloors().get(userCurrentFloor).getNodes().get(userCurrentRoom).getCoords();

            if (!userCurrentFloor.equals(destinationFloor))
                menuItemChangeFloorDisabled = false;
        }

        this.invalidateOptionsMenu();

        touchImageViewMapImage = new TouchImageView(this);
        touchImageViewMapImage.setMaxZoom(4f);

        retrive();*/










    }

    protected void onStart()
    {
        super.onStart();
        MainApplication.setCurrentActivity(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(EXIT_MAPS);

        backgroundEmergency = false;

        retrive();

        getBaseContext().registerReceiver(broadcastReceiver,intentFilter);
    }

    protected void onResume() {
        super.onResume();
        MainApplication.setVisible(true);
    }

    protected void onPause() {
        super.onPause();
        MainApplication.setVisible(false);
    }

    protected void onStop() {
        super.onStop();

        if(!MainApplication.getEmergency() && broadcastReceiver != null)
            getBaseContext().unregisterReceiver(broadcastReceiver);
        else
            backgroundEmergency = true;
    }

    public void onDestroy() {
        super.onDestroy();
    }

    private void setActivityUpdatedMapImage(int identifierIdMap)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = true;
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        //options.inPurgeable = true;

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(8);

        Bitmap editableBitmap = Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), identifierIdMap, options)).copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(editableBitmap);


        ArrayList<String> listRoomPathCurrentFloor = new ArrayList<>();

        for (int i = 0; i < nodePathStartEndArray.length; i++)
        {
            if (nodePathStartEndArray[i].startsWith(userCurrentFloor))
                listRoomPathCurrentFloor.add(nodePathStartEndArray[i]);
        }

        // bluetooth non attivo o emergenza e condividono il fatto di avere un percorso sorgente-destinazione già stabilito per il piano
        if (!MainApplication.controlBluetooth() || MainApplication.getEmergency())
        {
            /*canvas.drawBitmap(bitmapUserCurrentPosition, userCurrentPosition[0], userCurrentPosition[1],null);
            touchImageViewMapImage.setImageBitmap(editableBitmap);

            if (userCurrentFloor.equals(destinationFloor))
            {
                userCurrentDestinationPosition = MainApplication.getFloors().get(destinationFloor).getNodes().get(destinationRoom).getCoords();
                canvas.drawBitmap(bitmapDestination, userCurrentDestinationPosition[0], userCurrentDestinationPosition[1],null);
            }
            else
            {
                String exitFloor = userCurrentFloor.concat("A3");
                userCurrentDestinationPosition = MainApplication.getFloors().get(userCurrentFloor).getNodes().get(exitFloor).getCoords();
                canvas.drawBitmap(bitmapDestination, userCurrentDestinationPosition[0], userCurrentDestinationPosition[1],null);
                Toast.makeText(this, "Raggiunta la destinazione cambia piano con l'apposita voce nel menu", Toast.LENGTH_LONG).show();
            }

            canvas.drawLine(userCurrentPosition[0], userCurrentPosition[1], userCurrentDestinationPosition[0], userCurrentDestinationPosition[1], paint);*/


            int[] currentUserCoords = MainApplication.getFloors().get(userCurrentFloor).getNodes().get(userCurrentRoom).getCoords();
            canvas.drawBitmap(bitmapUserCurrentPosition, currentUserCoords[0], currentUserCoords[1],null);

            if (listRoomPathCurrentFloor.size() >= 2)
            {
                int previousX = currentUserCoords[0];
                int previousY = currentUserCoords[1];

                for (int i = 1; i < listRoomPathCurrentFloor.size(); i++)
                {
                    int[] currentNodeCoords = MainApplication.getFloors().get(userCurrentFloor).getNodes().get(listRoomPathCurrentFloor.get(i)).getCoords();

                    if (i == (listRoomPathCurrentFloor.size() - 1))
                        canvas.drawBitmap(bitmapDestination, currentNodeCoords[0], currentNodeCoords[1],null);
                    else
                        canvas.drawBitmap(bitmapNode, currentNodeCoords[0], currentNodeCoords[1],null);

                    canvas.drawLine(previousX, previousY, currentNodeCoords[0], currentNodeCoords[1], paint);

                    previousX = currentNodeCoords[0];
                    previousY = currentNodeCoords[1];
                }
            }

            touchImageViewMapImage.setImageBitmap(editableBitmap);

            if (!userCurrentFloor.equals(destinationFloor) && !MainApplication.getEmergency())
                Toast.makeText(this, "Raggiunta la destinazione cambia piano con l'apposita voce nel menu", Toast.LENGTH_LONG).show();

            if (MainApplication.getEmergency() && listRoomPathCurrentFloor.size() == 1 && userCurrentFloor.equals(destinationFloor) && !emergencyCompleted)
            {
                emergencyCompleted = true;
                Toast.makeText(this, "Hai raggiunto la posizione sicura", Toast.LENGTH_LONG).show();
            }
        }
        // bluetooth attivo e si visualizza il percorso rimanente per il piano
        else
        {
            ArrayList<String> listRemainingRoomPathCurrentFloor = new ArrayList<>();
            boolean currentRoomFound = false;

            for (int i = 0; i < listRoomPathCurrentFloor.size(); i++)
            {
                if (!currentRoomFound)
                {
                    if (listRoomPathCurrentFloor.get(i).equals(userCurrentFloor))
                    {
                        currentRoomFound = true;
                        listRemainingRoomPathCurrentFloor.add(listRoomPathCurrentFloor.get(i));
                    }
                }
                else
                    listRemainingRoomPathCurrentFloor.add(listRoomPathCurrentFloor.get(i));
            }

            int[] currentUserCoords = MainApplication.getFloors().get(userCurrentFloor).getNodes().get(userCurrentRoom).getCoords();
            canvas.drawBitmap(bitmapUserCurrentPosition, currentUserCoords[0], currentUserCoords[1],null);

            if (listRemainingRoomPathCurrentFloor.size() >= 2)
            {
                int previousX = currentUserCoords[0];
                int previousY = currentUserCoords[1];

                for (int i = 1; i < listRemainingRoomPathCurrentFloor.size(); i++)
                {
                    int[] currentNodeCoords = MainApplication.getFloors().get(userCurrentFloor).getNodes().get(listRemainingRoomPathCurrentFloor.get(i)).getCoords();

                    if (i == (listRemainingRoomPathCurrentFloor.size() - 1))
                        canvas.drawBitmap(bitmapDestination, currentNodeCoords[0], currentNodeCoords[1],null);
                    else
                        canvas.drawBitmap(bitmapNode, currentNodeCoords[0], currentNodeCoords[1],null);

                    canvas.drawLine(previousX, previousY, currentNodeCoords[0], currentNodeCoords[1], paint);

                    previousX = currentNodeCoords[0];
                    previousY = currentNodeCoords[1];
                }
            }

            touchImageViewMapImage.setImageBitmap(editableBitmap);
        }
    }

    @Override
    public void update() {

    }

    @Override
    public void retrive()
    {
        if (MainApplication.controlBluetooth())
        {
            if (!firstRetrive)
            {
                userCurrentFloor = Data.getUserPosition().getFloor();
                userCurrentRoom = Data.getUserPosition().getRoomCod();
            }
            else
                firstRetrive = false;

            if (MainApplication.getEmergency())
            {
                String currentUserBeacon = MainApplication.getFloors().get(userCurrentFloor).getNodes().get(userCurrentRoom).getBeaconId();
                nodePathStartEndArray = ServerComunication.getShortestPathToSafePlace(currentUserBeacon);

                destinationRoom = nodePathStartEndArray[nodePathStartEndArray.length - 1];

                Iterator iterator = MainApplication.getFloors().entrySet().iterator();
                boolean roomFound = false;

                while (iterator.hasNext() && !roomFound)
                {
                    Map.Entry pair = (Map.Entry) iterator.next();

                    roomFound = MainApplication.getFloors().get(pair.getKey().toString()).getListNameRoomOrBeacon(true).contains(destinationRoom);

                    if (roomFound)
                        destinationFloor = MainApplication.getFloors().get(pair.getKey().toString()).getFloorName();
                }
            }
        }

        final int mapResId = getResources().getIdentifier("m".concat(userCurrentFloor).concat("_color") , "drawable", getPackageName());

        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                setActivityUpdatedMapImage(mapResId);
                setContentView(touchImageViewMapImage);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_map_legend:

                final Dialog legendDialog = new Dialog(this);
                legendDialog.setContentView(R.layout.legend);
                //legendDialog.setTitle("Legenda");

                Button buttonCancel = (Button) legendDialog.findViewById(R.id.buttonLegendClose);

                buttonCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        legendDialog.dismiss();
                    }
                });

                legendDialog.show();

                return true;

            case R.id.menu_map_change_floor:

                int valueUserCurrentFloor = Integer.parseInt(userCurrentFloor);

                if (valueUserCurrentFloor < Integer.parseInt(destinationFloor))
                    userCurrentFloor = "" + (valueUserCurrentFloor + 5);
                else
                    userCurrentFloor = "" + (valueUserCurrentFloor - 5);

                if (userCurrentFloor.equals(destinationFloor))
                {
                    menuItemChangeFloorDisabled = true;
                    this.invalidateOptionsMenu();
                }

                boolean newRoomFloorFound = false;

                for (int i = 0; i < nodePathStartEndArray.length && !newRoomFloorFound; i++)
                {
                    if (nodePathStartEndArray[i].startsWith(userCurrentFloor))
                    {
                        userCurrentRoom = nodePathStartEndArray[i];
                        newRoomFloorFound = true;
                    }
                }

                retrive();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu (Menu menu)
    {
        MenuItem item = menu.findItem(R.id.menu_map_change_floor);

        if (menuItemChangeFloorDisabled)
        {
            item.setEnabled(false);
            item.setVisible(false);
        }

        return true;
    }

    @Override
    public void onBackPressed()
    {
        if (MainApplication.controlBluetooth())
            MainApplication.getScanner().suspendScan();

        if (broadcastReceiver != null)
            getBaseContext().unregisterReceiver(broadcastReceiver);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.i("ACTIVTY MAPS","ricevuto broadcast: " + intent.getAction());

            if(intent.getAction().equals(EXIT_MAPS))
            {

                //nel caso in cui l'app sia stata messa in background durante l'emergenza
                //il broadcastreceiver non è stato cancellata, quindi cancellato ora
                if(backgroundEmergency == true)
                {
                    getBaseContext().unregisterReceiver(broadcastReceiver);
                }

                finish();
            }
        }
    };
}
