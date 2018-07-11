package com.example.user.progetto_ids;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import app_library.MainApplication;
import app_library.comunication.ServerComunication;
import app_library.dijkstra.Dijkstra;
import app_library.maps.grid.TouchImageView;
import app_library.sharedstorage.Data;
import app_library.sharedstorage.DataListener;
import app_library.utility.CSVHandler;

/**
 * Created by User on 26/06/2018.
 */

// activity per la mappa a pieno schermo
public class MapActivity  extends AppCompatActivity implements DataListener {

    // elemento grafico che contenere la mappa con gesture di pitch e zoom
    private TouchImageView touchImageViewMapImage;

    // piano e stanza di destinazione
    private String destinationFloor;
    private String destinationRoom;

    // piano e stanza correnti
    private String userCurrentFloor;
    private String userCurrentRoom;

    // booleano per indicare se abilitare o meno l'elemento del menu per cambiare piano
    private boolean menuItemChangeFloorDisabled;

    //flag per permettere di capire quando ci si trova in uno stato di emergenza e l'app viene messa in background
    private boolean backgroundEmergency;

    // immagini bitmap degli elemnti sulla mappa
    private Bitmap bitmapUserCurrentPosition;
    private Bitmap bitmapDestination;
    private Bitmap bitmapNode;

    // costante per uscire dalla mappa a pieno schermo
    private static final String EXIT_MAPS = "EXIT_MAPS";

    // percorso costituito da un insieme di nodi da seguire sulla mappa
    private String[] nodePathStartEndArray;

    // booleano per indicare il primo recupero di dati della mappa
    private boolean firstRetrive;

    // booleano per indicare che il percorso da seguire è stato completato
    private boolean pathCompleted;

    // extra dell'intent dell'activity precedente a questa
    private Bundle intentExtras;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // impostazione del titolo dell'activity
        getSupportActionBar().setTitle("Mappa");

        //Carico le differenti immagini da visualizzare sulla mappa
        bitmapUserCurrentPosition = BitmapFactory.decodeResource(getResources(), R.drawable.map_user_pos);
        bitmapDestination = BitmapFactory.decodeResource(getResources(), R.drawable.map_dest_pos);
        bitmapNode = BitmapFactory.decodeResource(getResources(), R.drawable.map_node_pos);

        // elementi per il settaggio dello zoom
        touchImageViewMapImage = new TouchImageView(this);
        touchImageViewMapImage.setMaxZoom(4f);

        // recuperato l'extra dell'activity
        intentExtras = getIntent().getExtras();

        // non sono in stato di emergenza (ricerca con bluetooth e ricerca senza bluetooth)
        if(!MainApplication.getEmergency())
        {
            // recupero le informazioni sul piano e stanza di destinazione
            String[] destinationExtraSplit = intentExtras.getString("map_info_dest").split(";");
            destinationFloor = destinationExtraSplit[0];
            destinationRoom = destinationExtraSplit[1];
        }

        // inizialmente l'elemento del menu per il cambio di piano è disabilitato
        menuItemChangeFloorDisabled = true;

        // posso recuperare automaticamente con il bluetooth le informazioni sul piano e la stanza correnti (emergenza o ricerca con bluetooth)
        if (intentExtras.getString("map_info_curr_pos", "").equals(""))
        {
            // recupero automaticamente le informazioni sul piano e stanza corrente
            userCurrentFloor = Data.getUserPosition().getFloor();
            userCurrentRoom = Data.getUserPosition().getRoomCod();

            //Registro la classe all'interno della struttura dati
            //in modo tale viene richiamato il suo metodo retrive al cambio della posizione dell'utente
            if(!Data.getUserPosition().getListeners().contains(this)){
                Data.getUserPosition().addDataListener(this);
            }

            // si inizializza lo scanner dei beacon a emergenza
            if(MainApplication.getEmergency())
                MainApplication.initializeScanner(this,"EMERGENCY");
            // si inizializza lo scanner dei beacon a ricerca
            else
                MainApplication.initializeScanner(this,"SEARCHING");

        }
        // non posso recuperare automaticamente con il bluetooth le informazioni
        else
        {
            // recupero non automaticamente le informazioni sul piano e stanza corrente
            String[] userCurrentExtraSplit = intentExtras.getString("map_info_curr_pos").split(";");
            userCurrentFloor = userCurrentExtraSplit[0];
            userCurrentRoom = userCurrentExtraSplit[1];

            // se il piano corrente è diverso dalla destinazione abilito l'elemento del menu per cambiare piano
            if (!userCurrentFloor.equals(destinationFloor))
                menuItemChangeFloorDisabled = false;
        }

