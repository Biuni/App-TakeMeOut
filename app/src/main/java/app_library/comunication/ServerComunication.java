package app_library.comunication;

import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import app_library.MainApplication;
import app_library.comunication.HttpRequest;
import app_library.comunication.Message;
import app_library.user.UserProfile;


/**
 * Questa classe serve per gestire la comunicazione con il server
 */

public class ServerComunication{

    private static String hostMaster;

    /**
     *  Metodo che permette di compiere la login
     * @param mail email dell'utente
     * @param pass password dell'utente
     * @return un boolean che indica se la richiesta è stata fatta con successo o no
     * @throws ExecutionException
     * @throws InterruptedException
     */

    /*public static boolean login(String mail, String pass) throws ExecutionException, InterruptedException {
        ArrayList<String> name = new ArrayList<>();
        ArrayList<String> value = new ArrayList<>();
        name.add("email");
        name.add("password");
        value.add(mail);
        value.add(pass);
        String mex = Message.keyValueToJSON(name,value,value.size(),0);
        return Boolean.parseBoolean(new HttpRequest().execute(HttpRequest.POST_REQUEST, hostMaster,"user/login",mex).get());
    }*/

    public static String login(String mail, String pass) throws ExecutionException, InterruptedException {
        ArrayList<String> keys = new ArrayList<>();
        ArrayList<String> value = new ArrayList<>();
        keys.add("mail");
        keys.add("pwd");
        value.add(mail);
        value.add(pass);
        String mex = Message.keyValueToJSON(keys,value,value.size(),0);
        return new HttpRequest().execute(HttpRequest.POST_REQUEST, hostMaster,"user/login",mex).get();
    }

    /**
     *  Metodo che permette all'utente di inviare i suoi dati per iscriversi
     * @param info informazioni dell'utente
     * @return un boolean che indica se la richiesta è stata fatta con successo o no
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static String userRegister(HashMap<String,String> info) throws ExecutionException, InterruptedException {

        ArrayList<String> keys = new ArrayList<String>(){{
            add("mail");
            add("pwd");
            add("name");
        }};

        ArrayList<String> values = new ArrayList<>();

        values.add(info.get("mail"));
        values.add(info.get("pwd"));
        values.add(info.get("name"));

        String msg = Message.keyValueToJSON(keys,values,values.size(),0);

        return new HttpRequest().execute(HttpRequest.POST_REQUEST, hostMaster,"user/register",msg).get();
    }

    /**
     * Metodo che restituisce tutte le informazioni di un utente
     * @param uuid uuid dell'utente di cui voglio prendere le informazioni
     * @return l'oggetto userprofile che contiente tutte le info dell'utente
     * @throws ExecutionException
     * @throws InterruptedException
     */

    /*public static UserProfile getprofile(String uuid) throws ExecutionException, InterruptedException, JSONException {

        UserProfile profile = null;

        String serverProfileResponse = new HttpRequest().execute(HttpRequest.GET_REQUEST, hostMaster,"user/get/" + uuid).get();
        JSONObject objectServerProfileResponse = new JSONObject(serverProfileResponse);

        try {

            String status = objectServerProfileResponse.getString("status");

            if (status.equals("1"))
            {
                JSONArray jsonArrayResult = objectServerProfileResponse.getJSONArray("result");
                JSONObject objectResult = jsonArrayResult.getJSONObject(0);

                String mail = objectResult.getString("mail");
                String nameSurname = objectResult.getString("name");

                profile = new UserProfile(mail, nameSurname, uuid);

            }
            else
                Toast.makeText(MainApplication.getCurrentActivity().getApplicationContext(), objectServerProfileResponse.getString("message"), Toast.LENGTH_LONG).show();

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return profile;
    }*/

    public static String getProfile(String uuid) throws ExecutionException, InterruptedException, JSONException {

        return new HttpRequest().execute(HttpRequest.GET_REQUEST, hostMaster,"user/get/" + uuid).get();
    }




    /**
     * Metodo che invia i dati dei sensori di un beacon al server
     * @param message stringa che contiene tutte i dati prelevati
     * @throws ExecutionException
     * @throws InterruptedException
     */
    /*public static void sendValue(String message) throws ExecutionException, InterruptedException {
        new HttpRequest().execute(HttpRequest.POST_REQUEST, hostMaster,"beaconvalue/insertvalue",message).get();
    }*/

