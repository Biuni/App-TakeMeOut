package com.example.user.progetto_ids;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import app_library.MainApplication;
import app_library.comunication.ServerComunication;
import app_library.user.UserHandler;

/**
 * Created by User on 22/06/2018.
 */

// activity che rappresenta la home dell'applicazione
public class HomeActivity  extends AppCompatActivity {

    // costanti per le stringhe dei button
    public static final String STRING_BUTTON_LOGIN = "Login";
    public static final String STRING_BUTTON_LOGOUT = "Logout";
    public static final String STRING_BUTTON_CREATE_PROFILE = "Crea profilo";
    public static final String STRING_BUTTON_VIEW_PROFILE = "Vedi profilo";

    // codice per rilevare il risultato della chiusura delle impostazioni
    private static final int SETTING_ACTIVITY_REQUEST_CODE = 0;

    // elementi grafici
    private TextView textViewHomeUser;
    private ImageView imageViewHomeUser;
    private Button buttonHomeMap;
    private Button buttonHomeLoginLogout;
    private Button buttonHomeInscripViewProf;
    private ImageView imageViewHomeMap;
    private ImageView imageViewHomeLoginLogout;
    private ImageView imageViewHomeInscripViewProf;
    private ImageView imageViewHomeNoEditSetting;

    // booleano per indicare che le impostazioni sono state impostate almeno una volta
    private boolean settingOK;

    //codice utilizzato come risposta alla richiesta di attivazione della localizzazione sul dispositivo
    public static final int PERMISSIONS_REQUEST_LOCATION_FOR_BLUETOOTH = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // impostazione del titolo dell'activity
        getSupportActionBar().setTitle("Home");

        // recupero riferimenti elementi grafici
        textViewHomeUser = (TextView) findViewById(R.id.textViewHomeUser);
        imageViewHomeUser = (ImageView) findViewById(R.id.imageViewHomeUser);
        buttonHomeMap = (Button) findViewById(R.id.buttonHomeMap);
        buttonHomeLoginLogout = (Button) findViewById(R.id.buttonHomeLoginLogout);
        buttonHomeInscripViewProf = (Button) findViewById(R.id.buttonHomeInscripViewProf);
        imageViewHomeMap = (ImageView) findViewById(R.id.imageViewHomeMap);
        imageViewHomeLoginLogout = (ImageView) findViewById(R.id.imageViewHomeLoginLogout);
        imageViewHomeInscripViewProf = (ImageView) findViewById(R.id.imageViewHomeInscripViewProf);
        imageViewHomeNoEditSetting = (ImageView) findViewById(R.id.imageViewHomeNoEditSetting);

        // disabilitazione elementi grafici iniziale
        buttonHomeMap.setEnabled(false);
        buttonHomeMap.setVisibility(View.INVISIBLE);

        buttonHomeLoginLogout.setEnabled(false);
        buttonHomeLoginLogout.setVisibility(View.INVISIBLE);

        buttonHomeInscripViewProf.setEnabled(false);
        buttonHomeInscripViewProf.setVisibility(View.INVISIBLE);

        imageViewHomeMap.setEnabled(false);
        imageViewHomeMap.setVisibility(View.INVISIBLE);

        imageViewHomeLoginLogout.setEnabled(false);
        imageViewHomeLoginLogout.setVisibility(View.INVISIBLE);

        imageViewHomeInscripViewProf.setEnabled(false);
        imageViewHomeInscripViewProf.setVisibility(View.INVISIBLE);

        imageViewHomeNoEditSetting.setEnabled(true);
        imageViewHomeNoEditSetting.setVisibility(View.VISIBLE);


