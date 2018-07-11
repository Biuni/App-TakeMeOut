package app_library.beacon;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import app_library.MainApplication;
import app_library.comunication.ServerComunication;
import app_library.maps.components.Floor;
import app_library.maps.components.Node;
import app_library.sharedstorage.Data;
import app_library.sharedstorage.DataListener;
import app_library.user.UserHandler;
import app_library.utility.StateMachine;


/**
    Classe utilizzata per gestire lo scan dei beacon in prossimità dell'utente
 */

public class BeaconScanner extends StateMachine implements DataListener {

        //contiene le caratteristiche legate allo scan, in riferimento alla configurazione settata
    private Setup setup;

        //alcuni possibili messaggi che può ricevere lo scan (vengono utilizzati come parametri per l'intenFilter)
    public static final String SCAN_PHASE_FINISHED = "ScanPhaseFinished";
    public static final String SUSPEND_SCAN = "SuspendScan";
    public static final String EMERGENCY = "EMERGENCY";

    private static final String TAG = "BeaconRESPONSE";

        //adapter per interpretare ciò che viene trovato nello scan
    private BScanLeDeviceListAdapter mLeDeviceListAdapter;
        //activity in cui viene fatto partire lo scan, in modo da poterne ricavare il context
    private Activity activity;

        //rappresenta il beacon più vicino, con cui si deve effettuare il collegamento
    private BluetoothDevice currentBeacon;
        //rappresenta il beacon trovato dallo scan
    private BluetoothDevice selectedBeacon;

        //uuid dei sensortag utilizati
    private static final String beaconUUID = "0000aa80-0000-1000-8000-00805f9b34fb";

        //maschera di UUID, serve per filtrare i dispositivi bluetooth da analizzare
    private UUID[] uuids;
        //handler utilizzato per lanciare le varie Runnable (start,stop,wait)
    private Handler scanHandler;
        //filtro dei messaggi per il broadcast receiver
    private IntentFilter intentFilter;
        //connessione con un determinato sensortag, per leggere i dati dai sensori
    private BeaconConnection connection;

        //elementi necessari per efettuare lo scan
    private ScanFilter scanFilter;
    private ScanSettings scanSettings;
    private List<ScanFilter> scanFilters;
        //numero massimo di scan senza che venga mandata la posizione al server
    private static final int maxNoUpdate = 5;
        //conta quante volte consecutive non si invia la propria posizione al server
    private int cont;

    /**
     * Costruisce l'oggetto per lo scan (con il setup di default)
     * @param a, activity in cui viene creato lo scan
     */
    public BeaconScanner(Activity a) {
        super();

        activity = a;
            //inizializza il contenitore
        setup = new Setup();
        //mette in funzione la state machine
        running = true;
        //inizializzati i componenti del bluetooth
        mLeDeviceListAdapter = new BScanLeDeviceListAdapter();
//        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

//        if(!controlBluetooth()) activateBluetooth(a);

        cont = 0;

            //insieme di UUID riconosciuti dallo scan e relativa inizializzazione
        uuids = new UUID[1];
        uuids[0] = UUID.fromString(beaconUUID);
            //inizializzati gli elementi per lo scan
        scanFilter = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(UUID.fromString(beaconUUID))).build();

        scanFilters = new ArrayList<>();
        scanFilters.add(scanFilter);
        scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();

