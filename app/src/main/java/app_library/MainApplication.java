package app_library;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Log;


/*import com.example.user.progetto_ids.FullScreenMap;
import com.example.user.progetto_ids.Home;*/

import com.example.user.progetto_ids.MapActivity;
import com.example.user.progetto_ids.R;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import app_library.beacon.BeaconScanner;
import app_library.comunication.ServerComunication;
import app_library.maps.components.Node;
import app_library.maps.components.Floor;
import app_library.sharedstorage.Data;
import app_library.user.UserHandler;
import app_library.utility.CSVHandler;
import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Questa classe gestisce alcuni elementi riguarda la logica dell'applicazione.
 *
 */

public class MainApplication {

        //struttura dati per identificare i piani di un edificio (K: nome piano, V: piano)
    private static HashMap<String,Floor> floors;

        //flag che permette di capire se è presente o meno un'emergenza
    private static boolean emergency;
        //struttura utilizzata per interfacciarsi con il bluetooth
    private static BluetoothAdapter mBluetoothAdapter;
        //scanner per ricercare i dispositivi beacon
    private static BeaconScanner scanner;

        //identifica la home
    private static Activity activity;

        //identifica l'activity corrente dell'app
    private static Activity currentActivity;

        //flag che indica se l'activity è visibile o meno (serve per vedere se l'app è in background o meno)
    private static boolean visible;

        //costante usata per attivare il bluetooth
    private static final int REQUEST_ENABLE_BT = 1;

    private static NotificationManager notificationManager;
        //filtro usato per discriminare quali messaggi deve ricevere MainApplication
    private static IntentFilter intentFilter;
        //identificativo del messaggio che si può ricevere
    public static final String TERMINATED_SCAN = "TerminatedScan";
        //modalità di funzionamento dell'applicazione (per gestire le comunicazioni col server)
    private static boolean onlineMode = true;

        //parametri per la durata dello scan (presi dal server)
    //private static HashMap<String, Long> scanParameters;

        //flag che indica quando l'applicazione sta per essere chiusa (passando dal backbutton)
    private static boolean isFinishing;


