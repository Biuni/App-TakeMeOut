package app_library.user;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.ExecutionException;
import app_library.MainApplication;
import app_library.comunication.ServerComunication;
import app_library.sharedstorage.Data;

/**
 * Classe che gestirà tutte le operazioni su un utente, quali login, iscrizione, logout, ecc..
 * Inoltre c'è un campo pref (SharedPreferences) che conterrà le informazioni quali mail, nome e cognome e uuid.
 * Queste vengono mantenute all'interno della struttura anche dopo la chiusura dell'applicazione.
 * In questo modo se l'utente rimane loggato, ad un nuovo accesso avrà ancora in memoria
 * le informazioni pregresse.
 */

public class UserHandler {

    // mail dell'utente
    private static String mail;

    // nome e cognome utente
    private static String nameSurname;

    // uuid dell'utente
    private static String uuid;

    // preferenze salvate dall'applicazione
    private static SharedPreferences pref;

    // editor associato alle preferenze
    private static Editor editor;

    // metodo per l'inizializzazione dell'editor
    public static void init(){

        editor = pref.edit();
    }

    // metodo che inizializza la posizione dell'utente a partire dal suo uuid e costruisce il messaggio con la sua posizione, rilevata dal mac adress del beacon
    public static void initializePosition() {

        try {

            // mac adress beacon non trovato
            String stringBeaconId = "no_data";

            // risposta del server
            String serverResponse = "";

            // se l'informazione del mac adress del beacon è presente si acquisisce
            if(MainApplication.getScanner().getCurrentBeacon() != null)
                stringBeaconId = MainApplication.getScanner().getCurrentBeacon().getAddress();

            // se l'utente e online e loggato (per la necessita del suo uuid) si inviano i datii della posizione al server
            if(MainApplication.getOnlineMode() && isLogged())
            {
                // invio richiesta posizione al server
                serverResponse = ServerComunication.sendUserPositionWithData(getUuid(), stringBeaconId, "no_data");
                Log.i("user-beacon", "" + getUuid() + "-" + stringBeaconId);
            }

            // si controlla se nella risposta del server viene indicata se è presente un'emergenza
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


    /**
     * Metodo che permette l'iscrizione di un utente, inviando al server le informazioni già controllate in fase di inserimento.
     * @param info informazioni dell'utente
     * @return una stringa che indica se la richiesta è andata a buon fine o no
     */
    public static String userRegister(HashMap<String,String> info){

        String result = "";

        if(MainApplication.getOnlineMode()) {
            try {

                // invio richiesta registrazione al server
                String serverRegisterResponse = ServerComunication.userRegister(info);

                JSONObject jsonObjectServerRegisterResponse = new JSONObject(serverRegisterResponse);
                String status = jsonObjectServerRegisterResponse.getString("status");

                // se lo status della risposta è 1 significa che la chiamata è avvenuta con successo
                if (status.equals("1"))
                    result = "OK";
                else
                    result = jsonObjectServerRegisterResponse.getString("message");

            }catch (JSONException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    // metodo che permette di effettuare la logout
    public static void logout() {

        // risposta del server
        String serverResponse = "";

        // se l'utente è online e loggato si cancellano le proprie informazioni sulla posizione
        if(MainApplication.getOnlineMode() && isLogged())
            serverResponse = ServerComunication.deleteUserPositionWithData(getUuid());

        mail = null;

        // si cancellano le preferenze con le informazioni dell'utente
        cleanUserEditor();

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

    /**
     * Metodo che restituisce le informazioni di un utente
     * @param userUuid : uuid dell'utente
     * @return l'oggetto UserProfile che contiene tutte le info dell'utente
     */

    public static UserProfile getProfile(String userUuid){

        UserProfile profile = null;

        // si controlla se si è online
        if(MainApplication.getOnlineMode()) {
            try {

                // si invia la richiesta per ottenere le info sul profilo al server
                String serverProfileResponse = ServerComunication.getProfile(userUuid);

                JSONObject jsonObjectServerProfileResponse = new JSONObject(serverProfileResponse);
                String status = jsonObjectServerProfileResponse.getString("status");

                // se lo status della risposta è 1 significa che la chiamata è avvenuta con successo
                if (status.equals("1"))
                {
                    JSONArray jsonArrayResult = jsonObjectServerProfileResponse.getJSONArray("result");
                    JSONObject jsonObjectResult = jsonArrayResult.getJSONObject(0);

                    String userMail = jsonObjectResult.getString("mail");
                    String userNameSurname = jsonObjectResult.getString("name");

                    // creazione oggetto del profilo con le informazioni ottenute
                    profile = new UserProfile(userMail, userNameSurname, userUuid);
                }

            }catch (JSONException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return profile;
    }

    // aggiornamento delle preferenze con le informazioni dell'utente
    private static void updateUserEditor() {
        editor.putString("mail",mail);
        editor.putString("nameSurname",nameSurname);
        editor.putString("uuid",uuid);

        // salvataggio modifiche
        editor.commit();
    }

    // si cancellano le preferenze con le informazioni dell'utente
    private static void cleanUserEditor() {

        editor.putString("mail",null);
        editor.putString("nameSurname",null);
        editor.putString("uuid",null);

        // salvataggio modifiche
        editor.commit();
    }

    /**
     * controlla che utente sia loggato o che ci siano dati nella sharedpreferencies
     * @return boolen che indica lo stato dell'utente
     */
    public static boolean isLogged() {

        // booleano per indicare se l'utente è loggato
        boolean b = false;

        // ci sono informazioni nella classe quindi l'utente è loggato
        if(mail!=null)
            b = true;
        // se non ci sono informazioni nella classe quindi si prova a reperirle dalle preferenze
        else if (pref.getString("mail",null)!=null) {
            setInfo(pref.getString("mail",null),pref.getString("nameSurname",null),pref.getString("uuid",null));
            b = true;
        }

        return b;
    }

    /**
     * Metodo che permette di compiere la login
     * @param userMail mail dell'utente
     * @param pass password dell'utente
     * @param rememberLogin parametro boolean che indica se l'utente ha scelto o no se mantenere i suoi dati di accesso in memoria (ricordami login)
     * @return
     */
    public static String login(String userMail, String pass, boolean rememberLogin){

        String result = "";

        // si controlla se si è online
        if(MainApplication.getOnlineMode())
        {
            try
            {
                // si invia la richiesta per effettuare il login al server
                String serverLoginResponse = ServerComunication.login(userMail, pass);

                JSONObject jsonObjectServerLoginResponse = new JSONObject(serverLoginResponse);
                String status = jsonObjectServerLoginResponse.getString("status");

                // se lo status della risposta è 1 significa che la chiamata è avvenuta con successo
                if (status.equals("1"))
                {
                    JSONArray jsonArrayResult = jsonObjectServerLoginResponse.getJSONArray("result");
                    JSONObject jsonObjectResult = jsonArrayResult.getJSONObject(0);

                    // vengono lette le informazioni dell'utente dalla risposta del server
                    String userNameSurname = jsonObjectResult.getString("name");
                    String userUuid = jsonObjectResult.getString("uuid");
                    Log.i("nameSurname", userNameSurname);

                    // aggiornamento info utente della classe
                    mail=userMail;
                    nameSurname = userNameSurname;
                    uuid = userUuid;

                    // se si è selezionato di ricordare l'accesso nella login si aggiornano le preferenze dell'app altrimenti si cancellano
                    if(rememberLogin)
                        updateUserEditor();
                    else
                        cleanUserEditor();

                    // invio posizione utente al server se disponibile
                    initializePosition();
                    result = "OK";
                }
                else
                    result = jsonObjectServerLoginResponse.getString("message");

            }catch (JSONException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        Log.i("Risp ", result);
        return result;
    }

    public static String getMail() {
        return mail;
    }

    public static String getNameSurname() {
        return nameSurname;
    }

    public static String getUuid() {
        return uuid;
    }

    public static void setPref(SharedPreferences p) {
        pref = p;
    }

    // aggiornamento dei dati dell'utente della classe
    public static void setInfo(String userMail,String userNameSurname,String userUuid){
        mail = userMail;
        nameSurname = userNameSurname;
        uuid = userUuid;
    }
}