        // evento che si attiva al click del pulsante mappe
        buttonHomeMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // si avvia l'activity per l'impostazione della mappa
                Intent intentMapSett = new Intent (getApplicationContext(), MapSettingActivity.class);
                startActivity(intentMapSett);
            }
        });

        // evento che si attiva al click del pulsante login/logout
        buttonHomeLoginLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // comunicazione con il server ha successo
                if (ServerComunication.handShake(ServerComunication.getIP()))
                {
                    // in base alla scritta presente nel button si avvia il relativo metodo
                    // login
                    if (buttonHomeLoginLogout.getText().toString().equals(STRING_BUTTON_LOGIN))
                        login();
                    // logout
                    else
                        logout();
                }
                else
                    Toast.makeText(getApplicationContext(), "Comunicazione con il server fallita", Toast.LENGTH_LONG).show();

            }
        });

        // evento che si attiva al click del pulsante crea/vedi profilo
        buttonHomeInscripViewProf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // comunicazione con il server ha successo
                if (ServerComunication.handShake(ServerComunication.getIP()))
                {
                    // in base alla scritta presente nel button si apre la schermata del profilo in modalità visione o creazione profilo
                    // creazione profilo
                    if (buttonHomeInscripViewProf.getText().toString().equals(STRING_BUTTON_CREATE_PROFILE))
                        openProfileScreen(STRING_BUTTON_CREATE_PROFILE);
                    // visione profilo
                    else
                        openProfileScreen(STRING_BUTTON_VIEW_PROFILE);
                }
                else
                    Toast.makeText(getApplicationContext(), "Comunicazione con il server fallita", Toast.LENGTH_LONG).show();
            }
        });


        //nel caso in cui l'applicazione lavori su una versione di Android
        //superiore alla 6.0, per far funzionare il Bluetooth bisogna attivare la localizzazione
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestPermissionLocationForBluetooth();

        // recupero preferenze app
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        //passata a UserHandler un'istanza delle preferencies, in modo che vengano assegnate
        //eventuali informazioni li salvate, ad esso correlate (es. se già loggato vengono presi i suoi dati dalla memoria)
        UserHandler.setPref(prefer);

        //prima controlla se è già loggato o se ci sono sharedpreferencies
        //qualora l'utente è loggato viene scritto il nome e cognome dell'utente e mostrata l'immagine dell'utente loggato
        if(UserHandler.isLogged())
        {
            textViewHomeUser.setText(UserHandler.getNameSurname());
            imageViewHomeUser.setImageResource(R.drawable.home_user_logged);
        }
        // scritto guest e mostrata l'immagine dell'utente non loggato
        else
        {
            textViewHomeUser.setText("Guest");
            imageViewHomeUser.setImageResource(R.drawable.home_user_no_logged);
        }

        // impostazioni non ancora impostate
        settingOK = false;
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

    protected void onStop() {
        super.onStop();
    }

    public void onDestroy() {
        super.onDestroy();
    }


    // evento attivato alla pressione del tasto back
    @Override
    public void onBackPressed()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Sei sicuro di voler uscire dall'applicazione?").setCancelable(false).setTitle("Attenzione").setPositiveButton("SI", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                // le impostazioni sono state impostate
                if (settingOK)
                {
                    try
                    {
                        // l'applicazione si prepara per essere spenta
                        MainApplication.setIsFinishing(true);

                        // cancellazione posizione utente nel server
                        if(MainApplication.getOnlineMode() && UserHandler.isLogged())
                            ServerComunication.deleteUserPositionWithData(UserHandler.getUuid());

                        if (MainApplication.controlBluetooth())
                        {
                            HomeActivity.this.sendBroadcast(new Intent("SuspendScan"));

                            MainApplication.getScanner().suspendScan();
                        }
                        else
                            finish();
                    }
                    catch (Exception e)
                    {
                        finish();
                    }
                }
                else
                    finish();
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //do nothings
            }
        });

        AlertDialog alertDialogExit = builder.create();
        alertDialogExit.show();
    }

    // metodo per eseguire la login vera e propria
    private void login()
    {
        final Dialog loginDialog = new Dialog(this);
        loginDialog.setContentView(R.layout.login);

        // inizializzati i bottoni del dialog
        Button buttonLogin = (Button) loginDialog.findViewById(R.id.buttonLoginLogin);
        Button buttonCancel = (Button) loginDialog.findViewById(R.id.buttonLoginCancel);

        // elementi input dialog
        final EditText editTextMail = (EditText) loginDialog.findViewById(R.id.editTextLoginMail);
        final EditText editTextPassw = (EditText) loginDialog.findViewById(R.id.editTextLoginPassword);
        final CheckBox checkBoxRemember = (CheckBox) loginDialog.findViewById(R.id.checkBoxLoginRemember);

        // click su pulsante login
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean loginCompleted = false;

                if(!editTextMail.getText().toString().equals("") && !editTextPassw.getText().toString().equals(""))
                {
                    // invio della richiesta di login al server
                    String loginResultString = UserHandler.login(editTextMail.getText().toString(), editTextPassw.getText().toString(), checkBoxRemember.isChecked());

                    // login completato con successo
                    if (loginResultString.equals("OK"))
                        loginCompleted = true;
                }

                // se il login è stato completato con successo si aggiornano gli elementi grafici
                if (loginCompleted)
                {
                    updateDisplayHome(SettingActivity.RETURN_STATE_ONLINE_LOGGED);
                    Toast.makeText(getApplicationContext(), "Benvenuto " + UserHandler.getNameSurname(),Toast.LENGTH_LONG).show();
                }
                // visualizzato quando vengono immessi dati sbagliati nella login
                else
                    Toast.makeText(getApplicationContext(), "Email e/o password non corretti",Toast.LENGTH_LONG).show();

                loginDialog.dismiss();

            }
        });

        // click su pulsante annulla
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loginDialog.dismiss();
            }
        });

        // mostrata la dialog
        loginDialog.show();
    }

    // metodo per eseguire la logout vera e propria
    private void logout()
    {
        UserHandler.logout();

        // aggiornamento degli elementi grafici
        updateDisplayHome(SettingActivity.RETURN_STATE_ONLINE_NOT_LOGGED);
        Toast.makeText(getApplicationContext(), "Ti sei disconnesso",Toast.LENGTH_LONG).show();
    }

    // apertura dell'activity del profilo per la creazione o visione del profilo
    private void openProfileScreen(String profileMode)
    {
        Intent intentProfile = new Intent (getApplicationContext(), ProfileActivity.class);

        // passata la tipologia di schermata di profilo da visualizzare
        intentProfile.putExtra("profileMode", profileMode);
        startActivity(intentProfile);
    }

    // creazione del menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_setting, menu);
        return true;
    }

    // eventi attivati al click sugli elementi del menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            // elemento setting e si apre l'activity delle impostazioni
            case R.id.menu_setting:

                Intent intent = new Intent(this, SettingActivity.class);
                startActivityForResult(intent, SETTING_ACTIVITY_REQUEST_CODE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // metodo che permette di gestire gli eventi da svolgere nella home al ritorno da activity aperte dalla home
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // ritorno dalle impostazioni
        if (requestCode == SETTING_ACTIVITY_REQUEST_CODE) {

            // impostazioni settate correttamente
            if (resultCode == RESULT_OK) {

                // è la prima volta che si ritorna dalle impostazioni
                if (!settingOK)
                {
                    // si inizializza gli elementi della classe di supporto
                    MainApplication.start(this);
                    settingOK = true;

                    // si rimuovono gli elementi visualizzati all'apertura dell'app che indicano di andare nelle impostazioni
                    imageViewHomeNoEditSetting.setVisibility(View.GONE);
                    imageViewHomeNoEditSetting.setEnabled(false);
                }

                // stringa extra di ritorno dalle impostazioni che permette di aggiornare opportunamente la vista della home
                String returnStateString = data.getStringExtra("settingResult");

                updateDisplayHome(returnStateString);
            }
        }
    }

    // metodo che aggiorna opportunamente la vista della home dopo essere ritornati dalle impostazioni
    // vi sono 4 modalità: utente offline loggato, offline non loggato, online loggato e online non loggato
    private void updateDisplayHome(String state)
    {

        buttonHomeMap.setVisibility(View.VISIBLE);
        buttonHomeMap.setEnabled(true);

        imageViewHomeMap.setVisibility(View.VISIBLE);
        imageViewHomeMap.setEnabled(true);

        // utente offline sia loggato che non
        if (state.equals(SettingActivity.RETURN_STATE_OFFLINE_LOGGED) || state.equals(SettingActivity.RETURN_STATE_OFFLINE_NOT_LOGGED))
        {
            buttonHomeLoginLogout.setVisibility(View.INVISIBLE);
            buttonHomeLoginLogout.setEnabled(false);

            imageViewHomeLoginLogout.setVisibility(View.INVISIBLE);
            imageViewHomeLoginLogout.setEnabled(false);

            buttonHomeInscripViewProf.setVisibility(View.INVISIBLE);
            buttonHomeInscripViewProf.setEnabled(false);

            imageViewHomeInscripViewProf.setVisibility(View.INVISIBLE);
            imageViewHomeInscripViewProf.setEnabled(false);

            // utente offline loggato
            if (state.equals(SettingActivity.RETURN_STATE_OFFLINE_LOGGED))
            {
                textViewHomeUser.setText(UserHandler.getNameSurname());
                imageViewHomeUser.setImageResource(R.drawable.home_user_logged);
            }
            // utente offline non loggato
            else
            {
                textViewHomeUser.setText("Guest");
                imageViewHomeUser.setImageResource(R.drawable.home_user_no_logged);
            }

        }
        // state.equals(SettingActivity.RETURN_STATE_ONLINE_LOGGED) || state.equals(SettingActivity.RETURN_STATE_ONLINE_NOT_LOGGED)
        // utente online sia loggato che non
        else
        {
            // utente online loggato
            if (state.equals(SettingActivity.RETURN_STATE_ONLINE_LOGGED))
            {
                textViewHomeUser.setText(UserHandler.getNameSurname());
                imageViewHomeUser.setImageResource(R.drawable.home_user_logged);

                buttonHomeLoginLogout.setText(STRING_BUTTON_LOGOUT);
                buttonHomeInscripViewProf.setText(STRING_BUTTON_VIEW_PROFILE);

                imageViewHomeInscripViewProf.setImageResource(R.drawable.home_view_profile);
            }
            // utente online non loggato
            else
            {
                textViewHomeUser.setText("Guest");
                imageViewHomeUser.setImageResource(R.drawable.home_user_no_logged);

                buttonHomeLoginLogout.setText(STRING_BUTTON_LOGIN);
                buttonHomeInscripViewProf.setText(STRING_BUTTON_CREATE_PROFILE);

                imageViewHomeInscripViewProf.setImageResource(R.drawable.home_create_profile);
            }

            buttonHomeLoginLogout.setVisibility(View.VISIBLE);
            buttonHomeLoginLogout.setEnabled(true);

            imageViewHomeLoginLogout.setVisibility(View.VISIBLE);
            imageViewHomeLoginLogout.setEnabled(true);

            buttonHomeInscripViewProf.setVisibility(View.VISIBLE);
            buttonHomeInscripViewProf.setEnabled(true);

            imageViewHomeInscripViewProf.setVisibility(View.VISIBLE);
            imageViewHomeInscripViewProf.setEnabled(true);
        }
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
    }

    // metodo che gestisce gli eventi da attivare al ritorno della richiesta dei permessi della localizzazione
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_LOCATION_FOR_BLUETOOTH: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.i("prova","guaranteed");

                } else {

                    Log.i("prova","non guaranteed");
                }
            }

        }
    }
}