        // non sono in emergenza
        if (!MainApplication.getEmergency())
        {
            // booleano per indicare se il percorso è stato recuperato dal server nella modalità online
            boolean pathOnlineCompleted = false;

            // beacon partenza e destinazione
            String beaconIdStart = MainApplication.getFloors().get(userCurrentFloor).getNodes().get(userCurrentRoom).getBeaconId();
            String beaconIdEnd = MainApplication.getFloors().get(destinationFloor).getNodes().get(destinationRoom).getBeaconId();

            // sono online e recupero il percorso sorgente-destinazione dal server
            if (MainApplication.getOnlineMode())
            {
                // comunicazione con il server ha successo
                if (ServerComunication.handShake(ServerComunication.getIP()))
                {
                    // invio della richiesta per il percorso sorgente-destinazione al server
                    nodePathStartEndArray = ServerComunication.getShortestPathFromSourceToTarget(beaconIdStart, beaconIdEnd);

                    // controllo se il recupero delle informazioni ha avuto successo
                    if (nodePathStartEndArray != null)
                        pathOnlineCompleted = true;
                }
            }

            // se sono offline o il recupero online non è andato a buon fine, recupero il percorso dai dati locali e applico dijkstra
            if (!MainApplication.getOnlineMode() || !pathOnlineCompleted)
                nodePathStartEndArray = Dijkstra.getShortestPathOfflineFromTo(userCurrentRoom, destinationRoom, Dijkstra.getListNodeRoomName(), CSVHandler.readCSV(CSVHandler.FILE_ROUTE, this));
        }

        // controllo per abilitare o disabilitare l'elemento del menu per il cambio di piano
        this.invalidateOptionsMenu();

        // primo recupero dei dati vero
        firstRetrive = true;

