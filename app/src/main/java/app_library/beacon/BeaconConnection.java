package app_library.beacon;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import app_library.MainApplication;
import app_library.comunication.ServerComunication;
import app_library.comunication.Message;
import app_library.user.UserHandler;
import app_library.utility.StateMachine;

import static java.lang.Math.pow;

/**
    Classe utilizzata per gestire la connessione con un beacon
 */

public class BeaconConnection extends StateMachine {

        //alcuni possibili messaggi che si possono ricevere durante
        //la connessione(vengono utilizzati come parametri per l'intenFilter)
    public static final String DATA_CHANGED = "dataChanged";
    public static final String ACKNOWLEDGE = "ACKNOWLEDGE";
    public static final String STOP_CONNECTION = "STOP CONNECTION";

        //indirizzi UUID riferiti alla temperatura (il primo indica il servizio, il secondo la configurazione del sensore
        //mentre il terzo i dati veri e proprio
    private static final String UUIDTemp = "f000aa00-0451-4000-b000-000000000000";
    private static final String UUIDTempConfig = "f000aa02-0451-4000-b000-000000000000";
    private static final String UUIDTempData = "f000aa01-0451-4000-b000-000000000000";

        //indirizzi UUID riferiti al barometro (il primo indica il servizio, il secondo la configurazione del sensore
        //mentre il terzo i dati veri e proprio
    private static final String UUIDBarometer = "f000aa40-0451-4000-b000-000000000000";
    private static final String UUIDBarometerConfig = "f000aa42-0451-4000-b000-000000000000";
    private static final String UUIDBarometerData = "f000aa41-0451-4000-b000-000000000000";

        //indirizzi UUID riferiti al sensore di movimento (il primo indica il servizio, il secondo la configurazione del sensore
        //mentre il terzo i dati veri e proprio
    private static final String UUIDMovement = "f000aa80-0451-4000-b000-000000000000";
    private static final String UUIDMovementConfig = "f000aa82-0451-4000-b000-000000000000";
    private static final String UUIDMovementData = "f000aa81-0451-4000-b000-000000000000";

        //indirizzi UUID riferiti al sensore di luce (il primo indica il servizio, il secondo la configurazione del sensore
        //mentre il terzo i dati veri e proprio
    private static final String UUIDLuxometer = "f000aa70-0451-4000-b000-000000000000";
    private static final String UUIDLuxometerConfig = "f000aa72-0451-4000-b000-000000000000";
    private static final String UUIDLuxometerData = "f000aa71-0451-4000-b000-000000000000";

    private static final String TAG = "MyBroadcastReceiver";

        //activity in cui viene creata la connessione
    private Activity activity;
        //filtro dei messaggi per il broadcast receiver
    private IntentFilter intentFilter;

        //contatore utilizzato per scandire i servizi del beacon
    private int cont;
        //timer utilizzato per sbloccare la macchina a stati, nel caso in cui la comunicazione
    private Handler timer;
        //durata in millisecondi del timer
    private static final long timerPeriod = 50000l;
        //flag che indica si dati sono stati letti o meno dal sensore
    private boolean readData;

        //identifica il servizio di cui si stanno leggendo i valori
    private BeaconService currentService;
        //identifica il dispositivo (beacon) con cui ci si è collegati
    private BluetoothDevice device;
        //flag che indica se è stata stabilita la connessione
    private boolean connectionStarted;

        //lista di servizi trovati dalla callback
    private ArrayList<BluetoothGattService> findServices;
        //lista di servizi ricercati
    private ArrayList<BeaconService> services;

    /**
     * Costruttore dell'oggetto BeaconConnection
     * @param a, activity in cui viene creata la connessione
     * @param d, dispositivo bluetooth a cui ci si deve collegare
     */
    public BeaconConnection(Activity a, BluetoothDevice d) {
        super();
        Log.i("CONNECTION","new connection");
        connectionStarted = false;
        device = d;
        cont = 0;
        readData = false;
        activity = a;
        timer = new Handler();
        timer.postDelayed(runnable,timerPeriod);
        initializeFilter();
        initializeServices();
        connectionStarted = true;
        findServices = new ArrayList<>();
        activity.getBaseContext().registerReceiver(broadcastReceiver,intentFilter);
    }

