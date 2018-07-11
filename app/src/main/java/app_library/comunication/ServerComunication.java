package app_library.comunication;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;


/**
 * Questa classe serve per gestire la comunicazione con il server
 */

public class ServerComunication{

    // indirizzo ip del server
    private static String hostMaster;

    /**
     *  Metodo che permette di compiere la login
     * @param mail email dell'utente
     * @param pass password dell'utente
     * @return una stringa che indica se la richiesta è stata fatta con successo o no
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static String login(String mail, String pass) throws ExecutionException, InterruptedException {

        // chiavi e valori della login
        ArrayList<String> keys = new ArrayList<>();
        ArrayList<String> value = new ArrayList<>();
        keys.add("mail");
        keys.add("pwd");
        value.add(mail);
        value.add(pass);

        // costruzione json da inviare al server
        String mex = Message.keyValueToJSON(keys,value,value.size(),0);
        return new HttpRequest().execute(HttpRequest.POST_REQUEST, hostMaster,"user/login",mex).get();
    }

    /**
     *  Metodo che permette all'utente di inviare i suoi dati per iscriversi
     * @param info informazioni dell'utente
     * @return una stringa che indica se la richiesta è stata fatta con successo o no
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static String userRegister(HashMap<String,String> info) throws ExecutionException, InterruptedException {

        // chiavi e valori della registrazione
        ArrayList<String> keys = new ArrayList<String>(){{
            add("mail");
            add("pwd");
            add("name");
        }};

        ArrayList<String> values = new ArrayList<>();

        values.add(info.get("mail"));
        values.add(info.get("pwd"));
        values.add(info.get("name"));

        // costruzione json da inviare al server
        String msg = Message.keyValueToJSON(keys,values,values.size(),0);

        return new HttpRequest().execute(HttpRequest.POST_REQUEST, hostMaster,"user/register",msg).get();
    }

    /**
     * Metodo che restituisce tutte le informazioni di un utente dal suo uuid
     * @param uuid uuid dell'utente di cui voglio prendere le informazioni
     * @return stringa che contiente tutte le info dell'utente
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static String getProfile(String uuid) throws ExecutionException, InterruptedException, JSONException {

        return new HttpRequest().execute(HttpRequest.GET_REQUEST, hostMaster,"user/get/" + uuid).get();
    }


    /**
     * Metodo che invia la posizione dell'utente associata ad un beacon e i relativi dati
     * @param uuid stringa che contiene l'uuid dell'utente
     * @param beaconId stringa che contiene il mac adress del beacon in cui si trova l'utente
     * @param beaconData stringa che contiene i dati raccolti dal beacon
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static String sendUserPositionWithData(String uuid, String beaconId, String beaconData)
    {
        String result = "";

        try
        {
            // chiavi e valori per l'invio dei dati
            ArrayList<String> keys = new ArrayList<>();
            ArrayList<String> value = new ArrayList<>();
            keys.add("uuid");
            keys.add("position");
            keys.add("beacon_data");
            value.add(uuid);
            value.add(beaconId);
            value.add(beaconData);

            // costruzione json da inviare al server
            String mex = Message.keyValueToJSON(keys,value,value.size(),0);

            // invio richiesta posizione al server
            String serverSendPositionResponse = new HttpRequest().execute(HttpRequest.POST_REQUEST, hostMaster, "user/position", mex).get();

            JSONObject jsonObjectServerSendPositionResponse = new JSONObject(serverSendPositionResponse);
            String status = jsonObjectServerSendPositionResponse.getString("status");

            // se lo status della risposta è 1 significa che la chiamata è avvenuta con successo
            if (status.equals("1"))
            {
                // si controlla se è presente un'emergenza
                String stringEmergency = jsonObjectServerSendPositionResponse.getJSONObject("result").getString("emergency");

                // con 0 non c'è nessun emergenza
                if (stringEmergency.equals("0"))
                    result = "OK_no_emergency";
                // con 1 c'è un'emergenza
                else
                    result = "OK_emergency";
            }
            // si indica l'errore riscontrato
            else
                result = jsonObjectServerSendPositionResponse.getString("message");
        }
        catch (JSONException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }


    /**
     * Metodo che cancella la posizione dell'utente associata ad un beacon e i relativi dati passando alla funzione sendUserPositionWithData sia il beacon che i dati alla stringa "no_data"
     * @param uuid stringa che contiene l'uuid dell'utente
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static String deleteUserPositionWithData(String uuid)
    {
        return sendUserPositionWithData(uuid, "no_data", "no_data");
    }

    // metodo che ottiene dal server i dati sui nodi e sugli archi
    public static String getJSONData(){

        String result = "";

        try
        {
            // invio richiesta dati al server
            String serverDataResponse = new HttpRequest().execute(HttpRequest.GET_REQUEST, hostMaster, "conn/data").get();

            JSONObject jsonObjectServerDataResponse = new JSONObject(serverDataResponse);
            String status = jsonObjectServerDataResponse.getString("status");

            // se lo status della risposta è 1 significa che la chiamata è avvenuta con successo
            if (status.equals("1"))
                result = serverDataResponse;
        }
        catch (JSONException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }


    // metodo che analizza la risposta ricevuta dal server dal metodo getJSONData restituendo la struttura necessaria per i nodi
    public static HashMap<String,String>[] getNodeData(String serverJSONDataResponse) throws ExecutionException, InterruptedException, JSONException {

        HashMap<String,String>[] results;

        JSONObject jsonObjectNodeDataResponse = new JSONObject(serverJSONDataResponse);

        // chiavi del json
        ArrayList<String> keys = new ArrayList<>();
        keys.add("id");
        keys.add("code");
        keys.add("beacon");
        keys.add("x");
        keys.add("y");
        keys.add("altitude");
        keys.add("width");
        keys.add("secure");

        // costruzione struttura nodi
        results = Message.JSONToArrayKeyValue(jsonObjectNodeDataResponse.getString("results"),keys,"node");

        return results;
    }

    // metodo che analizza la risposta ricevuta dal server dal metodo getJSONData restituendo la struttura necessaria per gli archi
    public static HashMap<String,String>[] getRouteData(String serverJSONDataResponse) throws ExecutionException, InterruptedException, JSONException {

        HashMap<String,String>[] results;

        JSONObject jsonObjectRouteDataResponse = new JSONObject(serverJSONDataResponse);

        // chiavi del json
        ArrayList<String> keys = new ArrayList<>();
        keys.add("id");
        keys.add("code_p1");
        keys.add("code_p2");
        keys.add("people");
        keys.add("LOS");
        keys.add("V");
        keys.add("R");
        keys.add("K");
        keys.add("L");
        keys.add("pv");
        keys.add("pr");
        keys.add("pk");
        keys.add("pl");

        // costruzione struttura archi
        results = Message.JSONToArrayKeyValue(jsonObjectRouteDataResponse.getString("results"),keys,"route");

        return results;
    }

    // metodo che restituisce il percorso più breve in condizioni di emergenza a partire dal mac adress del beacon in cui si trova l'utente
    public static String[] getShortestPathToSafePlace(String beaconId)
    {
        String[] result = null;

        try
        {
            // invio richiesta percorso in situazione di emergenza al server
            String serverShortestPathResponse = new HttpRequest().execute(HttpRequest.GET_REQUEST, hostMaster, "nav/send/" + beaconId).get();

            JSONObject jsonObjectShortestPathResponse = new JSONObject(serverShortestPathResponse);
            String status = jsonObjectShortestPathResponse.getString("status");

            // se lo status della risposta è 1 significa che la chiamata è avvenuta con successo
            if (status.equals("1"))
            {
                // estrazione del percorso
                JSONArray jsonArray = jsonObjectShortestPathResponse.getJSONObject("results").getJSONArray("path");

                result = new String[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {

                    result[i] = jsonArray.getString(i);
                }
            }

        }catch (JSONException e) {
            e.printStackTrace();
            result = null;
        } catch (ExecutionException e) {
            e.printStackTrace();
            result = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            result = null;
        }

        return result;
    }


    // metodo che restituisce il percorso più breve in condizioni di non emergenza indicando il mac adress del beacon di partenza e di destinazione
    public static String[] getShortestPathFromSourceToTarget(String beaconIdStart, String beaconIdEnd)
    {
        String[] result = null;

        try
        {
            // invio richiesta percorso sorgente-destinazione al server
            String serverShortestPathFrToResponse = new HttpRequest().execute(HttpRequest.GET_REQUEST, hostMaster, "nav/path/" + beaconIdStart + "/" + beaconIdEnd).get();

            JSONObject jsonObjectShortestPathFrToResponse = new JSONObject(serverShortestPathFrToResponse);
            String status = jsonObjectShortestPathFrToResponse.getString("status");

            // se lo status della risposta è 1 significa che la chiamata è avvenuta con successo
            if (status.equals("1"))
            {
                // estrazione del percorso
                JSONArray jsonArray = jsonObjectShortestPathFrToResponse.getJSONObject("results").getJSONArray("path");

                result = new String[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {

                    result[i] = jsonArray.getString(i);
                }
            }

        }catch (JSONException e) {
            e.printStackTrace();
            result = null;
        } catch (ExecutionException e) {
            e.printStackTrace();
            result = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            result = null;
        }

        return result;
    }


    /**
     * Metodo che va a testare se è disponibile una connessione con un certo ip
     * @param ip ip dell'ipotetico server con cui voglio comunicare
     * @return un boolean che indica se la connessione è andata a buon fine oppure no
     */
    public static boolean handShake(String ip) {
        boolean b;
        try {

            // invio richiesta stato connessione al server
            String serverResponse = new HttpRequest().execute(HttpRequest.GET_REQUEST, ip,"conn/info").get();
            Log.i("s",serverResponse);

            // chiavi del json
            ArrayList<String> keys = new ArrayList<String>(){{
                add("server");
                add("database");
                add("version");
            }};

            HashMap<String,String> responseElements = Message.JSONToKeyValue(serverResponse, keys);

            // si controlla se il server e il database associato sono attivi
            b = responseElements.get("server").equals("connected") && responseElements.get("database").equals("connected");

        } catch (Exception e) {
            b = false;
        }

        return b;
    }

    public static String getIP() {
        return hostMaster;
    }

    public static void setHostMaster (String h) {
        hostMaster = h;
    }
}
