package app_library.comunication;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by User on 20/06/2018.
 */

/**
 * Classe che costruisce dei messaggi con struttura json, prendendo come parametro la lista di coppia (chiave,valore) e che
 * scompone una stringa, con struttura JSON, restituendo una lista di coppie(chiave,valore)
 */

public class Message {

    /**
     * Questo metodo costruisce un messaggio che dovra' essere inviato al server prendendo le chiavi e i corrispondenti valori,
     * costruendo il JSON, restituendo poi una stringa ((chiave, valore) -> JSON).
     *
     * @param keys   arraylist di chiavi
     * @param values arraylist di valori
     * @param elements indica il numero di elementi (quando arrays ==0, esso coincide con la lunghezza delle liste)
     * @param arrays indica il numero di oggetti che devo creare (se è == i la lunghezza delle liste sara' pari a i*elements)
     * @return ritorna una stringa che e' il messaggio json
     */
    public static String keyValueToJSON(ArrayList<String> keys, ArrayList<String> values, int elements, int arrays){

        JSONObject jsonObject;
        JSONArray jsonArray;

        if(arrays==0) {
            //se deco costruire un messaggio con un unico oggetto

            if (!keys.isEmpty() && !values.isEmpty() && keys != null && values != null) {
                //se nessuno dei due è vuoto possiamo costruire il messaggio
                jsonObject = new JSONObject();
                for (int i = 0; i < elements; i++) {

                    try {
                        jsonObject.put(keys.get(i), values.get(i));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                return jsonObject.toString();
            }
            //se  sono vuote o nulle una delle due liste o entrambe
            return null;
        }else {
            //se devo costruire un array
            jsonArray = new JSONArray();
            for(int k = 0;k<arrays;k++){
                if (!keys.isEmpty() && !values.isEmpty() && keys != null && values != null) {
                    //se nessuno dei due è vuoto possiamo costruire il messaggio
                    jsonObject = new JSONObject();
                    for (int i = 0; i < elements; i++) {

                        try {
                            jsonObject.put(keys.get(i+k*elements), values.get(i+k*elements));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                    jsonArray.put(jsonObject);

                }
                //se  sono vuote o nulle una delle due liste o entrambe
                return null;
            }

        }
        return jsonArray.toString();
    }

    /**
     * Attraverso le classi JSONObject di Android, siamo in grado di trasformare una stinga in un oggetto che dà
     * la possibilità di scorrerlo e costruire l'hashmap (chiave, valore) che viene fornita in output (JSON -> (chiave, valore)).
     * @param s: messaggio da scomporre
     * @param keys lista di chiavi che saranno contenute all'interno del messaggio
     * @return la lista di coppie (k,v) che sono contenute all'interno del messaggio
     * @throws JSONException
     */
    public static HashMap<String,String> JSONToKeyValue(String s, ArrayList<String> keys) throws JSONException {
        HashMap<String,String> messageElements = new HashMap<>();
        //messageElements.clear();
        Log.i("Messaggio da scomporre",s);
        JSONObject obj = new JSONObject(s);
        for(int k=0;k<keys.size();k++){
            try {
                messageElements.put(keys.get(k),obj.getString(keys.get(k)));
                Log.i("key and value :",keys.get(k)+" "+obj.getString(keys.get(k)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return messageElements;
    }

    /**
     *  Metodo molto simile a quello precendente ma che lavora con JSONArray, cioè array di JSONObject, quindi
     *  si scorre l'array si estraggono gli oggetti e si costruisce l'array di hashmap che viene fornita in output (JSON -> array (chiave, valore))
     *  del JSON.
     * @param s
     * @param keys
     * @param name
     * @return
     * @throws JSONException
     */
    public static HashMap<String,String>[] JSONToArrayKeyValue(String s,ArrayList<String> keys, String name) throws JSONException {
        JSONObject json = new JSONObject(s);
        JSONArray jsonArray = json.getJSONArray(name);
        HashMap<String,String>[] array = new HashMap[jsonArray.length()];

        JSONObject jsonobject;

        for (int i = 0; i < jsonArray.length(); i++) {
            jsonobject = jsonArray.getJSONObject(i);
            HashMap<String,String> messageElements = new HashMap<>();
            messageElements.clear();
            for(int k=0;k<keys.size();k++){
                try {
                    messageElements.put(keys.get(k),jsonobject.getString(keys.get(k)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            array[i] = messageElements;
        }
        return array;
    }
}