    /**
     * Metodo che invia la posizione dell'utente
     * @param message stringa che contiene i dati sulla posizione
     * @throws ExecutionException
     * @throws InterruptedException
     */
    /*public static void sendPosition(String message) throws ExecutionException, InterruptedException {
        new HttpRequest().execute(HttpRequest.PUT_REQUEST, hostMaster,"position/setposition",message).get();
    }*/

    /**
     * Metodo che cancella la posizione di un utente
     * @param ip ip dell'utente che dovrò cancellare
     * @throws ExecutionException
     * @throws InterruptedException
     */
    /*public static void deletePosition(String ip) throws ExecutionException, InterruptedException {
        new HttpRequest().execute(HttpRequest.DELETE_REQUEST, hostMaster,"position/deleteuser/"+ip).get();
    }*/



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
            ArrayList<String> keys = new ArrayList<>();
            ArrayList<String> value = new ArrayList<>();
            keys.add("uuid");
            keys.add("position");
            keys.add("beacon_data");
            value.add(uuid);
            value.add(beaconId);
            value.add(beaconData);
            String mex = Message.keyValueToJSON(keys,value,value.size(),0);

            String serverSendPositionResponse = new HttpRequest().execute(HttpRequest.POST_REQUEST, hostMaster, "user/position", mex).get();

            JSONObject jsonObjectServerSendPositionResponse = new JSONObject(serverSendPositionResponse);
            String status = jsonObjectServerSendPositionResponse.getString("status");

            if (status.equals("1"))
                result = "OK";
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
     * Metodo che cancella la posizione dell'utente associata ad un beacon e i relativi dati
     * @param uuid stringa che contiene l'uuid dell'utente
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static String deleteUserPositionWithData(String uuid)
    {
        return sendUserPositionWithData(uuid, "", "");
    }




    /**
     * Metodo che restitutisce tutti i beacon di una determinata struttura passata in input
     * @param building nome dell'edificio
     * @return la lista di utti i beacon dell'edificio
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws JSONException
     */
    /*public static HashMap<String,String>[] getBuildingBeacon(String building) throws ExecutionException, InterruptedException, JSONException {
        ArrayList<String> keys = new ArrayList<>();
        keys.add("beacon_ID");
        keys.add("floor");
        keys.add("x");
        keys.add("y");
        return Message.JSONToArrayKeyValue(new HttpRequest().execute(HttpRequest.GET_REQUEST, hostMaster,"beaconnode/getallnodes/"+building).get(),keys,"beacons");
    }*/

    /*public static HashMap<String,String> getScanParameters() throws ExecutionException, InterruptedException, JSONException {
        ArrayList<String> keys = new ArrayList<>();
        keys.add("scanPeriodNormal");
        keys.add("scanPeriodSearching");
        keys.add("scanPeriodEmergency");
        keys.add("periodBetweenScanNormal");
        keys.add("periodBetweenScanSearching");
        keys.add("periodBetweenScanEmergency");

        return Message.JSONToKeyValue(new HttpRequest().execute(HttpRequest.GET_REQUEST, hostMaster,"parameters").get(),keys);
    }*/

    /**
     * Metodo che restituisce la lista di tutte le stanze/aule di un determinato edificio
     * @return lista di tutte le stanze
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws JSONException
     */

    /*public static HashMap<String,String>[] getBuildingRoom(String building) throws ExecutionException, InterruptedException, JSONException {
        ArrayList<String> keys = new ArrayList<>();
        keys.add("x");
        keys.add("y");
        keys.add("floor");
        keys.add("width");
        keys.add("room");
        return Message.JSONToArrayKeyValue(new HttpRequest().execute(HttpRequest.GET_REQUEST, hostMaster,"room/getallrooms/"+building).get(),keys,"rooms");
    }*/




