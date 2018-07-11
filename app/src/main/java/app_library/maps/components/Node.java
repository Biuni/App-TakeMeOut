package app_library.maps.components;

/**
 * Classe che identifica una stanza con associato il relativo beacon
 */

public class Node {

    // codice della stanza
    private String roomCod;

    // mac adress beacon
    private String beaconId;

    // coordinate della stanza in cui nella posizione 0 vi è la x e nella posizione 1 la y
    private int[] coords;

    // piano della stanza
    private String altitude;

    // larghezza
    private double width;

    // costruttore vuoto
    public Node(){

    }

    // costruttore non vuoto
    public Node(String roomCod, String beaconId, int[] coords, String altitude, double width){

        this.roomCod = roomCod;
        this.beaconId = beaconId;
        this.coords = coords;
        this.altitude = altitude;
        this.width = width;
    }

    public String getRoomCod() {
        return roomCod;
    }

    public String getBeaconId() {
        return beaconId;
    }

    public int[] getCoords() {
        return coords;
    }

    public String getAltitude() {
        return altitude;
    }

    public double getWidth() {
        return width;
    }

}
