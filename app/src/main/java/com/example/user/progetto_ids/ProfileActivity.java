package com.example.user.progetto_ids;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;

import app_library.MainApplication;
import app_library.user.UserHandler;
import app_library.user.UserProfile;
import app_library.validation.FormControl;

/**
 * Created by User on 23/06/2018.
 */

// activity per la creazione o visione del profilo
public class ProfileActivity extends AppCompatActivity {

    // hasmap con i riferimenti agli elementi grafici
    private HashMap<String,TextView> hashMapProfileField;

    // dialog per mostrare gli errori all'utente
    private AlertDialog alertDialogMessage;

    // button per confermare le modifiche
    private Button buttonSend;

    // array per tenere conto dei campi vuoti o sbagliati
    private boolean[] arrayEmptyField;
    private boolean[] arrayErrorField;

    // messaggi di informazione o errore mostrati all'utente
    public static final String[] USER_MESSAGE = {"Almeno un campo è vuoto", "Almeno un campo non è corretto", "Password non corrispondenti", "L'email è posseduta da un altro utente", "Errore nel recuperare le informazioni del profilo"};
    public static final String[] ERROR_FIELD_MESSAGE = {"Email non valida (Es. prova@prova.com)", "La password deve avere almeno 8 caratteri", "Il nome utente deve avere solo lettere e spazi"};

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        hashMapProfileField = new HashMap<String, TextView>();

        arrayEmptyField = new boolean[4];
        arrayErrorField = new boolean[4];