    public static String getJSONData(){

        String result = "";

        try
        {
            String serverDataResponse = new HttpRequest().execute(HttpRequest.GET_REQUEST, hostMaster, "conn/data").get();

            JSONObject jsonObjectServerDataResponse = new JSONObject(serverDataResponse);
            String status = jsonObjectServerDataResponse.getString("status");

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


    public static HashMap<String,String>[] getNodeData(String serverJSONDataResponse) throws ExecutionException, InterruptedException, JSONException {

        HashMap<String,String>[] results;

        JSONObject jsonObjectNodeDataResponse = new JSONObject(serverJSONDataResponse);

        ArrayList<String> keys = new ArrayList<>();
        keys.add("id");
        keys.add("code");
        keys.add("beacon");
        keys.add("x");
        keys.add("y");
        keys.add("altitude");
        keys.add("width");
        keys.add("secure");

        results = Message.JSONToArrayKeyValue(jsonObjectNodeDataResponse.getString("results"),keys,"node");

        return results;
    }

    public static HashMap<String,String>[] getRouteData(String serverJSONDataResponse) throws ExecutionException, InterruptedException, JSONException {

        HashMap<String,String>[] results;

        JSONObject jsonObjectRouteDataResponse = new JSONObject(serverJSONDataResponse);

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

        results = Message.JSONToArrayKeyValue(jsonObjectRouteDataResponse.getString("results"),keys,"route");

        return results;
    }


    /*public static HashMap<String,String>[] getNodeData() throws ExecutionException, InterruptedException, JSONException {

        HashMap<String,String>[] results = null;

        String serverNodeDataResponse = new HttpRequest().execute(HttpRequest.GET_REQUEST, hostMaster, "conn/data").get();

        JSONObject jsonObjectNodeDataResponse = new JSONObject(serverNodeDataResponse);
        String status = jsonObjectNodeDataResponse.getString("status");

        if (status.equals("1"))
        {
            ArrayList<String> keys = new ArrayList<>();
            keys.add("id");
            keys.add("code");
            keys.add("beacon");
            keys.add("x");
            keys.add("y");
            keys.add("altitude");
            keys.add("width");
            keys.add("secure");

            results = Message.JSONToArrayKeyValue(jsonObjectNodeDataResponse.getString("results"),keys,"node");
        }

        return results;
    }*/


    public static String[] getShortestPathToSafePlace(String beaconId)
    {
        String[] result = null;

        try
        {
            String serverShortestPathResponse = new HttpRequest().execute(HttpRequest.GET_REQUEST, hostMaster, "nav/send/" + beaconId).get();

            JSONObject jsonObjectShortestPathResponse = new JSONObject(serverShortestPathResponse);
            String status = jsonObjectShortestPathResponse.getString("status");

            if (status.equals("1"))
            {
                JSONObject jsonObjectResults = new JSONObject(jsonObjectShortestPathResponse.getString("results"));
                JSONArray jsonArray = jsonObjectResults.getJSONArray("path");

                result = new String[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObjectCurrent = jsonArray.getJSONObject(i);

                    result[i] = jsonObjectCurrent.getString("" + i);
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


    public static String[] getShortestPathFromSourceToTarget(String beaconIdStart, String beaconIdEnd)
    {
        String[] result = null;

        try
        {
            String serverShortestPathFrToResponse = new HttpRequest().execute(HttpRequest.GET_REQUEST, hostMaster, "nav/path/" + beaconIdStart + "/" + beaconIdEnd).get();

            JSONObject jsonObjectShortestPathFrToResponse = new JSONObject(serverShortestPathFrToResponse);
            String status = jsonObjectShortestPathFrToResponse.getString("status");

            if (status.equals("1"))
            {
                JSONObject jsonObjectResults = new JSONObject(jsonObjectShortestPathFrToResponse.getString("results"));
                JSONArray jsonArray = jsonObjectResults.getJSONArray("path");

                result = new String[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObjectCurrent = jsonArray.getJSONObject(i);

                    result[i] = jsonObjectCurrent.getString("" + i);
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
            String serverResponse = new HttpRequest().execute(HttpRequest.GET_REQUEST, ip,"conn/info").get();
            Log.i("s",serverResponse);

            ArrayList<String> keys = new ArrayList<String>(){{
                add("server");
                add("database");
                add("version");
            }};

            HashMap<String,String> responseElements = Message.JSONToKeyValue(serverResponse, keys);

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