            //inizializzazione del filtro per i messaggi e registrazione del broadcast receiver
        initializeFilter();
            //viene registrato il receiver, in modo che possa ricevere messaggi e possa leggere
            //quelli la cui intestazione si trova nell'intentFilter
        activity.getBaseContext().registerReceiver(broadcastReceiver,intentFilter);
            //viene inizializzato l'handler
        scanHandler = new Handler();
            //viene eseguito lo scan corrente
        executeState();
    }

    /**
     * Costruisce l'oggetto per lo scan
     * @param a, activity in cui viene creato lo scan
     * @param set, indica l'identificativo del Setup con cui viene costruito lo scan
     */
    public BeaconScanner(Activity a, String set) {
        super();
        Data.getUserPosition().addDataListener(this);
        activity = a;
        //inizializza il contenitore
        setup = new Setup(set);
        //mette in funzione la state machine
        running = true;
        //inizializzati i componenti del bluetooth
        mLeDeviceListAdapter = new BScanLeDeviceListAdapter();
//        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //viene controllato che il bluetooth sia accesso, nel caso in cui ciò non sia vero
        //viene mostrata l'opzione per attivarlo
//        if(!controlBluetooth()) activateBluetooth(a);

        cont = 0;

        //insieme di UUID riconosciuti dallo scan e relativa inizializzazione
        uuids = new UUID[1];
        uuids[0] = UUID.fromString(beaconUUID);

        scanFilter = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(UUID.fromString(beaconUUID))).build();

        scanFilters = new ArrayList<>();
        scanFilters.add(scanFilter);
        scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();

        //inizializzazione del filtro per i messaggi e registrazione del broadcast receiver
        initializeFilter();
        //viene registrato il receiver, in modo che possa ricevere messaggi e possa leggere
        //quelli la cui intestazione si trova nell'intentFilter
        activity.getBaseContext().registerReceiver(broadcastReceiver,intentFilter);
        //viene inizializzato l'handler
        scanHandler = new Handler();
        //viene eseguito lo scan corrente
        executeState();
    }

    /**
     * Metodo per costruire il filtro per i messaggi che può ricevere il broadcastReceiver
     */
    //inseriti i filtri per i messaggi ricevuti
    private void initializeFilter() {
        intentFilter = new IntentFilter();
        intentFilter.addAction(SCAN_PHASE_FINISHED);
        intentFilter.addAction(SUSPEND_SCAN);
        intentFilter.addAction(EMERGENCY);
    }

    /**
     * Metodo per gestire la ricezione dei messaggi
     * @param intent, indica il messaggio ricevuto
     */
    private void messageHandler(Intent intent) {
        switch (intent.getAction()) {
                //ricevuto quando è terminata la connessione al beacon
            case (SCAN_PHASE_FINISHED):
                connection = null;
                    //finita l'esecuzione dello stato richiama
                int next = nextState();
                changeState(next);
                executeState();
                break;
                //ricevuto quando deve essere sospeso lo scan
            case (SUSPEND_SCAN):
                suspendScan();
                break;
            default:
                break;
        }
    }
    /**
     * Metodo che gestisce le istruzioni relative alla sospesione dello scan, diverse in base allo stato in cui si trova
     * la macchina a stati
     */

    public void suspendScan() {
        Log.i("SUSPENDSCAN","suspend scan");
        running = false;

            //nel caso in cui si stiano leggendo i dati dai sensori, viene interrotta la procedura
            //fermando la macchina a stati che se ne occupa
        if(currentState==1) {
            activity.getBaseContext().sendBroadcast(new Intent(BeaconConnection.STOP_CONNECTION));
        }
            //se si sta aspettando per un nuovo scan, viene abortito il processo di attesa
            //e si passa allo stato successivo (gestione della chiusura dello scan)
        else if(currentState==2) {
            scanHandler.removeCallbacks(wait);
            int next = nextState();
            changeState(next);
            executeState();
        }
    }

        //il broadcast receiver deputato alla ricezione dei messaggi
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG,"ricevuto broadcast: " + intent.getAction());
            messageHandler(intent);
        }
    };

    /**
     * Metodo che restituisce il beacon più vicino, dall'ultimo scan
     * @return beacon più vicino rispetto all'utente
     */
    public BluetoothDevice getSelectedBeacon() {
        return selectedBeacon;
    }

    /**
     * Metodo che restituisce l'ultimo beacon rilevato
     * @return ultimo beacon con cui ci si è collegati
     */
    public BluetoothDevice getCurrentBeacon() {
        return currentBeacon;
    }

    /**
     * Metodo che restituisce il setup con cui sta lavorando lo scan
     * @return setup con cui sta lavorando lo scan
     */

    public BeaconConnection getConnection() { return connection; }

    public Setup getSetup() {
        return setup;
    }

    /**
     * Metodo per iniziare lo scan che ricerca i sensortag presenti nel raggio d'azione dell'utente
     */

    private void discoverBLEDevices() {
            //parte il thread deputato allo scan dei bluetooth LE
        startScan.run();

        Log.e("BLE_Scanner", "DiscoverBLE, in " + setup.getState() + " condition");
    }


    // metodo per passare allo stato successivo della macchina a stati
    protected int nextState() {
        int next;
        switch(currentState) {
            case(0):
                if (!running) next = 3;
                else if (running && mLeDeviceListAdapter.getCurrentBeacon()!=null && setup.mustAnalyze()) next = 1;
                else next = 2;
                break;
            case(1):
                if (!running) next = 3;
                else next = 2;
                break;
            case(2):
                if (!running) next = 3;
                else next = 0;
                break;
            default:
                next = 3;
                break;
        }
        return next;
    }


    /**
     * Metodo per attendere il tempo necessario fra due scan consecutivi
     * @return beacon più vicino rispetto all'utente
     */
    private void waiting() {
        Log.i("WAITING","waiting for " + setup.getPeriodBetweenScan()/1000 + " seconds for new Scan ");
        scanHandler.postDelayed(wait,setup.getPeriodBetweenScan());
    }


    // metodo per l'esecuzione di uno stato
    protected void executeState() {
        Log.i("State","execute scan state " + getState());
        switch(currentState) {
                //in questo stato viene effettuato lo scan dei beacon
            case(0):
                discoverBLEDevices();
                break;
                //in questo stato viene effettuata la connessione al beacon
            case(1):
                connection = new BeaconConnection(activity, currentBeacon);
                connection.start();
                break;
                //in questo stato si attende un certo periodo prima di effettuare un altro scan
            case (2):
                if (currentBeacon==null) closeConnection();
                waiting();
                break;
                //in questo stato si gestisce lo spegnimento della macchina a stati
            case (3):
                activity.getBaseContext().sendBroadcast(new Intent("TerminatedScan"));
                break;
        }
    }

    /**
     * Metodo per chiudere la connessione, una volta terminata
     */

    public void closeConnection() {
        connection = null;
    }

        //thread che si occupa di far partire lo scan in cerca dei beacon
    private Runnable startScan = new Runnable() {
        @Override
        public void run() {
                //cancella la lista di sensortag precedentemente trovati
            mLeDeviceListAdapter.clear();
            mLeDeviceListAdapter.setCurrentBeacon(null);

            Log.e(TAG, "Start Scan");
                //parte effettivamente la ricerca dei sensortag
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (MainApplication.getmBluetoothAdapter() != null) {
                    try {
                        MainApplication.getmBluetoothAdapter().getBluetoothLeScanner()
                                .startScan(scanFilters, scanSettings, mScanCallback);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        Log.e("bluetooth error","accendi il bluetooth");
                    }

                }
            }
            else {
                    //parte effettivamente la ricerca dei sensortag
                MainApplication.getmBluetoothAdapter().startLeScan(uuids, mLeScanCallback);
            }

                //attende per la durata dello scan e poi lancia la runnable per stopparlo
            scanHandler.postDelayed(stopScan, setup.getScanPeriod());

        }
    };

        //thread per mettere in pausa lo scan ed eventualmente elaborare i dati
    private Runnable stopScan = new Runnable() {
        @Override
        public void run() {

            Log.e(TAG, "Stop Scan");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    MainApplication.getmBluetoothAdapter().getBluetoothLeScanner()
                            .stopScan(mScanCallback);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    Log.e("bluetooth error","accendi il bluetooth");
                }

            }
            else {
                MainApplication.getmBluetoothAdapter().stopLeScan(mLeScanCallback);
            }
            Log.i(TAG,"numero: " + mLeDeviceListAdapter.getCount());

            //trova il beacon più vicino

            selectedBeacon = mLeDeviceListAdapter.selectedDevice();

            if(selectedBeacon!=null){
                if (currentBeacon==null || !currentBeacon.getAddress().equals(mLeDeviceListAdapter.getCurrentBeacon().getAddress())) {
                    currentBeacon = mLeDeviceListAdapter.getCurrentBeacon();
                    cont = 0;
                    update();
                }
                //nel caso per n cicli non venga aggiornato
                else {
                    cont++;
                    if(cont>=maxNoUpdate) {
                        currentBeacon = selectedBeacon;
                        update();
                        cont = 0;
                    }

                }
            }

                //finita l'esecuzione dello stato richiama
            int next = nextState();
            changeState(next);
            executeState();


        }
    };

        //thread per gestire l'attesa fra due scan consecutivi
    private Runnable wait = new Runnable() {
        @Override
        public void run() {
            //finita l'attesa richiama i metodi per passare allo stato successivo
            int next = nextState();
            changeState(next);
            executeState();
        }
    };


    /**
     * Metodo che cancella la registrazione del broadcast receiver
     */
    public void closeScan() {
        if(broadcastReceiver!=null) activity.getBaseContext().unregisterReceiver(broadcastReceiver);
    }

        //callback utilizzata per trovare dispositivi nel raggio d'azione
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi,
                                     byte[] scanRecord) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLeDeviceListAdapter.addDevice(device,rssi);
                        }
                    });
                }

            };


        //callback utilizzata per trovare dispositivi nel raggio d'azione
    private ScanCallback mScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("result", result.toString());
            BluetoothDevice btDevice = result.getDevice();
            mLeDeviceListAdapter.addDevice(btDevice,result.getRssi());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            //System.out.println("BLE// onBatchScanResults");
            for (ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            //System.out.println("BLE// onScanFailed");
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }

    };

    // metodo update del listener per l'aggiornamento della struttura dati condivisa alla scoperta di un beacon
    @Override
    public void update() {

        //viene aggiornato il beacon a cui si è collegato l'utente
        String beaconCod = currentBeacon.getAddress();

        Node nodeRoomBeacon = null;

        Iterator iteratorFloors = MainApplication.getFloors().entrySet().iterator();

        // si cerca se il mac adress del beacon trovato è presente nel file dei nodi
        while (iteratorFloors.hasNext() && (nodeRoomBeacon == null))
        {
            Map.Entry pairFloor = (Map.Entry) iteratorFloors.next();

            Floor currentFloor = MainApplication.getFloors().get(pairFloor.getKey().toString());

            Iterator iteratorNodes = currentFloor.getNodes().entrySet().iterator();

            while (iteratorNodes.hasNext() && (nodeRoomBeacon == null))
            {
                Map.Entry pairNodes = (Map.Entry) iteratorNodes.next();

                if (currentFloor.getNodes().get(pairNodes.getKey().toString()).getBeaconId().equals(beaconCod))
                    nodeRoomBeacon = currentFloor.getNodes().get(pairNodes.getKey().toString());
            }
        }

        // mac adress beacon trovato e si aggiornano le informazioni sulla posizione dell'utente come x, y, mac adress beacon e stanza in cui si trova
        if (nodeRoomBeacon != null)
        {
            Log.e("get","coords " + nodeRoomBeacon.getCoords() + " floor " + nodeRoomBeacon.getAltitude() + " room code " + nodeRoomBeacon.getRoomCod());

            //viene aggiornato il piano in cui si trova l'utente, le sue coordinate x,y e la stanza
            Data.getUserPosition().setFloor(nodeRoomBeacon.getAltitude());
            Data.getUserPosition().setRoomCod(nodeRoomBeacon.getRoomCod());
            Data.getUserPosition().setPosition(nodeRoomBeacon.getCoords());

            String serverResponse = "";

            // si invia la posizione dell'utente al server
            if(MainApplication.getOnlineMode() && UserHandler.isLogged())
                serverResponse = ServerComunication.sendUserPositionWithData(UserHandler.getUuid(), beaconCod, "no_data");

            // si controlla se nella risposta del server viene indicata se è presente un'emergenza
            try
            {
                // è presente un'emergenza
                if (serverResponse.equals("OK_emergency"))
                {
                    // si controlla se il bluetooth è attivo e ci sono informazioni sulla posizione corrente dell'utente
                    if (MainApplication.controlBluetooth() && Data.getUserPosition().getFloor() != null)
                    {
                        // se non è già attiva un'emergenza
                        if (!MainApplication.getEmergency())
                        {
                            // se l'applicazione è in primo piano si imposta il booleano per l'emergenza a vero per aprire la mappa a pieno schermo altrimenti si invia una notifica nel dispositivo
                            if(MainApplication.getVisible())
                                MainApplication.setEmergency(true);
                            else
                                MainApplication.launchNotification();
                        }
                    }
                }
            }
            catch (Exception e)
            {

            }
        }
    }

    // metodo retrive del listener
    @Override
    public void retrive() {

    }
}