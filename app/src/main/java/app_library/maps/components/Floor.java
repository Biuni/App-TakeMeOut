package app_library.maps.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


// Classe che definisce un piano che ha come proprietà un insieme di stanze o nodi:
public class Floor {

    // nome del piano
    private String floorName;

    // hasmap nodi con chiave roomCod ovvero il codice della stanza e valore l'oggetto nodo
    private HashMap<String,Node> nodes;

    // costruttore del piano
    public Floor(String floorName){

        this.floorName = floorName;
        nodes = new HashMap<>();
    }

    public HashMap<String, Node> getNodes() {
        return nodes;
    }

    public void addNode(String roomCod, Node n){
        nodes.put(roomCod, n);
    }

    // metodo che costruisce una lista di array di stringhe che contiente tutti i codici delle stanze all'elemento 0 e del beacon associato all'elemento 1 riferiti al piano
    public ArrayList<String[]> getListNameRoomAndBeacon() {

        // lista di ouput
        ArrayList<String[]> result = new ArrayList<>();

        Iterator iterator = nodes.entrySet().iterator();

        while (iterator.hasNext())
        {
            Map.Entry pair = (Map.Entry) iterator.next();

            String[] currentElement = new String[2];
            currentElement[0] = pair.getKey().toString();
            currentElement[1] = nodes.get(pair.getKey().toString()).getBeaconId();

            result.add(currentElement);
        }

        return result;
    }

    // metodo che costruisce una lista di stringhe che contiente o tutti i codici delle stanze (booleano vero) o dei beacon (booleano falso) riferiti al piano a seconda del parametro
    public ArrayList<String> getListNameRoomOrBeacon(boolean roomName) {

        // lista di ouput con o i nomi delle stanze o dei beacon
        ArrayList<String[]> listRoomOrBeacon = getListNameRoomAndBeacon();
        ArrayList<String> result = new ArrayList<>();
        int index;

        // se è vero si imposta l'indice a 0 per le stanze altrimenti a 1 per i beacon
        if (roomName)
            index = 0;
        else
            index = 1;

        for (int i = 0; i < listRoomOrBeacon.size(); i++)
            result.add(listRoomOrBeacon.get(i)[index]);

        return result;
    }

    public String getFloorName() {
        return floorName;
    }
}