        // inizialmente si mettono tutti i campi vuoti e nessun errore
        for (int i = 0; i < 4; i++)
        {
            arrayEmptyField[i] = true;
            arrayErrorField[i] = false;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("").setCancelable(false).setTitle("Errore").setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //do nothings
            }
        });

        alertDialogMessage = builder.create();

        // si recupera l'extra dell'intent per vedere la modalità del profilo (creazione o visione)
        String profileMode = getIntent().getStringExtra("profileMode");

        // visione del profilo
        if (profileMode.equals(HomeActivity.STRING_BUTTON_VIEW_PROFILE))
        {
            // impostazione del titolo dell'activity a vedi profilo
            getSupportActionBar().setTitle("Vedi profilo");

            // recupero layout vedi profilo
            setContentView(R.layout.view_profile);

            // inizializzazione hasmap elementi grafici per la visione
            initializeHashMapProfileField(profileMode);

            // si mostrano le informazioni del profilo dell'utente nell'activity
            initializeViewProfileField();
        }
        // profileMode.equals(HomeActivity.STRING_BUTTON_CREATE_PROFILE)
        // creazione del profilo
        else
        {
            // impostazione del titolo dell'activity a crea profilo
            getSupportActionBar().setTitle("Crea profilo");

            // recupero layout crea profilo
            setContentView(R.layout.create_profile);

            // inizializzazione hasmap elementi grafici per la creazione
            initializeHashMapProfileField(profileMode);

            // si avviano i controlli sui campi inserity nell'activity
            startControlProfileFieldCreate();
        }
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

    // inizializzazione hasmap elementi grafici per la visione o la creazione del profilo in base al parametro di input
    private void initializeHashMapProfileField(String profileMode)
    {
        // creazione del profilo
        if (profileMode.equals(HomeActivity.STRING_BUTTON_CREATE_PROFILE))
        {
            TextView textViewCurrent;

            textViewCurrent = (TextView) findViewById(R.id.editTextCreateProfMail);
            hashMapProfileField.put("mail", textViewCurrent);

            textViewCurrent.setFocusable(true);

            textViewCurrent = (TextView) findViewById(R.id.editTextCreateProfPassw);
            hashMapProfileField.put("password", textViewCurrent);

            textViewCurrent = (TextView) findViewById(R.id.editTextCreateProfRepPassw);
            hashMapProfileField.put("password_repeat", textViewCurrent);

            textViewCurrent = (TextView) findViewById(R.id.editTextCreateProfNameSurname);
            hashMapProfileField.put("name_surname", textViewCurrent);

            buttonSend = (Button) findViewById(R.id.buttonCreateProfSend);
        }
        // profileMode.equals(HomeActivity.STRING_BUTTON_VIEW_PROFILE)
        // visione del profilo
        else
        {
            TextView textViewCurrent;

            textViewCurrent = (TextView) findViewById(R.id.textViewVProfMail);
            hashMapProfileField.put("mail", textViewCurrent);

            textViewCurrent = (TextView) findViewById(R.id.textViewVProfNameSurname);
            hashMapProfileField.put("name_surname", textViewCurrent);

            textViewCurrent = (TextView) findViewById(R.id.textViewVProfUuid);
            hashMapProfileField.put("uuid", textViewCurrent);
        }

    }

    // metodo per avviare i controlli sui campi inserity nell'activity
    private void startControlProfileFieldCreate()
    {
        // colore per campo vuoto
        final int colorEmpty = Color.rgb(230,230,230);

        // colore per campo sbagliato
        final int colorError = Color.RED;

        // colore per campo corretto
        final int colorCorrect = Color.GREEN;

        // al cambio di focus sul campo mail si controlla se il campo è vuoto, sbagliato o corretto mostrando il risultato all'utente
        hashMapProfileField.get("mail").setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if( hashMapProfileField.get("mail").getText().toString().isEmpty()) {
                    hashMapProfileField.get("mail").setBackgroundColor(colorEmpty);
                    arrayEmptyField[0] = true;

                    // se il campo è vuoto non presenta contemporaneamente errori
                    arrayErrorField[0] = false;
                }else if (!FormControl.mailControl(hashMapProfileField.get("mail").getText().toString())) {

                    if (!arrayErrorField[0])
                    {
                        hashMapProfileField.get("mail").setBackgroundColor(colorError);
                        arrayErrorField[0] = true;

                        alertDialogMessage.setMessage(ERROR_FIELD_MESSAGE[0]);
                        alertDialogMessage.show();
                    }

                }else{
                    hashMapProfileField.get("mail").setBackgroundColor(colorCorrect);
                    arrayEmptyField[0] = false;
                    arrayErrorField[0] = false;
                }
            }
        });

        // al cambio di focus sul campo password si controlla se il campo è vuoto, sbagliato o corretto mostrando il risultato all'utente
        hashMapProfileField.get("password").setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if( hashMapProfileField.get("password").getText().toString().isEmpty()){
                    hashMapProfileField.get("password").setBackgroundColor(colorEmpty);
                    arrayEmptyField[1] = true;

                    // se il campo è vuoto non presenta contemporaneamente errori
                    arrayErrorField[1] = false;
                }else if (!FormControl.passwordControl(hashMapProfileField.get("password").getText().toString())){

                    if (!arrayErrorField[1])
                    {
                        hashMapProfileField.get("password").setBackgroundColor(colorError);
                        arrayErrorField[1] = true;

                        alertDialogMessage.setMessage(ERROR_FIELD_MESSAGE[1]);
                        alertDialogMessage.show();
                    }

                }else{
                    hashMapProfileField.get("password").setBackgroundColor(colorCorrect);
                    arrayEmptyField[1] = false;
                    arrayErrorField[1] = false;
                }
            }
        });

        // al cambio di focus sul campo ripeti password si controlla se il campo è vuoto, sbagliato o corretto mostrando il risultato all'utente
        hashMapProfileField.get("password_repeat").setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if( hashMapProfileField.get("password_repeat").getText().toString().isEmpty()){
                    hashMapProfileField.get("password_repeat").setBackgroundColor(colorEmpty);
                    arrayEmptyField[2] = true;

                    // se il campo è vuoto non presenta contemporaneamente errori
                    arrayErrorField[2] = false;
                }else if (hashMapProfileField.get("password_repeat").getText().toString().compareTo(hashMapProfileField.get("password").getText().toString())!=0) {

                    if (!arrayErrorField[2])
                    {
                        hashMapProfileField.get("password_repeat").setBackgroundColor(colorError);
                        arrayErrorField[2] = true;

                        alertDialogMessage.setMessage(USER_MESSAGE[2]);
                        alertDialogMessage.show();
                    }

                }else{
                    hashMapProfileField.get("password_repeat").setBackgroundColor(colorCorrect);
                    arrayEmptyField[2] = false;
                    arrayErrorField[2] = false;
                }
            }
        });

        // al cambio di focus sul campo nome utente si controlla se il campo è vuoto, sbagliato o corretto mostrando il risultato all'utente
        hashMapProfileField.get("name_surname").setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //controlla che sia composto solo da lettere e spazi
                if( hashMapProfileField.get("name_surname").getText().toString().isEmpty()){
                    hashMapProfileField.get("name_surname").setBackgroundColor(colorEmpty);
                    arrayEmptyField[3] = true;

                    // se il campo è vuoto non presenta contemporaneamente errori
                    arrayErrorField[3] = false;
                }else if (!FormControl.letterControl(hashMapProfileField.get("name_surname").getText().toString())) {

                    if (!arrayErrorField[3])
                    {
                        hashMapProfileField.get("name_surname").setBackgroundColor(colorError);
                        arrayErrorField[3] = true;

                        alertDialogMessage.setMessage(ERROR_FIELD_MESSAGE[2]);
                        alertDialogMessage.show();
                    }

                }else{
                    hashMapProfileField.get("name_surname").setBackgroundColor(colorCorrect);
                    arrayEmptyField[3] = false;
                    arrayErrorField[3] = false;
                }
            }
        });


        // evento che si attiva alla pressione del button conferma modifiche
        buttonSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                buttonSend.setFocusable(true);
                buttonSend.setFocusableInTouchMode(true);
                buttonSend.requestFocus();

                // si va a creare il profilo
                createProfile();
            }
        });
    }


    // metodo per la creazione del profilo
    private void createProfile()
    {
        // booleano per campo non corretto
        boolean wrongField = false;

        //controllo campi errati
        for(int i = 0; i < 4 && !wrongField; i++){

            if(arrayErrorField[i]){
                wrongField = true;
                alertDialogMessage.setMessage(USER_MESSAGE[1]);
            }
        }

        // controllo campi vuoti
        for(int i = 0; i < 4 && !wrongField; i++) {

            if(arrayEmptyField[i]) {
                wrongField = true;
                alertDialogMessage.setMessage(USER_MESSAGE[0]);
            }
        }

        // controllo coincidenza password
        if(!wrongField && !hashMapProfileField.get("password").getText().toString().equals(hashMapProfileField.get("password_repeat").getText().toString()))
        {
            wrongField = true;
            alertDialogMessage.setMessage(USER_MESSAGE[2]);
        }

        // se uno dei controlli precedenti non è stato superato si mostra un messaggio all'utente
        if(wrongField)
            alertDialogMessage.show();
        else
        {
            HashMap<String, String> insertedData = new HashMap<>();

            // si recuperano i dati inserity nei campi
            insertedData.put("mail", hashMapProfileField.get("mail").getText().toString());
            insertedData.put("pwd", hashMapProfileField.get("password").getText().toString());
            insertedData.put("name", hashMapProfileField.get("name_surname").getText().toString());

            // invio della richiesta di registrazione al server
            String serverRegisterResponse = UserHandler.userRegister(insertedData);

            // registrazione completata con successo
            if(serverRegisterResponse.equals("OK"))
            {
                Toast.makeText(getApplicationContext(),"Creazione profilo completata",Toast.LENGTH_LONG).show();

                // si chiude la schermata del profilo ritornando alla home
                finish();
            }
            else
            {
                // si mostra il messaggio di errore all'utente
                alertDialogMessage.setMessage(USER_MESSAGE[3]);
                alertDialogMessage.show();
            }
        }

    }

    // si mostrano le informazioni del profilo dell'utente nell'activity di visione del profilo
    private void initializeViewProfileField()
    {
        // invio della richiesta per ottenere le informazioni sul profilo al server
        UserProfile profile = UserHandler.getProfile(UserHandler.getUuid());

        // informazioni recuperate con successo e vengono mostrate
        if (profile != null)
        {
            hashMapProfileField.get("mail").setText(profile.getMail());
            hashMapProfileField.get("name_surname").setText(profile.getNameSurname());
            hashMapProfileField.get("uuid").setText(profile.getUuid());
        }
        // si mostra il messaggio di errore all'utente
        else
        {
            hashMapProfileField.get("mail").setText("");
            hashMapProfileField.get("name_surname").setText("");
            hashMapProfileField.get("uuid").setText("");

            alertDialogMessage.setMessage(USER_MESSAGE[4]);
            alertDialogMessage.show();
        }
    }

}