        // percorso completato è falso
        pathCompleted = false;
    }

    protected void onStart()
    {
        super.onStart();

        // si imposta l'activity corrente
        MainApplication.setCurrentActivity(this);

        //inizializzato filtro per i messaggi
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(EXIT_MAPS);

        backgroundEmergency = false;

        // recupero dei dati della mappa
        retrive();

        // registrato il receiver nell'activity
        getBaseContext().registerReceiver(broadcastReceiver,intentFilter);
    }

    protected void onResume() {
        super.onResume();

        // activity visibile
        MainApplication.setVisible(true);
    }

    protected void onPause() {
        super.onPause();

        // activity non visibile
        MainApplication.setVisible(false);
    }

    protected void onStop() {
        super.onStop();

        try {

            // si controlla se rimuovere la registrazione del receiver nell'activity
            if(!MainApplication.getEmergency() && broadcastReceiver != null)
                getBaseContext().unregisterReceiver(broadcastReceiver);
            else
                backgroundEmergency = true;

        }
        catch (IllegalArgumentException e)
        {

        }

    }

    public void onDestroy() {
        super.onDestroy();
    }

    // metodo che aggiorna gli elementi sulla mappa come posizione utente, nodi, destinazione e disegno del percorso
    private void setActivityUpdatedMapImage(int identifierIdMap)
    {
        // opzioni bitmap
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inDither = true;
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        // pennello per il disegno del percorso
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(6);

        // bitmap modificabile su cui mettere gli elementi
        Bitmap editableBitmap = Bitmap.createBitmap(BitmapFactory.decodeResource(getResources(), identifierIdMap, options)).copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(editableBitmap);

        // si filtra del percorso da seguire solamente i nodi del piano corrente
        ArrayList<String> listRoomPathCurrentFloor = new ArrayList<>();

        for (int i = 0; i < nodePathStartEndArray.length; i++)
        {
            if (nodePathStartEndArray[i].startsWith(userCurrentFloor))
                listRoomPathCurrentFloor.add(nodePathStartEndArray[i]);
        }

        // bluetooth non attivo o emergenza e condividono il fatto di avere un percorso sorgente-destinazione già stabilito per il piano
        if (!MainApplication.controlBluetooth() || MainApplication.getEmergency())
        {
            // sono recuperate le coordinate x e y della stanza coorente
            int[] currentUserCoords = MainApplication.getFloors().get(userCurrentFloor).getNodes().get(userCurrentRoom).getCoords();

            // disegno dell'immagine della posizione corrente sulla mappa
            canvas.drawBitmap(bitmapUserCurrentPosition, currentUserCoords[0], currentUserCoords[1],null);

            // il numero di nodi del percorso filtrato è superiore a 2
            if (listRoomPathCurrentFloor.size() >= 2)
            {
                // si recupera la x e y del nodo precedente
                int previousX = currentUserCoords[0];
                int previousY = currentUserCoords[1];

                // si scorrono i nodi del percorso per disegnarli sulla mappa e i relativi archi per formare il percorso
                for (int i = 1; i < listRoomPathCurrentFloor.size(); i++)
                {
                    // coordinate x e y nodo
                    int[] currentNodeCoords = MainApplication.getFloors().get(userCurrentFloor).getNodes().get(listRoomPathCurrentFloor.get(i)).getCoords();

                    // se il nodo è l'ultimo del percorso filtrato utilizzo l'immagine della destinazione altrimenti di un nodo
                    if (i == (listRoomPathCurrentFloor.size() - 1))
                        canvas.drawBitmap(bitmapDestination, currentNodeCoords[0], currentNodeCoords[1],null);
                    else
                        canvas.drawBitmap(bitmapNode, currentNodeCoords[0], currentNodeCoords[1],null);

                    // disegno l'arco tra il nodo precedente e attuale
                    canvas.drawLine(previousX, previousY, currentNodeCoords[0], currentNodeCoords[1], paint);

                    previousX = currentNodeCoords[0];
                    previousY = currentNodeCoords[1];
                }
            }

            // imposto la nuova mappa con gli elementi sopra disegnati
            touchImageViewMapImage.setImageBitmap(editableBitmap);

            // non sono sul piano della destinazione e non sono in emergenza ho l'opzione per cambiare piano
            if (!userCurrentFloor.equals(destinationFloor) && !MainApplication.getEmergency())
                Toast.makeText(this, "Raggiunta la destinazione cambia piano con l'apposita voce nel menu", Toast.LENGTH_LONG).show();

            // sono sul piano della destinazione e non sono in emergenza e indico che sono sul piano della destinazione
            if (userCurrentFloor.equals(destinationFloor) && !MainApplication.getEmergency())
                Toast.makeText(this, "Hai raggiunto il piano della destinazione", Toast.LENGTH_LONG).show();

            // se sono in emergenza, ho solo un nodo nel percorso, piano coorente e destinazione coincidono e non ho ancora completato il percorso allora mostro il messaggio che sono arrivato alla posizione sicura
            if (MainApplication.getEmergency() && listRoomPathCurrentFloor.size() == 1 && userCurrentFloor.equals(destinationFloor) && !pathCompleted)
            {
                pathCompleted = true;
                Toast.makeText(this, "Hai raggiunto la posizione sicura", Toast.LENGTH_LONG).show();
            }
        }
        // bluetooth attivo e si visualizza il percorso rimanente per il piano
        else
        {
            // si filtra ulteriormente dal percorso per il piano corrente solamente i nodi che rimangono da percorrere
            ArrayList<String> listRemainingRoomPathCurrentFloor = new ArrayList<>();

            // booleano che indica la stanza attuale in cui si trova è stata trovata
            boolean currentRoomFound = false;

            for (int i = 0; i < listRoomPathCurrentFloor.size(); i++)
            {
                // la stanza non è stata ancora trovata
                if (!currentRoomFound)
                {
                    // la stanza della lista coincide con quella della posizione corrente allora da ora prendo tutti i nodi rimanenti impostando il relativo booleano
                    if (listRoomPathCurrentFloor.get(i).equals(userCurrentFloor))
                    {
                        currentRoomFound = true;
                        listRemainingRoomPathCurrentFloor.add(listRoomPathCurrentFloor.get(i));
                    }
                }
                // aggiungo tutti i nodi
                else
                    listRemainingRoomPathCurrentFloor.add(listRoomPathCurrentFloor.get(i));
            }

            // sono recuperate le coordinate x e y della stanza coorente
            int[] currentUserCoords = MainApplication.getFloors().get(userCurrentFloor).getNodes().get(userCurrentRoom).getCoords();

            // disegno dell'immagine della posizione corrente sulla mappa
            canvas.drawBitmap(bitmapUserCurrentPosition, currentUserCoords[0], currentUserCoords[1],null);

            // il numero di nodi del percorso filtrato due volte è superiore a 2
            if (listRemainingRoomPathCurrentFloor.size() >= 2)
            {
                // coordinate x e y nodo
                int previousX = currentUserCoords[0];
                int previousY = currentUserCoords[1];

                // si scorrono i nodi del percorso per disegnarli sulla mappa e i relativi archi per formare il percorso
                for (int i = 1; i < listRemainingRoomPathCurrentFloor.size(); i++)
                {
                    // coordinate x e y nodo
                    int[] currentNodeCoords = MainApplication.getFloors().get(userCurrentFloor).getNodes().get(listRemainingRoomPathCurrentFloor.get(i)).getCoords();

                    // se il nodo è l'ultimo del percorso filtrato utilizzo l'immagine della destinazione altrimenti di un nodo
                    if (i == (listRemainingRoomPathCurrentFloor.size() - 1))
                        canvas.drawBitmap(bitmapDestination, currentNodeCoords[0], currentNodeCoords[1],null);
                    else
                        canvas.drawBitmap(bitmapNode, currentNodeCoords[0], currentNodeCoords[1],null);

                    // disegno l'arco tra il nodo precedente e attuale
                    canvas.drawLine(previousX, previousY, currentNodeCoords[0], currentNodeCoords[1], paint);

                    previousX = currentNodeCoords[0];
                    previousY = currentNodeCoords[1];
                }
            }

            // imposto la nuova mappa con gli elementi sopra disegnati
            touchImageViewMapImage.setImageBitmap(editableBitmap);

            // se ho solo un nodo nel percorso, piano coorente e destinazione coincidono e non ho ancora completato il percorso allora mostro il messaggio che sono arrivato alla destinazione
            if (listRemainingRoomPathCurrentFloor.size() == 1 && userCurrentFloor.equals(destinationFloor) && !pathCompleted)
            {
                pathCompleted = true;
                Toast.makeText(this, "Hai raggiunto la destinazione", Toast.LENGTH_LONG).show();
            }
        }
    }

    // metodo update del listener
    @Override
    public void update() {

    }

    // metodo retrive del listener richiamato ad ogni aggiornamento della posizione dell'utente in Data
    @Override
    public void retrive()
    {
        // posso recuperare automaticamente con il bluetooth le informazioni sul piano e la stanza correnti (emergenza o ricerca con bluetooth)
        if (intentExtras.getString("map_info_curr_pos", "").equals(""))
        {
            // non è il primo recupero delle informazioni e acquisico le nuove informazioni sulla posizione corrente
            if (!firstRetrive)
            {
                userCurrentFloor = Data.getUserPosition().getFloor();
                userCurrentRoom = Data.getUserPosition().getRoomCod();
            }
            // è il primo recupero delle informazioni
            else
                firstRetrive = false;

            // sono in emergenza
            if (MainApplication.getEmergency())
            {
                // beacon corrente in cui mi trovo
                String currentUserBeacon = MainApplication.getFloors().get(userCurrentFloor).getNodes().get(userCurrentRoom).getBeaconId();

                // invio della richiesta per il perocrso in situazione di emergenza dal beacon corrente al server
                nodePathStartEndArray = ServerComunication.getShortestPathToSafePlace(currentUserBeacon);

                // la destinazione è l'ultimo elemento dell'array
                destinationRoom = nodePathStartEndArray[nodePathStartEndArray.length - 1];

                Iterator iterator = MainApplication.getFloors().entrySet().iterator();
                boolean roomFound = false;

                // si va a recuperare in base alla stanza di destinazione il piano di destinazione
                while (iterator.hasNext() && !roomFound)
                {
                    Map.Entry pair = (Map.Entry) iterator.next();

                    roomFound = MainApplication.getFloors().get(pair.getKey().toString()).getListNameRoomOrBeacon(true).contains(destinationRoom);

                    if (roomFound)
                        destinationFloor = MainApplication.getFloors().get(pair.getKey().toString()).getFloorName();
                }
            }
        }

        // identificativo della risorsa drawable immagine bitmap riferita al piano corrente
        final int mapResId = getResources().getIdentifier("m".concat(userCurrentFloor).concat("_color") , "drawable", getPackageName());

        // avvio un thread per non bloccare quello principale durante l'aggiornamento della mappa
        runOnUiThread(new Runnable() {

            @Override
            public void run() {

                // aggiorno gli elementi sulla mappa come posizione utente, nodi, destinazione e disegno del percorso
                setActivityUpdatedMapImage(mapResId);

                // imposto l'immagine della mappa come layout dell'activity
                setContentView(touchImageViewMapImage);
            }
        });
    }

    // creazione del menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_map, menu);
        return true;
    }

    // eventi attivati al click sugli elementi del menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            // l'elemento premuto è quello per mostrare la legenda quindi viene aperta la dialog della legenda
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

            // l'elemento premuto è per cambiare piano
            case R.id.menu_map_change_floor:

                // si recupera il valore intero del piano corrente
                int valueUserCurrentFloor = Integer.parseInt(userCurrentFloor);

                // se il piano corrente è inferiore a quello di destinazione si incrementa il piano corrente di 5
                if (valueUserCurrentFloor < Integer.parseInt(destinationFloor))
                    userCurrentFloor = "" + (valueUserCurrentFloor + 5);
                // se il piano corrente è maggiore di quello di destinazione si decrementa il piano corrente di 5
                else
                    userCurrentFloor = "" + (valueUserCurrentFloor - 5);

                // se il piano corrente e quello di destinazione coincidono disabilito l'elemento del menu per cambiare piano
                if (userCurrentFloor.equals(destinationFloor))
                {
                    menuItemChangeFloorDisabled = true;
                    this.invalidateOptionsMenu();
                }

                // booleano per indicare se è stata trovata la nuova stanza nel nuovo piano
                boolean newRoomFloorFound = false;

                // si scorre l'array del percorso da seguire fino a trovare il primo elemento riferito al nuovo piano e quello rappresenta la nuova stanza
                for (int i = 0; i < nodePathStartEndArray.length && !newRoomFloorFound; i++)
                {
                    if (nodePathStartEndArray[i].startsWith(userCurrentFloor))
                    {
                        userCurrentRoom = nodePathStartEndArray[i];
                        newRoomFloorFound = true;
                    }
                }

                // recupero dei dati della mappa
                retrive();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // metodo che permette di disabilitare l'elemento del menu per cambiare piano in base al valore del relativo booleano
    @Override
    public boolean onPrepareOptionsMenu (Menu menu)
    {
        MenuItem item = menu.findItem(R.id.menu_map_change_floor);

        // elemento da disabilitare
        if (menuItemChangeFloorDisabled)
        {
            item.setEnabled(false);
            item.setVisible(false);
        }

        return true;
    }

    // evento attivato alla pressione del tasto back
    @Override
    public void onBackPressed()
    {
        // se sono in emergenza e chiudo la mappa la disabilito
        if (MainApplication.getEmergency())
            MainApplication.setEmergency(false);

        // se posso recuperare automaticamente con il bluetooth le informazioni sul piano e la stanza correnti allora disabilito lo scan
        if (MainApplication.controlBluetooth() && intentExtras.getString("map_info_curr_pos", "").equals(""))
            MainApplication.getScanner().suspendScan();

        try {
            // si rimuove la registrazione del receiver nell'activity
            if (broadcastReceiver != null)
                getBaseContext().unregisterReceiver(broadcastReceiver);
        }
        catch (IllegalArgumentException e)
        {

        }

        finish();
    }

    //il broadcast receiver deputato alla ricezione dei messaggi
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.i("ACTIVTY MAPS","ricevuto broadcast: " + intent.getAction());

            // uscita dalla mappa
            if(intent.getAction().equals(EXIT_MAPS))
            {

                //nel caso in cui l'app sia stata messa in background durante l'emergenza
                //il broadcastreceiver non è stato cancellata, quindi cancellato ora
                if(backgroundEmergency == true)
                {
                    try {
                        getBaseContext().unregisterReceiver(broadcastReceiver);
                    }
                    catch (IllegalArgumentException e)
                    {

                    }
                }

                finish();
            }
        }
    };
}