    /**
     * Metodo che inizializza i parametri legati all'applicazione
     * @param a, activity in cui vengono inizializzati i parametri (activity home)
     */
    public static void start(Activity a) {
        activity = a;
        initializeFilter();
        emergency = false;
            //registrato il receiver
        activity.getBaseContext().registerReceiver(broadcastReceiver,intentFilter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            //attivazione del bluetooth (qualora non sia già funzionante)
        if(!controlBluetooth()) activateBluetooth();
            //creazione dello scanner
        if(mBluetoothAdapter!=null) initializeScanner(activity);
            //inizializzata la struttura dati legata all'utente
        UserHandler.init();


            //impostato l'indirizzo ip del server
        //ServerComunication.setHostMaster(PreferenceManager.getDefaultSharedPreferences(activity.getBaseContext()).getString("serverIp",""));


        //creata struttura dati legata ai nodi dell'edificio, leggendo dal file salvato in memoria intera
        /*ArrayList<String[]> nodeList = CSVHandler.readCSV(CSVHandler.FILE_NODE, activity.getBaseContext());
        loadNode(nodeList);*/

        if(MainApplication.getOnlineMode() && UserHandler.isLogged())
            UserHandler.initializePosition();

        isFinishing = false;
    }
    /**
     * Metodo per impostare la modalità di funzionamento dell'applicazione
     * @param b, valore che si vuole assegnare al flag per la modalità di funzionamento
     */
    public static void setOnlineMode(boolean b) {
        onlineMode = b;
    }

    /**
     * Metodo che indica se il funzionamento dell'applicazione è in modalità online oppure offline
     * @return true se modalità online, false se offline
     */
    public static boolean getOnlineMode(){ return onlineMode;}
    /**
     * Metodo per impostare l'activity in cui si trova a lavorare l'applicazione
     * @param a, activity in cui si trova a lavorare l'applicazione
     */
    public static void setCurrentActivity (Activity a) {
        currentActivity = a;
    }

    /**
     * Metodo che restituisce l'activity in cui si trova l'applicazione in quel momento
     * @return activity in cui si trova a lavorare l'applicazione
     */
    public static Activity getCurrentActivity () {
        return currentActivity;
    }
    /**
     * Metodo per impostare la visibilità o meno dell'applicazione. Considerando che ad ogni cambio di activity
     * una viene chiusa (il flag diviene false) e l'altra viene aperta (il flag diviene true), quando si il metodo restituisce
     * un valore false si presuppone che l'applicazione stia lavorando in background
     * @param b, valore che si vuole assegnare al flag per la visibilità dell'activity
     */
    public static void setVisible(boolean b) {
        visible = b;
//        Log.i("visible",""+visible);
    }


    /**
     * Metodo che restituisce la visibilità dell'applicazione
     * @return, visibilità dell'applicazione
     */
    public static boolean getVisible() {
        return visible;
    }
    /**
     * Metodo che restituisce l'istanza contenente le durata per le varie fasi dello scan, ricevute dal server
     * @return, hashmap (K: nome parametro, V:sua durata in millisecondi) contenente i valori per le varie fasi dello scan
     */
    /*public static HashMap<String,Long> getScanParameters() {
        return scanParameters;
    }*/

    /**
     * Metodo imposta l'attributo isFinishing, che indica se l'applicazione sta per essere chiusa
     * @param b, booleano che indica se l'applicazione sta per essere chiusa o meno
     */
    public static void setIsFinishing(boolean b) {
        isFinishing = b;
    }

    /**
     * Metodo che assegna l'istanza contenente le durata per le varie fasi dello scan, ricevute dal server
     * @param s, hashmap (K: nome parametro, V:sua durata in millisecondi) contenente i valori per le varie fasi dello scan
     */
    /*public static void setScanParameters(HashMap<String,Long> s) {
        scanParameters = s;
    }*/

    /**
     * Metodo che restituisce la struttura dati in cui sono memorizzati i beacon
     * @return, struttura dati contenente i beacon
     */
    /*public static HashMap<String,Node> getSensors() {
        return sensors;
    }*/


    /**
     * Metodo che restituisce l'adapter necessario per la comunicazione bluetooth
     * @return, adapter per la comunicazione bluetooth
     */
    public static BluetoothAdapter getmBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    /**
     * Metodo che restituisce la struttura dati in cui sono memorizzati i piani di un edificio
     * @return, struttura dati contenente i piani di un edificio
     */
    public static HashMap<String,Floor> getFloors(){
        return floors;
    }
    /**
     * Metodo che indica se sia presente o meno un'emergenza nell'edificio
     * @return, presenza o meno di un'emergenza
     */
    public static boolean getEmergency() {
        return emergency;
    }
    /**
     * Metodo per impostare lo stato di emergenza dell'applicazione
     * @param e, flag per indicare la presenza di un'emergenza (true indica la presenza di un'emergenza, false l'assenza)
     */
    public static void setEmergency(boolean e) {
        emergency = e;
        Log.i("emergency","emergency: " + emergency);
        scanner.suspendScan();
    }

    /**
     * Metodo che restituisce l'oggetto utilizzato per lo scan dei sensori
     * @return, scanner dei sensori
     */
    public static BeaconScanner getScanner() {
        return scanner;
    }
    /**
     * Metodo che restituisce l'attributo activity
     * @return, attributo activity
     */
    public static Activity getActivity() {
        return activity;
    }

    /**
     * Metodo utilizzato per inizializzare lo scanner, con il setup di default (NORMAL condition).
     * @param a, activity in cui viene creato lo scan
     */
    public static void initializeScanner(Activity a) {
        scanner = new BeaconScanner(a);
    }

    /**
     * Metodo utilizzato per inizializzare lo scanner, con il setup dato dal parametro cond.
     * @param a, activity in cui viene creato lo scan
     * @param cond, identificativo del tipo di setup con cui si vuole costruire lo scanner
     */
    public static void initializeScanner(Activity a, String cond) {
        scanner = new BeaconScanner(a,cond);
    }


    /**
     * Metodo per caricare nella struttura dati dei piani i nodi i cui valori sono passati come parametro
     * @param listNodeRead, i valori dei nodi da caricare in memoria
     */
    public static void loadNode(ArrayList<String[]> listNodeRead) {

        floors = new HashMap<>();

        for (String[] currentNode : listNodeRead) {

            String roomCod = currentNode[0];
            String beaconId = currentNode[1];
            int[] coords = new int[2];
            coords[0] = Integer.parseInt(currentNode[2]); //x
            coords[1] = Integer.parseInt(currentNode[3]); //y
            String altitude = currentNode[4];
            double width = Double.parseDouble(currentNode[5].replace(",","."));

            //il piano esiste
            if(floors.containsKey(altitude))
                floors.get(altitude).addNode(roomCod, new Node(roomCod, beaconId, coords.clone(), altitude, width));
            //aggiungo il nuovo piano
            else
            {
                floors.put(altitude, new Floor(altitude));
                floors.get(altitude).addNode(roomCod, new Node(roomCod, beaconId, coords.clone(), altitude, width));
            }
        }
    }


    /**
     * Metodo che restituisce una lista di stringhe, contenente i nomi dei piani
     * @return lista di stringhe contenente i nomi dei piani
     */
    public static ArrayList<String> obtainFloorsName()
    {
        ArrayList<String> floorKeyOut = new ArrayList();

        Iterator iterator = getFloors().entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry pair = (Map.Entry) iterator.next();
            floorKeyOut.add(pair.getKey().toString());
        }

        return floorKeyOut;
    }


    /*public static void closeApp(DeviceNotifReceiver httpServerThread) {

        scanner.suspendScan();

        if(MainApplication.getOnlineMode()) {

            if (httpServerThread.status()) {
                try {
                    httpServerThread.closeConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                ServerComunication.deletePosition(UserHandler.getIpAddress());
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }*/


