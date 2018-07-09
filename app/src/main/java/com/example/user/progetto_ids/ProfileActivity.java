package com.example.user.progetto_ids;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.HashMap;

import app_library.MainApplication;
import app_library.user.UserHandler;
import app_library.user.UserProfile;
import app_library.validation.FormControl;

/**
 * Created by User on 23/06/2018.
 */

public class ProfileActivity extends AppCompatActivity {

    private HashMap<String,TextView> hashMapProfileField;
    private AlertDialog alertDialogMessage;
    private Button buttonSend;

    private boolean[] arrayEmptyField;
    private boolean[] arrayErrorField;

    public static final String[] USER_MESSAGE = {"Almeno un campo è vuoto", "Almeno un campo non è corretto", "Password non corrispondenti", "L'email è posseduta da un altro utente", "Errore nel recuperare le informazioni del profilo"};
    public static final String[] ERROR_FIELD_MESSAGE = {"Email non valida (Es. prova@prova.com)", "La password deve avere almeno 8 caratteri", "Il nome utente deve avere solo lettere e spazi"};

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        hashMapProfileField = new HashMap<String, TextView>();

        arrayEmptyField = new boolean[4];
        arrayErrorField = new boolean[4];

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

        String profileMode = getIntent().getStringExtra("profileMode");

        if (profileMode.equals(HomeActivity.STRING_BUTTON_VIEW_PROFILE))
        {
            getSupportActionBar().setTitle("Vedi profilo");
            setContentView(R.layout.view_profile);
            initializeHashMapProfileField(profileMode);
            initializeViewProfileField();
        }
        // profileMode.equals(HomeActivity.STRING_BUTTON_CREATE_PROFILE)
        else
        {
            getSupportActionBar().setTitle("Crea profilo");
            setContentView(R.layout.create_profile);
            initializeHashMapProfileField(profileMode);
            startControlProfileFieldCreate();
        }
    }

    protected void onStart() {
        super.onStart();
        MainApplication.setCurrentActivity(this);

        /*if(!MainApplication.controlBluetooth())
            MainApplication.activateBluetooth();*/
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

    private void initializeHashMapProfileField(String profileMode)
    {
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

    private void startControlProfileFieldCreate()
    {
        final int colorEmpty = Color.rgb(230,230,230);
        final int colorError = Color.RED;
        final int colorCorrect = Color.GREEN;

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


        buttonSend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                buttonSend.setFocusable(true);
                buttonSend.setFocusableInTouchMode(true);
                buttonSend.requestFocus();

                createProfile();
            }
        });
    }


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

        if(wrongField)
            alertDialogMessage.show();
        else
        {
            HashMap<String, String> insertedData = new HashMap<>();

            insertedData.put("mail", hashMapProfileField.get("mail").getText().toString());
            insertedData.put("pwd", hashMapProfileField.get("password").getText().toString());
            insertedData.put("name", hashMapProfileField.get("name_surname").getText().toString());

            String serverRegisterResponse = UserHandler.userRegister(insertedData);

            if(serverRegisterResponse.equals("OK"))
            {
                Toast.makeText(getApplicationContext(),"Creazione profilo completata",Toast.LENGTH_LONG).show();
                finish();
            }
            else
            {
                alertDialogMessage.setMessage(USER_MESSAGE[3]);
                alertDialogMessage.show();
            }
        }

    }

    private void initializeViewProfileField()
    {
        UserProfile profile = UserHandler.getProfile(UserHandler.getUuid());

        if (profile != null)
        {
            hashMapProfileField.get("mail").setText(profile.getMail());
            hashMapProfileField.get("name_surname").setText(profile.getNameSurname());
            hashMapProfileField.get("uuid").setText(profile.getUuid());
        }
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
