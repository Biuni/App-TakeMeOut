package app_library.maps.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Classe che definisce un piano che ha come proprietà un insieme di:
 * - stanze/aule (rooms)
 * - nodi/beacons (nodes)
 * Viene identificata da un nome
 */

public class Floor {

    /*private HashMap<String,Room> rooms;
    private HashMap<String,Node> nodes;
    private String floorName;

    public Floor(String s){
        floorName = s;
        rooms = new HashMap<>();
        nodes = new HashMap<>();
    }

    public HashMap<String,Room> getRooms() {
        return rooms;
    }

    public HashMap<String, Node> getNodes() {
        return nodes;
    }

    public void addNode(String cod, Node n){
        nodes.put(cod,n);
    }

    public void deleteNode(int idNode){

    }

    public void addNotification(Notify n){

    }

    public void deleteNotification(String n){

    }

    public void addRoom(String name,Room r) {
        rooms.put(name,r);
    }


    metodo che costruisce un arraylist di stringhe che contiente tutti i nomi delle aule
    public ArrayList<String> nameStringRoom() {
        ArrayList<String> s = new ArrayList();
        Iterator it = rooms.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            s.add(pair.getKey().toString());
        }
        return s;
    }

    metodo che costruisce un arraylist di stringhe che contiente tutti i nomi dei beacon
    public ArrayList<String> nameStringNode() {
        ArrayList<String> s = new ArrayList();
        Iterator it = nodes.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            s.add(pair.getKey().toString());
        }
        return s;
    }

    public String getFloorName() {
        return floorName;
    }*/









    private String floorName;

    // roomCod-Node
    private HashMap<String,Node> nodes;

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

    // metodo che costruisce una lista di array di stringhe che contiente tutti i codici delle stanze all'elemento 0 e del beacon associato all'elemento 1
    public ArrayList<String[]> getListNameRoomAndBeacon() {

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

    // metodo che costruisce una lista di stringhe che contiente o tutti i codici delle stanze o dei beacon a seconda del parametro booleano
    public ArrayList<String> getListNameRoomOrBeacon(boolean roomName) {

        ArrayList<String[]> listRoomOrBeacon = getListNameRoomAndBeacon();
        ArrayList<String> result = new ArrayList<>();
        int index;

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
