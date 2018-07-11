package com.example.user.progetto_ids;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import app_library.MainApplication;
import app_library.comunication.ServerComunication;
import app_library.user.UserHandler;
import app_library.utility.CSVHandler;
import app_library.validation.FormControl;

/**
 * Created by User on 22/06/2018.
 */

// activity per settare le impostazioni dell'app
public class SettingActivity  extends AppCompatActivity {

    // costanti per il ritorno della modalità in cui si trova l'utente alla chiusura delle impostazioni ovvero offline loggato, offline non loggato, online loggato e online non loggato
    public static final String RETURN_STATE_OFFLINE_LOGGED = "Offline_logged_OK";
    public static final String RETURN_STATE_OFFLINE_NOT_LOGGED = "Offline_nologged_OK";
    public static final String RETURN_STATE_ONLINE_LOGGED = "Online_logged_OK";
    public static final String RETURN_STATE_ONLINE_NOT_LOGGED = "Online_nologged_OK";

    // elementi grafici
    private CheckBox checkBoxOffline;
    private TextView textViewIP;
    private EditText editTextIP;
    private Button buttonSaveEdit;
    private Button buttonBluetoothPerm;

    //codice utilizzato come risposta alla richiesta di attivazione della localizzazione sul dispositivo
    public static final int PERMISSIONS_REQUEST_LOCATION_FOR_BLUETOOTH = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setting);

        // impostazione del titolo dell'activity
        getSupportActionBar().setTitle("Impostazioni");

        // recupero riferimenti elementi grafici
        checkBoxOffline = (CheckBox) findViewById(R.id.checkBoxSettingOffline);
        textViewIP = (TextView) findViewById(R.id.textViewSettingIPServer);
        editTextIP = (EditText) findViewById(R.id.editTextSettingIPServer);
        buttonSaveEdit = (Button) findViewById(R.id.buttonSettingSaveEdit);
        buttonBluetoothPerm = (Button) findViewById(R.id.buttonSettingBluetoothPerm);

        // evento che si attiva alla pressione della checkBox per la modalità offline
        checkBoxOffline.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                // se è stata selezionata si disabilita l'elemento grafico per inserire l'indirizzo ip del server
                if (b)
                {
                    textViewIP.setEnabled(false);
                    editTextIP.setEnabled(false);
                }

                else
                {
                    textViewIP.setEnabled(true);
                    editTextIP.setEnabled(true);
                }
            }
        });


        // evento che si attiva alla pressione del pulsante salva modifiche
        buttonSaveEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // offline
                if (checkBoxOffline.isChecked())
                {
                    AlertDialog.Builder builderOffline = new AlertDialog.Builder(SettingActivity.this);
                    builderOffline.setTitle("Offline");
                    builderOffline.setCancelable(false);

                    // ho localmente nella memoria dell'app i dati sui nodi e archi
                    if(CSVHandler.csvContainsElements(CSVHandler.getFiles().get(CSVHandler.FILE_NODE)) && CSVHandler.csvContainsElements(CSVHandler.getFiles().get(CSVHandler.FILE_ROUTE)))
                    {
                        //creata struttura dati legata ai nodi dell'edificio, leggendo dal file salvato in memoria intera
                        ArrayList<String[]> nodeList = CSVHandler.readCSV(CSVHandler.FILE_NODE, getBaseContext());
                        MainApplication.loadNode(nodeList);

                        // si imposta la modalità offline
                        MainApplication.setOnlineMode(false);

                        builderOffline.setMessage("Hai tutte le informazioni necessarie per utilizzare la modalità offline");

                        builderOffline.setPositiveButton("OK", new DialogInterface.OnClickListener(){

                            public void onClick(DialogInterface dialog, int which){

                                dialog.dismiss();

                                String returnString;

                                // se l'utente è loggato si ritorna lo stato offline loggato
                                if (UserHandler.isLogged())
                                    returnString = RETURN_STATE_OFFLINE_LOGGED;
                                // si ritorna lo stato offline non loggato
                                else
                                    returnString = RETURN_STATE_OFFLINE_NOT_LOGGED;

                                Intent returnIntent = new Intent();
                                returnIntent.putExtra("settingResult", returnString);
                                setResult(Activity.RESULT_OK,returnIntent);
                                finish();
                            }
                        });
                    }
                    // se non ho localmente nella memoria dell'app i dati sui nodi e archi devo accedere prima online
                    else
                    {
                        builderOffline.setMessage("Non hai le informazioni necessarie per utilizzare la modalità offline. Accedi prima online per scaricare le mappe necessarie");

                        builderOffline.setPositiveButton("OK", new DialogInterface.OnClickListener(){

                            public void onClick(DialogInterface dialog, int which){

                                dialog.dismiss();
                            }
                        });
                    }

                    AlertDialog alertDialogOffline = builderOffline.create();
                    alertDialogOffline.show();

                }
                // online
                else
                {
                    AlertDialog.Builder builderOnline = new AlertDialog.Builder(SettingActivity.this);
                    builderOnline.setTitle("Online");
                    builderOnline.setCancelable(false);
                    String ipInserted = editTextIP.getText().toString();

                    // l'indirizzo ip è scritto correttamente
                    if (FormControl.ipControl(ipInserted))
                    {
                        // la comunicazione con il server ha successo
                        if (ServerComunication.handShake(ipInserted))
                        {
                            // si imposta l'indirizzo ip inserito
                            ServerComunication.setHostMaster(ipInserted);

                            // si ottengono i dati dei nodi e archi aggiornati dal server
                            String serverJSONDataResponse = ServerComunication.getJSONData();

                            // dati ricevuti con successo
                            if (!serverJSONDataResponse.equals(""))
                            {
                                // creazione strutture necessarie per i nodi e archi con i dati ricevuti dal server
                                HashMap<String,String>[] hashMapsNode = null;
                                HashMap<String,String>[] hashMapsRoute = null;

                                try
                                {
                                    hashMapsNode = ServerComunication.getNodeData(serverJSONDataResponse);
                                    hashMapsRoute = ServerComunication.getRouteData(serverJSONDataResponse);
                                }
                                catch (ExecutionException e) {
                                    e.printStackTrace();
                                    hashMapsNode = null;
                                    hashMapsRoute = null;
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                    hashMapsNode = null;
                                    hashMapsRoute = null;
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    hashMapsNode = null;
                                    hashMapsRoute = null;
                                }

                                // create le strutture con successo
                                if (hashMapsNode != null && hashMapsRoute != null)
                                {
                                    boolean updatedCSVCompleted = false;

                                    try
                                    {
                                        // aggiornamento dei csv per i nodi e archi nella memoria interna dell'app
                                        CSVHandler.updateCSV(hashMapsNode, getBaseContext(), CSVHandler.FILE_NODE);
                                        CSVHandler.updateCSV(hashMapsRoute, getBaseContext(), CSVHandler.FILE_ROUTE);

                                        // il csv è sovrascritto presentando elementi
                                        if (CSVHandler.csvContainsElements(CSVHandler.getFiles().get(CSVHandler.FILE_NODE)) && CSVHandler.csvContainsElements(CSVHandler.getFiles().get(CSVHandler.FILE_ROUTE)))
                                            updatedCSVCompleted = true;
                                        else
                                            updatedCSVCompleted = true;
                                    }
                                    catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (NullPointerException e) {
                                        Log.e("CSV Error","Update CSV Error");
                                    } catch (Exception e) {
                                    }

                                    // il csv è stato sovrascritto correttamente
                                    if (updatedCSVCompleted)
                                    {
                                        //creata struttura dati legata ai nodi dell'edificio, leggendo dal file salvato in memoria intera
                                        ArrayList<String[]> nodeList = CSVHandler.readCSV(CSVHandler.FILE_NODE, getBaseContext());
                                        MainApplication.loadNode(nodeList);

                                        // si imposta la modalità online
                                        MainApplication.setOnlineMode(true);

                                        builderOnline.setMessage("La comunicazione con il server è avvenuta con successo e sono state scaricate le mappe aggiornate");

                                        builderOnline.setPositiveButton("OK", new DialogInterface.OnClickListener(){

                                            public void onClick(DialogInterface dialog, int which){

                                                dialog.dismiss();

                                                String returnString;

                                                // se l'utente è loggato si ritorna lo stato online loggato
                                                if (UserHandler.isLogged())
                                                    returnString = RETURN_STATE_ONLINE_LOGGED;
                                                // se l'utente non è loggato si ritorna lo stato online non loggato
                                                else
                                                    returnString = RETURN_STATE_ONLINE_NOT_LOGGED;

                                                Intent returnIntent = new Intent();
                                                returnIntent.putExtra("settingResult", returnString);
                                                setResult(Activity.RESULT_OK,returnIntent);
                                                finish();
                                            }
                                        });
                                    }
                                    else
                                    {
                                        builderOnline.setMessage("Si è verificato un problema nell'aggiornamento dei dati locali");

                                        builderOnline.setPositiveButton("OK", new DialogInterface.OnClickListener(){

                                            public void onClick(DialogInterface dialog, int which){

                                                dialog.dismiss();
                                            }
                                        });
                                    }
                                }
                                else
                                {
                                    builderOnline.setMessage("Formato dei dati scaricati non supportato");

                                    builderOnline.setPositiveButton("OK", new DialogInterface.OnClickListener(){

                                        public void onClick(DialogInterface dialog, int which){

                                            dialog.dismiss();
                                        }
                                    });
                                }
                            }
                            else
                            {
                                builderOnline.setMessage("Si è verificato un problema nel download dei dati");

                                builderOnline.setPositiveButton("OK", new DialogInterface.OnClickListener(){

                                    public void onClick(DialogInterface dialog, int which){

                                        dialog.dismiss();
                                    }
                                });
                            }
                        }
                        else
                        {
                            builderOnline.setMessage("Comunicazione con il server fallita");

                            builderOnline.setPositiveButton("OK", new DialogInterface.OnClickListener(){

                                public void onClick(DialogInterface dialog, int which){

                                    dialog.dismiss();
                                }
                            });
                        }
                    }
                    else
                    {
                        builderOnline.setMessage("L'indirizzo IP immesso non è scritto in modo corretto");

                        builderOnline.setPositiveButton("OK", new DialogInterface.OnClickListener(){

                            public void onClick(DialogInterface dialog, int which){

                                dialog.dismiss();
                            }
                        });

                    }

                    AlertDialog alertDialogOnline = builderOnline.create();
                    alertDialogOnline.show();
                }
            }
        });


        // evento che si attiva alla pressione della pulsante per la richiesta dei permessi necessari per il bluetooth
        buttonBluetoothPerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // richiesta dei permessi per il bluetooth
                requestPermissionLocationForBluetooth();
            }
        });

        // in base alla versione di android sul dispositivo si stabilisce se abilitare o meno il pulsante per la richiesta dei permessi necessari per il bluetooth
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                buttonBluetoothPerm.setEnabled(true);
            else
                buttonBluetoothPerm.setEnabled(false);
        }
        else
            buttonBluetoothPerm.setEnabled(false);


        // all'apertura delle impostazioni si rimpostano gli elemnti allo stesso stato della chiusura precedente
        if (MainApplication.getOnlineMode())
        {
            checkBoxOffline.setChecked(false);

            if (ServerComunication.getIP() != null)
                editTextIP.setText(ServerComunication.getIP());
        }
        else
            checkBoxOffline.setChecked(true);

        // inizializzazione struttura file csv nodi e archi se non è stato già fatto
        if (CSVHandler.getFiles() == null)
            CSVHandler.createCSV(this);
    }

    protected void onStart() {
        super.onStart();

        // si imposta l'activity corrente
        MainApplication.setCurrentActivity(this);
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

    /**
     * Metodo che si occupa dell'attivazione del sistema di localizzazione del dispositivo
     * (questa funzionalità è necessaria per i dispositivi con installata una versione di Android
     * superiore alla 6.0, in quanto senza di essa non può funzionare il Bluetooth)
     */
    private void requestPermissionLocationForBluetooth() {

        // non ho il permesso della localizzazione e lo richiedo
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.i("activate","activate location");

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_LOCATION_FOR_BLUETOOTH);
        }
        else
            Toast.makeText(this, "Hai già i permessi richiesti per utilizzare il bluetooth", Toast.LENGTH_LONG).show();
    }

    // metodo che gestisce gli eventi da attivare al ritorno della richiesta dei permessi della localizzazione
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_LOCATION_FOR_BLUETOOTH: {

                // se il permesso è stato ottenuto disabilito il button per la richiesta
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.i("prova","guaranteed");
                    buttonBluetoothPerm.setEnabled(false);

                } else {

                    Log.i("prova","non guaranteed");
                }
            }

        }
    }
}