    /**
     * Metodo richiamato quando viene attivata la connessione (esegue lo stato 0 della macchina a stati)
     */
    public void start() {
        executeState();
    }


    protected int nextState() {
        int next = 0;
        Log.i("RUNNING","running " + running);
        switch(currentState) {
            case(0):
                    //caso in cui la connessione vada chiusa
                if (!running) next = 8;
                else
                    next = 1;
                break;
            case(1):
                    //caso in cui si debba chiudere la connessione o siano stati letti tutti i servizi
                if (!running || cont>=services.size())
                    next = 8;
                else
                    next = 2;
                break;
            case(2):
                    //caso in cui si debba chiudere la connessione
                if (!running) next = 6;
                else next = 3;
                break;
            case(3):
                    //caso in cui si debba chiudere la connessione
                if (!running) next = 5;
                else next = 4;
                break;
            case(4):
                next = 5;
                break;
            case(5):
                next = 6;
                break;
            case(6):
                    //caso in cui vadano letti i dati presi dal sensore
                if(readData) next = 7;
                else next = 8;
                break;
            case(7):
                    //caso in cui si debba chiudere la connessione o siano stati letti tutti i servizi
                if (!running || cont>=services.size()) next = 8;
                else next = 1;
                break;

        }
        return next;
    }


    protected void executeState() {
        Log.i("State","execute connection state " + getState());
        switch(currentState) {
            case(0):
                    //viene effettuata la connessione al sensore
                BConnGattLeService.execute(device, activity.getBaseContext());
                break;
            case(1):
                readData = false;
                    //vengono letti i servizi disponibili
                findServices = BConnGattLeService.getServices();
                    //ci si collega al servizio
                searchService();
                break;
            case(2):
                    //attivato il sensore riferito al servizio da leggere
                BConnGattLeService.changeStateSensor(BConnGattLeService.getmBluetoothGatt(), currentService, true);
                break;
            case(3):
                    //scritta maschera di bit in modo che il sensore invii dati tramite il bluetooth
                BConnGattLeService.changeNotificationState(BConnGattLeService.getmBluetoothGatt(), currentService, true);
                break;
            case(4):
                    //letti i dati dal sensore
                BConnGattLeService.initializeData();
                readData = true;
                break;
            case(5):
                BConnGattLeService.setSampleFlag(false);
                    //disattivato il sensore riferito al servizio da leggere
                BConnGattLeService.changeNotificationState(BConnGattLeService.getmBluetoothGatt(), currentService, false);
                break;
            case(6):
                    //scritta maschera di bit in modo che il sensore smetta di inviare dati tramite il bluetooth
                BConnGattLeService.changeStateSensor(BConnGattLeService.getmBluetoothGatt(), currentService, false);
                break;
            case(7):
                    //analizzati i dati letti dal sensore
                BConnGattLeService.analyzeData();
                break;
            case(8):

                //impacchettato il messaggio ed eventualmente viene inviato al server
                if(MainApplication.getOnlineMode() && UserHandler.isLogged()) {

                    String mex = packingMessage();
                    ServerComunication.sendUserPositionWithData(UserHandler.getUuid(), device.getAddress(), mex);
                }

                timer.removeCallbacks(runnable);
                    //chiusa la connessione e cancellata la registrazione del broadcast receiver
                close();
                BConnGattLeService.closeConnection();
                activity.getBaseContext().sendBroadcast(new Intent("ScanPhaseFinished"));
                break;

        }
    }