    /*public static void closeApp() {

        scanner.suspendScan();

        if(MainApplication.getOnlineMode() && UserHandler.isLogged())
            ServerComunication.deleteUserPositionWithData(UserHandler.getUuid());
    }*/


        //il broadcast receiver deputato alla ricezione dei messaggi
    private static BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        Log.i("MESSAGE ARRIVED","ricevuto broadcast: " + intent.getAction());

            //MainApplication può ricevere solo questo messaggio, che indica il fatto che lo scanner
            //ha terminato il proprio ciclo di funzionamento, se ne può quindi far partire un altro
        if(intent.getAction().equals("TerminatedScan")) {

            if (isFinishing)
            {
                if(broadcastReceiver!=null) activity.unregisterReceiver(broadcastReceiver);
                scanner.closeScan();
                scanner = null;
                activity.finish();
            }
            else
            {
                //viene gestito il cambio di activity nel caso in cui ci sia un'emergenza
                if(emergency)
                {
                    //se lo scanner fino a quel momento ha lavorato in modalità normale
                    //significa che l'applicazione non si trovava in modalità SEARCHING
                    scanner.closeScan();
                    scanner = null;

                    //presi i dati riferiti alla posizione per poter inizializzare l'activity Map
                    String currPosFloorName = Data.getUserPosition().getFloor();
                    String currPosRoomCod = Data.getUserPosition().getRoomCod();

                    String mapExtraInformationCurrPos = currPosFloorName.concat(";").concat(currPosRoomCod);
                    Log.i("mex", mapExtraInformationCurrPos);

                    // si apre la mappa
                    Intent intentMap = new Intent (context, MapActivity.class);
                    intentMap.putExtra("map_info_curr_pos", mapExtraInformationCurrPos);
                    context.startActivity(intentMap);
                }
                //viene gestito il cambio di activity nel caso in cui non sia presente un'emergenza
                else
                {
                    //se lo scanner in quel momento si trova in modalità normale,
                    //significa che non è in corso una ricerca
                    if(scanner.getSetup().getState().equals("NORMAL"))
                    {
                        scanner.closeScan();
                        scanner = null;
                        //viene inviato un messaggio per creare la FullScreenMap activity
                        context.sendBroadcast(new Intent("STARTMAPS"));
                    }
                    //se lo scan sta lavorando in modalità diversa da NORMAL, significa che si trova nella
                    //FullScreenMaps
                    else
                    {
                        //viene inviato il messaggio per chiudere la FullScreenMaps
                        context.sendBroadcast(new Intent("EXIT_MAPS"));
                        scanner.closeScan();
                        scanner = null;
                        //riinizializzato lo scanner in modalità NORMAL
                        initializeScanner(activity);
                    }
                }
            }

        }
        }
    };

    /**
     * Metodo per costruire il filtro per i messaggi che può ricevere il broadcastReceiver
     */
    private static void initializeFilter() {
        intentFilter = new IntentFilter();
        intentFilter.addAction(TERMINATED_SCAN);
    }


    /**
     * Metodo che controlla se il dispositivo su cui sta lavorando l'applicazione ha il bluetooth acceso
     */
    public static boolean controlBluetooth() {
        boolean b;
        if (getmBluetoothAdapter()==null || !getmBluetoothAdapter().isEnabled()) b = false;
        else b = true;
        return b;
    }








    /**
     * Metodo per lanciare una notifica push, qualora l'applicazione sia in background nel momento
     * in cui sopraggiunge un'emergenza
     */
    public static void launchNotification() {
            //activity creata al click della notifica
        /*Intent intent = new Intent(activity, Home.class);
        intent.putExtra("MESSAGE","EMERGENCY");

        PendingIntent pIntent = PendingIntent.getActivity(activity, (int) System.currentTimeMillis(), intent, 0);*/
            //creazione della notifica vera e propria
        /*Notification n  = new Notification.Builder(activity)
                .setContentTitle("Progetto Ingegneria")
                .setContentText("C'è un'emergenza")
                .setSmallIcon(R.drawable.danger)
                .setLargeIcon(BitmapFactory.decodeResource(activity.getResources(), R.drawable.danger))
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .addAction(R.drawable.danger, "Open", pIntent).build();

        notificationManager =
                (NotificationManager) activity.getSystemService(NOTIFICATION_SERVICE);*/
            //lancio della notifica
        //notificationManager.notify(0, n);
    }







    /**
     * Metodo per cancellare eventuali notifiche rimaste sul dispositivo dell'utente, nonostante
     * l'emergenza sia terminata
     */
    public static void deleteNotification() {
        //if (notificationManager!=null) notificationManager.cancel(0);
    }

    /**
     * Metodo all'interno del quale viene richiesta l'attivazione del bluetooth
     */
    public static void activateBluetooth () {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

}

