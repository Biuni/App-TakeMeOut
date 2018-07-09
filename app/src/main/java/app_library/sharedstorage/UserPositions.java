package app_library.sharedstorage;

/**
 * Classe che estende SharedData che contiene la posizione dell'utente.
 * All'atto della setPosition viene richiamata la updateInformation che richiama la serie di retrive.
 */

public class UserPositions extends SharedData {

    private int x;
    private int y;
    private String floor;
    private String roomCod;

    public UserPositions() {
        x=0;
        y=0;
    }

    public void setFloor(String f) {
        floor = f;
    }

    public String getFloor() {
        return floor;
    }

    public String getRoomCod() {
        return roomCod;
    }

    public void setRoomCod(String rcode) {
        roomCod = rcode;
    }

    public int[] getPosition() {
        int[] pos = {this.x,
                     this.y};
        return pos;
    }

    public void setPosition(int[] pos) {
        this.x = pos[0];
        this.y = pos[1];
            //Vengono richiamate tutte le diverse retrive dei subscriber
        updateInformation();
    }
}
