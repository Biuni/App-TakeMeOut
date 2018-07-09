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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import app_library.MainApplication;
import app_library.comunication.Message;
import app_library.comunication.ServerComunication;
import app_library.dijkstra.Dijkstra;
import app_library.dijkstra.Edge;
import app_library.dijkstra.Vertex;
import app_library.user.UserHandler;

/**
 * Created by User on 22/06/2018.
 */

public class HomeActivity  extends AppCompatActivity {

    public static final String STRING_BUTTON_LOGIN = "Login";
    public static final String STRING_BUTTON_LOGOUT = "Logout";
    public static final String STRING_BUTTON_CREATE_PROFILE = "Crea profilo";
    public static final String STRING_BUTTON_VIEW_PROFILE = "Vedi profilo";
    private static final int SETTING_ACTIVITY_REQUEST_CODE = 0;
    private TextView textViewHomeUser;
    private ImageView imageViewHomeUser;
    private Button buttonHomeMap;
    private Button buttonHomeLoginLogout;
    private Button buttonHomeInscripViewProf;
    private ImageView imageViewHomeMap;
    private ImageView imageViewHomeLoginLogout;
    private ImageView imageViewHomeInscripViewProf;
    private ImageView imageViewHomeNoEditSetting;

    //memorizza alcune informazioni nella memoria interna del dispositivo, per renderle persistenti
    //private SharedPreferences prefer;

    private boolean settingOK;

    //codice utilizzato come risposta alla richiesta di attivazione della localizzazione sul dispositivo
    public static final int PERMISSIONS_REQUEST_LOCATION_FOR_BLUETOOTH = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        getSupportActionBar().setTitle("Home");

        textViewHomeUser = (TextView) findViewById(R.id.textViewHomeUser);
        imageViewHomeUser = (ImageView) findViewById(R.id.imageViewHomeUser);
        buttonHomeMap = (Button) findViewById(R.id.buttonHomeMap);
        buttonHomeLoginLogout = (Button) findViewById(R.id.buttonHomeLoginLogout);
        buttonHomeInscripViewProf = (Button) findViewById(R.id.buttonHomeInscripViewProf);
        imageViewHomeMap = (ImageView) findViewById(R.id.imageViewHomeMap);
        imageViewHomeLoginLogout = (ImageView) findViewById(R.id.imageViewHomeLoginLogout);
        imageViewHomeInscripViewProf = (ImageView) findViewById(R.id.imageViewHomeInscripViewProf);
        imageViewHomeNoEditSetting = (ImageView) findViewById(R.id.imageViewHomeNoEditSetting);

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


        buttonHomeMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intentMapSett = new Intent (getApplicationContext(), MapSettingActivity.class);
                startActivity(intentMapSett);
            }
        });

        buttonHomeLoginLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ServerComunication.handShake(ServerComunication.getIP()))
                {
                    if (buttonHomeLoginLogout.getText().toString().equals(STRING_BUTTON_LOGIN))
                        login();
                    else
                        logout();
                }
                else
                    Toast.makeText(getApplicationContext(), "Comunicazione con il server fallita", Toast.LENGTH_LONG).show();

            }
        });

        buttonHomeInscripViewProf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (ServerComunication.handShake(ServerComunication.getIP()))
                {
                    if (buttonHomeInscripViewProf.getText().toString().equals(STRING_BUTTON_CREATE_PROFILE))
                        openProfileScreen(STRING_BUTTON_CREATE_PROFILE);
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

        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        //passata a UserHandler un'istanza delle preferencies, in modo che vengano assegnate
        //eventuali informazioni li salvate, ad esso correlate (es. se già loggato vengono presi i suoi dati dalla memoria)
        UserHandler.setPref(prefer);

        //prima controlla se è già loggato o se ci sono sharedpreferencies
        //qualora il primo caso sia verificato viene scritto il nome utente e mostrata l'immagine dell'utente loggato
        if(UserHandler.isLogged())
        {
            textViewHomeUser.setText(UserHandler.getNameSurname());
            imageViewHomeUser.setImageResource(R.drawable.home_user_logged);
        }
        else
        {
            textViewHomeUser.setText("Guest");
            imageViewHomeUser.setImageResource(R.drawable.home_user_no_logged);
        }

        settingOK = false;
    }

    protected void onStart() {
        super.onStart();
        MainApplication.setCurrentActivity(this);
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

    }

    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onBackPressed()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Sei sicuro di voler uscire dall'applicazione?").setCancelable(false).setTitle("Attenzione").setPositiveButton("SI", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if (settingOK)
                {
                    // l'applicazione si prepara per essere spenta
                    MainApplication.setIsFinishing(true);

                    HomeActivity.this.sendBroadcast(new Intent("SuspendScan"));

                    MainApplication.getScanner().suspendScan();

                    if(MainApplication.getOnlineMode() && UserHandler.isLogged())
                        ServerComunication.deleteUserPositionWithData(UserHandler.getUuid());
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

    private void login()
    {
        final Dialog loginDialog = new Dialog(this);
        loginDialog.setContentView(R.layout.login);
        //loginDialog.setTitle("Login");

        // inizializzati i bottoni del dialog
        Button buttonLogin = (Button) loginDialog.findViewById(R.id.buttonLoginLogin);
        Button buttonCancel = (Button) loginDialog.findViewById(R.id.buttonLoginCancel);

        // elementi input dialog
        final EditText editTextMail = (EditText) loginDialog.findViewById(R.id.editTextLoginMail);
        final EditText editTextPassw = (EditText) loginDialog.findViewById(R.id.editTextLoginPassword);
        final CheckBox checkBoxRemember = (CheckBox) loginDialog.findViewById(R.id.checkBoxLoginRemember);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean loginCompleted = false;

                if(!editTextMail.getText().toString().equals("") && !editTextPassw.getText().toString().equals(""))
                {
                    String loginResultString = UserHandler.login(editTextMail.getText().toString(), editTextPassw.getText().toString(), checkBoxRemember.isChecked());

                    if (loginResultString.equals("OK"))
                        loginCompleted = true;
                }

                if (loginCompleted)
                {
                    updateDisplayHome(SettingActivity.RETURN_STATE_ONLINE_LOGGED);
                    Toast.makeText(getApplicationContext(), "Benvenuto " + UserHandler.getNameSurname(),Toast.LENGTH_LONG).show();
                }
                // visualizzato quando vengono immessi dati sbagliati nella login
                else
                    Toast.makeText(getApplicationContext(), "Email e/o password non corretti",Toast.LENGTH_LONG).show();

                loginDialog.dismiss();


                /*AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                    builder.setMessage("Login non completato").setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // do nothings
                                }
                            });

                    AlertDialog alertDialogLoginError = builder.create();
                    alertDialogLoginError.show();*/

            }
        });


        // prova chiamata server funzionante
        /*buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean loginCompleted = false;

                if(editTextEmail.getText().toString().equals("")||editTextPassw.getText().toString().equals(""))
                    loginCompleted = false;


                String s = " ";
                ArrayList<String> key = new ArrayList<String>();
                key.add("status");
                key.add("message");
                key.add("result");

                try {
                    s = ServerComunication.login(editTextEmail.getText().toString(), editTextPassw.getText().toString());
                    s = Message.JSONToKeyValue(s, key).get("result");

                    JSONObject object = new JSONObject(s);
                    s = object.getJSONArray("results").getJSONObject(0).getString("mail");

                    textViewHomeUser.setText(s);

                }
                catch (Exception e)
                {

                }




                loginDialog.dismiss();
            }
        });*/

        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loginDialog.dismiss();
            }
        });

        loginDialog.show();
    }

    private void logout()
    {
        UserHandler.logout();
        updateDisplayHome(SettingActivity.RETURN_STATE_ONLINE_NOT_LOGGED);
        Toast.makeText(getApplicationContext(), "Ti sei disconnesso",Toast.LENGTH_LONG).show();
    }

    private void openProfileScreen(String profileMode)
    {
        Intent intentProfile = new Intent (getApplicationContext(), ProfileActivity.class);
        intentProfile.putExtra("profileMode", profileMode);
        startActivity(intentProfile);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_setting:

                Intent intent = new Intent(this, SettingActivity.class);
                startActivityForResult(intent, SETTING_ACTIVITY_REQUEST_CODE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SETTING_ACTIVITY_REQUEST_CODE) {

            if (resultCode == RESULT_OK) {

                if (!settingOK)
                {
                    MainApplication.start(this);
                    settingOK = true;
                    imageViewHomeNoEditSetting.setVisibility(View.GONE);
                    imageViewHomeNoEditSetting.setEnabled(false);
                }

                String returnStateString = data.getStringExtra("settingResult");

                updateDisplayHome(returnStateString);
            }
        }
    }

    private void updateDisplayHome(String state)
    {
        /*textViewHomeUser.setVisibility(View.VISIBLE);
        textViewHomeUser.setEnabled(true);

        imageViewHomeUser.setVisibility(View.VISIBLE);
        imageViewHomeUser.setEnabled(true);*/

        buttonHomeMap.setVisibility(View.VISIBLE);
        buttonHomeMap.setEnabled(true);

        imageViewHomeMap.setVisibility(View.VISIBLE);
        imageViewHomeMap.setEnabled(true);

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

            if (state.equals(SettingActivity.RETURN_STATE_OFFLINE_LOGGED))
            {
                textViewHomeUser.setText(UserHandler.getNameSurname());
                imageViewHomeUser.setImageResource(R.drawable.home_user_logged);
            }
            else
            {
                textViewHomeUser.setText("Guest");
                imageViewHomeUser.setImageResource(R.drawable.home_user_no_logged);
            }

        }
        // state.equals(SettingActivity.RETURN_STATE_ONLINE_LOGGED) || state.equals(SettingActivity.RETURN_STATE_ONLINE_NOT_LOGGED)
        else
        {
            if (state.equals(SettingActivity.RETURN_STATE_ONLINE_LOGGED))
            {
                textViewHomeUser.setText(UserHandler.getNameSurname());
                imageViewHomeUser.setImageResource(R.drawable.home_user_logged);

                buttonHomeLoginLogout.setText(STRING_BUTTON_LOGOUT);
                buttonHomeInscripViewProf.setText(STRING_BUTTON_VIEW_PROFILE);

                imageViewHomeInscripViewProf.setImageResource(R.drawable.home_view_profile);
            }
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

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.i("activate","activate location");

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_LOCATION_FOR_BLUETOOTH);

        }
    }

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
