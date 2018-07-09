package app_library.maps.components;

/**
 * Classe che identifica un beacon
 */

public class Node {

    /*private int[] coords;
    private String floor;

    public Node(){

    }

    public Node(int[] crds,String fl){

        coords = crds;
        floor = fl;
    }


    public int[] getCoords() {
        return coords;
    }

    public String getFloor() {
        return floor;
    }*/








    private String roomCod;
    private String beaconId;
    private int[] coords;
    private String altitude;
    private double width;

    public Node(){

    }

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