    /**
     * Metodo per gestire la ricezione dei messaggi
     * @param intent, indica il messaggio ricevuto
     */
    private void messageHandler(Intent intent) {
        switch (intent.getAction()) {
                //messaggio ricevuto in condizioni normali, inviato quando uno stato della macchina termina le istruzioni da eseguire
            case(ACKNOWLEDGE):
                if (BConnGattLeService.getmConnectionState()==0) running = false;
                    //viene calcolato il nuovo stato e vengono eseguite le istruzioni
                int next = nextState();
                changeState(next);
                executeState();
                break;
                    //messaggio ricevuto quando va stoppata la connessione per il sopraggiungere di un evento esterno
            case(STOP_CONNECTION):
                running = false;
                break;
                    //messaggio ricevuto quando sono stati letti un numero sufficente di dati da un sensore
            case (DATA_CHANGED):
                Log.i(TAG,"data changed");
                    //viene estratto il contenuto del messaggio
                Bundle b = intent.getExtras();
                    //vengono salvati i dati estratti dal messaggio (nel caso dell'accelerometro tre componenti)
                if (services.get(cont).getName().equals("movement")) {
                    double[] v = b.getDoubleArray("data");
                    services.get(cont).setValue(v);
                }
                    //viene salvato il dato estratti dal messaggio (per tutti gli altri sensori unico valore)
                else {
                    double v = b.getDouble("data");
                    services.get(cont).setValue(v);
                }
                    //aggiorna il timestamp
                services.get(cont).setLastSampleTime(new Timestamp(System.currentTimeMillis()));

                services.get(cont).printValue();
                    //aumentato il contatore per scorrere tutti i servizi da analizzare
                cont++;
                    //viene calcolato il nuovo stato e vengono eseguite le istruzioni
                next = nextState();
                changeState(next);
                executeState();

                break;
        }
    }

