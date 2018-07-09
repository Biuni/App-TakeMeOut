package app_library.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.net.NetworkInterface;
import java.util.*;
import java.util.concurrent.ExecutionException;
import app_library.MainApplication;
import app_library.comunication.ServerComunication;
import app_library.comunication.Message;

/**
 * Classe che gestirà tutte le operazioni su un utente, quali login, iscrizione, modifica profilo, logout, ecc..
 * Inoltre c'è un campo pref (SharedPreferences) che conterrà le informazioni, quali username, nome e cognome.
 * Queste vengono mantenute all'interno della struttura anche dopo la chiusura dell'applicazione. Si tratta di uno
 * storage di tipo Session. In questo modo se l'utente rimane loggato, ad un nuovo accesso avrà ancora in memoria
 * le informazioni pregresse.
 */

public class UserHandler {

    private static String mail;
    private static String nameSurname;
    private static String uuid;

        // preferenze salvate dall'applicazione
    private static SharedPreferences pref;
    private static Editor editor;

    public static void init(){

        editor = pref.edit();
    }

    /**
     * Metodo che inizializza la posizione dell'utente e costruisce il messaggio considerando le seguenti condizioni:
     * - se è già loggato nel messaggio verranno inseriti Nome e Cognome dell'utente altrimenti verrà utilizzata la coppia Guest e Guest
     * - se si conosce la sua posizione, rilevata da un beacon, si inserisce il codice del beacon altrimenti posizione sconosciuta (unkonwn).
     *
     */

    public static void initializePosition() {

        // "beacon_ID" != null
        if(MainApplication.getScanner().getCurrentBeacon() != null)
        {
            if(MainApplication.getOnlineMode() && isLogged())
            {
                ServerComunication.sendUserPositionWithData(getUuid(), MainApplication.getScanner().getCurrentBeacon().getAddress(), "");
                Log.i("user-beacon", "" + getUuid() + "-" + MainApplication.getScanner().getCurrentBeacon().getAddress());
            }
        }
    }


    /**
     * Metodo che permette l'iscrizione di un utente, inviando al server le informazioni già controllate in fase di inserimento.
     * @param info informazioni dell'utente
     * @return un boolean che indica se la richiesta è andata a buon fine o no
     */
    public static String userRegister(HashMap<String,String> info){

        String result = "";

        if(MainApplication.getOnlineMode()) {
            try {
                String serverRegisterResponse = ServerComunication.userRegister(info);

                JSONObject jsonObjectServerRegisterResponse = new JSONObject(serverRegisterResponse);
                String status = jsonObjectServerRegisterResponse.getString("status");

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

    /**
     * Metodo che permette di effettuare la logout
     */

    public static void logout() {

        if(MainApplication.getOnlineMode() && isLogged())
            ServerComunication.deleteUserPositionWithData(getUuid());

        mail = null;
        cleanUserEditor();
    }

    /**
     * Metodo che restituisce le informazioni di un utente
     * @param userUuid : uuid dell'utente
     * @return l'oggetto UserProfile che contiene tutte le info dell'utente
     */

    public static UserProfile getProfile(String userUuid){

        UserProfile profile = null;

        if(MainApplication.getOnlineMode()) {
            try {

                String serverProfileResponse = ServerComunication.getProfile(userUuid);

                JSONObject jsonObjectServerProfileResponse = new JSONObject(serverProfileResponse);
                String status = jsonObjectServerProfileResponse.getString("status");

                if (status.equals("1"))
                {
                    JSONArray jsonArrayResult = jsonObjectServerProfileResponse.getJSONArray("result");
                    JSONObject jsonObjectResult = jsonArrayResult.getJSONObject(0);

                    String userMail = jsonObjectResult.getString("mail");
                    String userNameSurname = jsonObjectResult.getString("name");

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

    /**
     * Modifica il contenute dell'oggetto session Pref
     */

    private static void updateUserEditor() {
        editor.putString("mail",mail);
        editor.putString("nameSurname",nameSurname);
        editor.putString("uuid",uuid);
        editor.commit();
    }

    /**
     * Resetta le informazioni dentro l'oggetto session Pref
     */

    private static void cleanUserEditor() {
        //editor.clear();
        editor.putString("mail",null);
        editor.putString("nameSurname",null);
        editor.putString("uuid",null);
        editor.commit();
    }

    /**
     * controlla che utente sia loggato o che ci siano dati nella sharedpreferencies
     * @return boolen che indica lo stato dell'utente
     */
    public static boolean isLogged() {
        boolean b = false;

        if(mail!=null)
            b = true;
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
     * @param rememberLogin parametro boolean che indica se l'utente ha scelto o no se mantenere i suoi dati di accesso in memoria. Basta pensare alla classica Ricordami su un form di login
     * @return
     */

    public static String login(String userMail, String pass, boolean rememberLogin){

        String result = "";

        if(MainApplication.getOnlineMode())
        {
            try
            {
                String serverLoginResponse = ServerComunication.login(userMail, pass);

                JSONObject jsonObjectServerLoginResponse = new JSONObject(serverLoginResponse);
                String status = jsonObjectServerLoginResponse.getString("status");

                if (status.equals("1"))
                {
                    JSONArray jsonArrayResult = jsonObjectServerLoginResponse.getJSONArray("result");
                    JSONObject jsonObjectResult = jsonArrayResult.getJSONObject(0);

                    String userNameSurname = jsonObjectResult.getString("name");
                    String userUuid = jsonObjectResult.getString("uuid");
                    Log.i("nameSurname", userNameSurname);

                    mail=userMail;
                    nameSurname = userNameSurname;
                    uuid = userUuid;

                    if(rememberLogin)
                        updateUserEditor();
                    else
                        cleanUserEditor();

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

    public static void setInfo(String userMail,String userNameSurname,String userUuid){
        mail = userMail;
        nameSurname = userNameSurname;
        uuid = userUuid;
    }
}
