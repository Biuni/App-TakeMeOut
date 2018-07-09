package com.example.user.progetto_ids;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
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

public class SettingActivity  extends AppCompatActivity {

    public static final String RETURN_STATE_OFFLINE_LOGGED = "Offline_logged_OK";
    public static final String RETURN_STATE_OFFLINE_NOT_LOGGED = "Offline_nologged_OK";
    public static final String RETURN_STATE_ONLINE_LOGGED = "Online_logged_OK";
    public static final String RETURN_STATE_ONLINE_NOT_LOGGED = "Online_nologged_OK";
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

        getSupportActionBar().setTitle("Impostazioni");

        checkBoxOffline = (CheckBox) findViewById(R.id.checkBoxSettingOffline);
        textViewIP = (TextView) findViewById(R.id.textViewSettingIPServer);
        editTextIP = (EditText) findViewById(R.id.editTextSettingIPServer);
        buttonSaveEdit = (Button) findViewById(R.id.buttonSettingSaveEdit);
        buttonBluetoothPerm = (Button) findViewById(R.id.buttonSettingBluetoothPerm);

        checkBoxOffline.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

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


        buttonSaveEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // offline
                if (checkBoxOffline.isChecked())
                {
                    AlertDialog.Builder builderOffline = new AlertDialog.Builder(SettingActivity.this);
                    builderOffline.setTitle("Offline");
                    builderOffline.setCancelable(false);

                    if(CSVHandler.csvContainsElements(CSVHandler.getFiles().get(CSVHandler.FILE_NODE)) && CSVHandler.csvContainsElements(CSVHandler.getFiles().get(CSVHandler.FILE_ROUTE)))
                    {
                        //creata struttura dati legata ai nodi dell'edificio, leggendo dal file salvato in memoria intera
                        ArrayList<String[]> nodeList = CSVHandler.readCSV(CSVHandler.FILE_NODE, getBaseContext());
                        MainApplication.loadNode(nodeList);

                        MainApplication.setOnlineMode(false);

                        builderOffline.setMessage("Hai tutte le informazioni necessarie per utilizzare la modalità offline");

                        builderOffline.setPositiveButton("OK", new DialogInterface.OnClickListener(){

                            public void onClick(DialogInterface dialog, int which){

                                dialog.dismiss();

                                String returnString;

                                if (UserHandler.isLogged())
                                    returnString = RETURN_STATE_OFFLINE_LOGGED;
                                else
                                    returnString = RETURN_STATE_OFFLINE_NOT_LOGGED;

                                Intent returnIntent = new Intent();
                                returnIntent.putExtra("settingResult", returnString);
                                setResult(Activity.RESULT_OK,returnIntent);
                                finish();
                            }
                        });
                    }
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

                    if (FormControl.ipControl(ipInserted))
                    {
                        if (ServerComunication.handShake(ipInserted))
                        {
                            ServerComunication.setHostMaster(ipInserted);

                            String serverJSONDataResponse = ServerComunication.getJSONData();

                            if (!serverJSONDataResponse.equals(""))
                            {
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

                                if (hashMapsNode != null && hashMapsRoute != null)
                                {
                                    boolean updatedCSVCompleted = false;

                                    try
                                    {
                                        CSVHandler.updateCSV(hashMapsNode, getBaseContext(), CSVHandler.FILE_NODE);
                                        CSVHandler.updateCSV(hashMapsRoute, getBaseContext(), CSVHandler.FILE_ROUTE);

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

                                    if (updatedCSVCompleted)
                                    {
                                        //creata struttura dati legata ai nodi dell'edificio, leggendo dal file salvato in memoria intera
                                        ArrayList<String[]> nodeList = CSVHandler.readCSV(CSVHandler.FILE_NODE, getBaseContext());
                                        MainApplication.loadNode(nodeList);

                                        MainApplication.setOnlineMode(true);

                                        builderOnline.setMessage("La comunicazione con il server è avvenuta con successo e sono state scaricate le mappe aggiornate");

                                        builderOnline.setPositiveButton("OK", new DialogInterface.OnClickListener(){

                                            public void onClick(DialogInterface dialog, int which){

                                                dialog.dismiss();

                                                String returnString;

                                                if (UserHandler.isLogged())
                                                    returnString = RETURN_STATE_ONLINE_LOGGED;
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


        buttonBluetoothPerm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                requestPermissionLocationForBluetooth();
            }
        });

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                buttonBluetoothPerm.setEnabled(true);
            else
                buttonBluetoothPerm.setEnabled(false);
        }
        else
            buttonBluetoothPerm.setEnabled(false);


        if (MainApplication.getOnlineMode())
        {
            checkBoxOffline.setChecked(false);

            if (ServerComunication.getIP() != null)
                editTextIP.setText(ServerComunication.getIP());
        }
        else
            checkBoxOffline.setChecked(true);

        MainApplication.setCurrentActivity(this);

        if (CSVHandler.getFiles() == null)
            CSVHandler.createCSV(this);
    }

    protected void onResume() {
        super.onResume();
        MainApplication.setVisible(true);
    }

    protected void onPause() {
        super.onPause();
        MainApplication.setVisible(false);
    }

    /**
     * Metodo che si occupa dell'attivazione del sistema di localizzazione del dispositivo
     * (questa funzionalità è necessaria per i dispositivi con installata una versione di Android
     * superiore alla 6.0, in quanto senza di essa non può funzionare il Bluetooth)
     */
    private void requestPermissionLocationForBluetooth() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.i("activate","activate location");

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_LOCATION_FOR_BLUETOOTH);
        }
        else
            Toast.makeText(this, "Hai già i permessi richiesti per utilizzare il bluetooth", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_LOCATION_FOR_BLUETOOTH: {

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