    /**
     * Metodo per impacchettare il messaggio da inviare al server
     */
    private String packingMessage() {

        // "timestamp"-"temperature"-"luxometer"-"barometer"-"accx"-"accy"-"accz"
        String mex;

        mex = ("" + new Timestamp(System.currentTimeMillis()).toString());

        int c = 0;

        //aggiunti i valori riferiti ai dati estratti dai sensori
        for (int i=0;i<services.size(); i++) {
            for (int j=0; j<services.get(i).getValue().size(); j++) {
                mex += ("-" + services.get(i).getValue().get(j));
                c++;
            }
        }

        //qualora alcuni valori non siano stati letti, riempe value con "- "
        while(c<6) {
            mex += "- ";
            c++;
        }

        return mex;
    }
        //broadcast receiver per lo scambio di messaggi
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG,"ricevuto broadcast: " + intent.getAction());
            messageHandler(intent);
        }
    };

    /**
     * Metodo per impostare i filtri sui messaggi ricevuti
     */
    private void initializeFilter() {
        intentFilter = new IntentFilter();
        intentFilter.addAction(ACKNOWLEDGE);
        intentFilter.addAction(DATA_CHANGED);
        intentFilter.addAction(STOP_CONNECTION);
    }

    /**
     * Metodo per selezionare i servizi desiderati
     */
    private void initializeServices() {
        services = new ArrayList<>();
        services.add(new BeaconService("temperature",UUIDTemp,UUIDTempData,UUIDTempConfig));
        services.add(new BeaconService("luxometer",UUIDLuxometer,UUIDLuxometerData,UUIDLuxometerConfig));
        services.add(new BeaconService("barometer",UUIDBarometer,UUIDBarometerData,UUIDBarometerConfig));
        services.add(new BeaconService("movement",UUIDMovement,UUIDMovementData,UUIDMovementConfig));

    }

    /**
     * Metodo per ricercare un servizio da analizzare,
     * fra quelli contenuti nell'arraylist di servizi di interesse per l'applicazione
     */

    private void searchService() {
        boolean b = false;
        int i = 0;
            //aggiorna il servizio da ricercare
        currentService = services.get(cont);
            //controllo sul contatore, per verificare che ancora ci siano servizi non analizzati
        if (cont<services.size()) {
                //fra la lista dei servizi cerca currentService
            while(i<findServices.size() && b==false) {
                    //controlla che il currentService sia fra quelli disponibili
                if (findServices.get(i).getUuid().equals(currentService.getService())) {
                    b = true;
                    Log.i(TAG,"service found");
                }
                i++;
            }
        }

            //viene calcolato il nuovo stato e vengono eseguite le istruzioni
        int next = nextState();
        changeState(next);
        executeState();

    }

    /**
     * Metodo utilizzato quando termina la connessione con il sensore,
     * viene cancellata la registrazione del receiver
     */
    public void close() {
        if (connectionStarted) {
            connectionStarted = false;
                //viene cancellata la registrazione del receiver
            activity.getBaseContext().unregisterReceiver(broadcastReceiver);
        }
    }
        //thread eseguito nel caso in cui il timer arrivi alla fine e si debba uscire dalla connessione
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Log.e("error","si è verificato un problema");
            running = false;
            int next = nextState();
            changeState(next);
            executeState();
        }
    };


    /**
     Classe utilizzata per la connessione vera e propria al beacon
     */

    public static class BConnGattLeService {

        public static final String TAG = "GattLeService";

        private static Context context;
        //lista di servizi da analizzare
        private static ArrayList<BluetoothGattService> services;

        //dispositivo con cui collegarsi
        private static BluetoothDevice device;
        //interfaccia per collegarsi a dispositivo
        private static BluetoothGatt mBluetoothGatt;
        //stato della connessione
        private static int mConnectionState;

        //numero di campioni da estrarre per ogni sensore
        private static final int numSample = 5;
        //servizio necessario per la modifica delle impostazioni dei sensori
        private static final String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

        private static final int STATE_DISCONNECTED = 0;
        private static final int STATE_CONNECTING = 1;
        private static final int STATE_CONNECTED = 2;

        //indica il nome del servizio attualmente analizzato
        private static String serviceAnalyzed;

        private static BeaconService currentBeacon;
        //contatore indicante il numero di campioni preso per un servizio
        private static int cont;

        //flag utilizzato per scartare la prima misurazione del sensore
        private static boolean sampleFlag = false;

        //lista di dati estratti dai sensori
        private static ArrayList<Double>[] data;

        /**
         * Metodo per inizializzare l'interfaccia per la connessione al beacon
         * @param d, dispositivo con cui ci si vuole connettere
         * @param c dell'applicazione, necessario per accedere alle sue risorse
         */
        public static void execute(BluetoothDevice d, Context c) {
            device = d;
            services = new ArrayList<>();
            mConnectionState = STATE_DISCONNECTED;
            context = c;
            cont = 0;
            //cominca la connessione con il beacon
            mBluetoothGatt = device.connectGatt(context,false,mGattCallback);
        }

        public static void setSampleFlag(boolean b) {
            sampleFlag = b;
        }

        public static void printServices() {
            Log.e("onServicesDiscovered", "Services count: " + mBluetoothGatt.getServices().size());
            for (BluetoothGattService service: mBluetoothGatt.getServices()) {
                String serviceUUID = service.getUuid().toString();
                Log.e("onServicesDiscovered", "Service uuid " + serviceUUID);
                for (BluetoothGattCharacteristic chaar : service.getCharacteristics()) {
                    Log.i(TAG,"char uuid " + chaar.getUuid());
                }
            }
        }

        /**
         * Metodo che restituisce i servizi disponibili nel beacon
         * @return lista dei servizi disponibili nel beacon
         */
        public static ArrayList<BluetoothGattService> getServices() {
            for (BluetoothGattService service: mBluetoothGatt.getServices()) {
                services.add(service);
            }
            return services;
        }

        /**
         * Metodo che inizializza la struttura per immagazzinare i dati letti dal sensore
         */
        public static void initializeData() {
            //contenitore dei dati
            data = new ArrayList[numSample];

            cont = 0;

            //inizializzati elementi dell'array
            for (int i=0; i<data.length;i++) {
                data[i] = new ArrayList<>();
            }
        }

        /**
         * Metodo per impacchettare i dati inviati raccolti dai sensori ed inviarli tramite un messaggio
         */
        public static void analyzeData() {
            Intent intent = new Intent(BeaconConnection.DATA_CHANGED);
            ArrayList<Double> value = mediaValues(data);
            if (value.size()==1) {
                intent.putExtra("data",value.get(0));
            }
            else {
                double[] d = new double[value.size()];
                for (int i=0; i<value.size(); i++) {
                    d[i]=value.get(i);
                }
                intent.putExtra("data",d);
            }
            data = null;
            cont = 0;
            context.sendBroadcast(intent);
        }

        /**
         * Metodo che restituisce lo stato della connessione
         * @return stato della connessione
         */
        public static int getmConnectionState() {
            return mConnectionState;
        }

        //Interfaccia contenente tutte le callback necessarie per la connessione al beacon
        public static BluetoothGattCallback mGattCallback =
                new BluetoothGattCallback() {
                    @Override
                    //Callback richiamata quando scambia lo stato della connessione
                    public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                        int newState) {
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            mConnectionState = STATE_CONNECTED;
                            Log.i(TAG, "Connected to GATT server.");
                            Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());

                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            mConnectionState = STATE_DISCONNECTED;
                            Log.i(TAG, "Disconnected from GATT server.");
                            context.sendBroadcast(new Intent(BeaconConnection.ACKNOWLEDGE));
                        }
                    }

                    @Override
                    //Callback richiamata quando vengono scoperti nuovi servizi
                    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            Log.w(TAG,"GATT_SUCCESS");
                            printServices();
                            context.sendBroadcast(new Intent(BeaconConnection.ACKNOWLEDGE));
                        } else {
                            Log.w(TAG, "onServicesDiscovered received: " + status);
                        }
                    }

                    @Override
                    //Callback richiamata durante l'opezione di lettura della "caratteristica" dei byte per un sensore
                    public void onCharacteristicRead(BluetoothGatt gatt,
                                                     BluetoothGattCharacteristic characteristic,
                                                     int status) {
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            Log.i(TAG,"action data available " + characteristic.getUuid().toString());
                        }
                    }

                    @Override
                    //Callback richiamata durante l'opezione di scrittura sui byte di un sensore
                    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                        if (status == BluetoothGatt.GATT_SUCCESS) {

                            //status 0 significa che è stato modificato con successo
                            context.sendBroadcast(new Intent(BeaconConnection.ACKNOWLEDGE));

                        }

                    }

                    @Override
                    //Callback richiamata durante l'opezione di lettura del descrittore per un determinato sensore
                    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                        Log.i("DESCRIPTOR READ", "descriptor read");
                    }

                    @Override
                    //Callback richiamata durante l'opezione di scrittura
                    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                        Log.i("DESCRIPTOR WRITE", "descriptor write");

                        context.sendBroadcast(new Intent(BeaconConnection.ACKNOWLEDGE));
                    }


                    @Override
                    //Callback richiamata quando la "caratteristica" di un sensore viene modificata
                    //significa che i dati sono stati acquisiti dal sensore
                    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

                        //valuta se si tratta della prima misurazione del sensore per scartala
                        if (sampleFlag) {
                            //valuta se devono essere fatta ancora lettura
                            if(cont<numSample) {
                                //in base al tipo di servizio viene richiamato il metodo per leggere ed interpretare i byte
                                //della caratteristica
                                switch(serviceAnalyzed) {
                                    case("temperature"):
                                        data[cont].add(extractAmbientTemperature(characteristic));
                                        break;
                                    case("barometer") :
                                        data[cont].add(extractBarometerValue(characteristic));
                                        break;
                                    case("movement") :
                                        double[] val = extractAccelerometerValue(characteristic);
                                        for (Double d : val) {
                                            data[cont].add(d);
                                        }
                                        break;
                                    case("luxometer") :
                                        data[cont].add(extractAmbientLight(characteristic));
                                        break;
                                }
                                Log.i("CHARCHANGE", "char changed " + data[cont]);
                                cont++;
                            }
                            //infine viene inviato un messaggio per comunicare la fine della lettura dei dati
                            else {
                                context.sendBroadcast(new Intent(BeaconConnection.ACKNOWLEDGE));
                            }
                        }
                        sampleFlag = true;

                    }
                };
        /**
         * Metodo che calcola la media matematica sui dati letti da un sensore
         * @param v lista di misurazioni per un dato sensore
         * @return media matematica per un sensore
         */
        private static ArrayList<Double> mediaValues(ArrayList<Double>[] v) {
            ArrayList<Double> value;
            value = new ArrayList<>();
            double tot = 0;
            //analizzati elementi unidimensionali
            if(v[0].size()==1) {
                for (int i = 0; i<v.length; i++) {
                    tot += v[i].get(0);
                }
                value.add(tot/v.length);
            }
            //analizzati elementi multidimensionali (solo nel caso dell'accelerometro)
            else {
                for (int j=0; j<v[0].size(); j++) {
                    for (int i=0; i<v.length; i++) {
                        tot+=v[i].get(j);
                    }
                    value.add(tot/v.length);
                    tot = 0;
                }
            }
            return value;
        }

        /**
         * Metodo che chiude la connessione con il beacon
         */
        public  static void closeConnection() {
            if (mBluetoothGatt != null) {
                mBluetoothGatt.close();
            }
        }

        /**
         * Metodo per attivare o disattivare un sensore
         * @param gatt interfaccia per collegarsi tramite bluetooth al sensore
         * @param beacon indica il dispositivo beacon con i quali si è connessi
         * @param condition flag che indica se il sensore vada attivato (true) o disattivato (false)
         * @return media matematica per un sensore
         */
        public static void changeStateSensor(BluetoothGatt gatt, BeaconService beacon, boolean condition) {
            currentBeacon = beacon;
            //vengono presi indirizzi UUID per poter scrivere sul beacon
            UUID serviceUUID = beacon.getService();
            UUID configUUID = beacon.getServiceConfig();

            BluetoothGattService service = gatt.getService(serviceUUID);
            BluetoothGattCharacteristic config = service.getCharacteristic(configUUID);

            //in base a come si vuole cambiare lo stato del sensore bisogna scrivere una maschera di byte
            if (condition) {
                if (beacon.getName().equals("movement")) {
                    byte[] b = new byte[2];
                    b[0] = 0x38;
                    b[1] = 0x02;
                    config.setValue(b);
                }
                else {
                    config.setValue(new byte[]{1});
                }
            }
            else {
                if (beacon.getName().equals("movement")) {
                    byte[] b = new byte[2];
                    b[0] = 0x00;
                    b[1] = 0x00;
                    config.setValue(b);
                }
                else {
                    config.setValue(new byte[]{0});
                }
            }

            gatt.writeCharacteristic(config);
        }

        /**
         * Metodo per attivare o disattivare le notifiche all'applicazione provenienti dal beacon, in base al servizio passato
         * @param gatt interfaccia per collegarsi tramite bluetooth al sensore
         * @param beacon indica il dispositivo beacon con i quali si è connessi
         * @param condition flag che indica se le notifiche vadano attivate (true) o disattivate (false)
         * @return media matematica per un sensore
         */

        public static void changeNotificationState(BluetoothGatt gatt, BeaconService beacon, boolean condition) {
            //vengono presi indirizzi UUID per poter scrivere sul beacon
            UUID serviceUUID = beacon.getService();
            UUID dataUUID = beacon.getServiceData();
            UUID clientCharacteristicConfigUUID = UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG);
            currentBeacon = beacon;
            serviceAnalyzed = beacon.getName();

            BluetoothGattService service = gatt.getService(serviceUUID);
            BluetoothGattCharacteristic dataCharacteristic = service.getCharacteristic(dataUUID);
            //attivate le notifiche
            gatt.setCharacteristicNotification(dataCharacteristic, true);
            BluetoothGattDescriptor config = dataCharacteristic.getDescriptor(clientCharacteristicConfigUUID);

            //in base a come si vuole cambiare lo stato del sensore bisogna scrivere una maschera di byte
            if(condition) {
                config.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            }
            else {
                config.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            }

            gatt.writeDescriptor(config);
        }

        /**
         * Metodo estrarre i valori dall'accelerometro
         * @param c caratteristica di byte ricevuta dal sensore
         * @return array di valori estratti dal sensore (coordinata x, coordinata y, coordinata z)
         */
        private static double[] extractAccelerometerValue(BluetoothGattCharacteristic c) {
            // Range 8G
            double acc[] = new double[3];
            //deriva da 32768/8 (8G)
            final double SCALE = 4096.0;

            acc[0] = ((c.getValue()[7]<<8) + c.getValue()[6])/SCALE;
            acc[1] = ((c.getValue()[9]<<8) + c.getValue()[8])/SCALE;
            acc[2] = ((c.getValue()[11]<<8) + c.getValue()[10])/SCALE;

            Log.i("ACC","x: " + acc[0] + " y: " + acc[1] + " z: " + acc[2]);

            return acc;
        }

        /**
         * Metodo estrarre i valori dal barometro
         * @param c caratteristica di byte ricevuta dal sensore
         * @return valore estratto dal sensore
         */
        private static double extractBarometerValue(BluetoothGattCharacteristic c) {
            double value;
            if (c.getValue().length > 4) {
                Integer val = twentyFourBitUnsignedAtOffset(c.getValue(), 2);
                value = (double) val / 10000.0;
            }
            else {
                int mantissa;
                int exponent;
                Integer sfloat = shortUnsignedAtOffset(c, 2);

                mantissa = sfloat & 0x0FFF;
                exponent = (sfloat >> 12) & 0xFF;

                double magnitude = pow(2.0f, exponent);
                value = (mantissa * magnitude) / 10000.0;

            }
            return value;
        }

        /**
         * Metodo estrarre i valori dal sensore di temperatura
         * @param c caratteristica di byte ricevuta dal sensore
         * @return valore estratto dal sensore
         */
        private static double extractAmbientTemperature(BluetoothGattCharacteristic c) {
            int offset = 2;
            return shortUnsignedAtOffset(c, offset) / 128.0;
        }

        /**
         * Metodo estrarre i valori dal sensore di luce
         * @param c caratteristica di byte ricevuta dal sensore
         * @return valore estratto dal sensore
         */
        private static double extractAmbientLight(BluetoothGattCharacteristic c) {

            int mantissa;
            int exponent;

            byte[] b = new byte[2];
            b[0] = c.getValue()[0];
            b[1] = c.getValue()[1];
            Integer sfloat= shortUnsignedAtOffset(b, 0);

            mantissa = sfloat & 0x0FFF;
            exponent = (sfloat >> 12) & 0xFF;

            double output;
            double magnitude = pow(2.0f, exponent);
            output = (mantissa * magnitude)/100.0;

            return output;
        }

        /**
         * Metodo per interpretare le caratteristiche di 24 byte
         * @param c caratteristica di byte ricevuta dal sensore
         * @param offset eventuale offset da applicare al byte
         * @return valore numerico del byte
         */
        private static Integer twentyFourBitUnsignedAtOffset(byte[] c, int offset) {
            Integer lowerByte = (int) c[offset] & 0xFF;
            Integer mediumByte = (int) c[offset+1] & 0xFF;
            Integer upperByte = (int) c[offset + 2] & 0xFF;
            return (upperByte << 16) + (mediumByte << 8) + lowerByte;
        }
        /**
         * Metodo per interpretare le caratteristiche di byte
         * @param c caratteristica di byte ricevuta dal sensore
         * @param offset eventuale offset da applicare al byte
         * @return valore numerico del byte
         */

        private static Integer shortUnsignedAtOffset(BluetoothGattCharacteristic c, int offset) {

            Integer lowerByte = (int) c.getValue()[offset] & 0xFF;
            Integer upperByte = (int) c.getValue()[offset+1] & 0xFF;
            return (upperByte << 8) + lowerByte;
        }

        /**
         * Metodo per interpretare le caratteristiche di byte
         * @param c caratteristica di byte ricevuta dal sensore
         * @param offset eventuale offset da applicare al byte
         * @return valore numerico del byte
         */
        private static Integer shortUnsignedAtOffset(byte[] c, int offset) {
            Integer lowerByte = (int) c[offset] & 0xFF;
            Integer upperByte = (int) c[offset+1] & 0xFF;
            return (upperByte << 8) + lowerByte;
        }

        public static BluetoothGatt getmBluetoothGatt() {
            return mBluetoothGatt;
        }
    }

}
