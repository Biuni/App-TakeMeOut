package app_library.beacon;

import android.util.Log;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.UUID;

/**
    Classe utilizzata per gestire i servizi messi a disposizione dal beacon (per servizi si intende
    quali sensori sono disponibili)
 */

public class BeaconService {
        //identificativo del servizio
    private String id;
        //indirizzo UUID del servizio
    private UUID serviceUUID;
        //indirizzo UUID contenente i dati del sensore rispetto al servizio selezionato
    private UUID serviceDataUUID;
        //indirizzo UUID contenente la configurazione del sensore rispetto al servizio selezionato
    private UUID serviceConfigUUID;
        //lista dei valori estratti dal sensore
    private ArrayList<Double> value;
    private Timestamp lastSampleTime;
    /**
     * Costruttore la struttura dati per il servizio
     * @param name, identificativo del servizio
     * @param s, uuid del servizio
     * @param sd, uuid dei dati del servizio
     * @param sc, uuid per la configurazione del servizio
     */
    public BeaconService(String name, String s, String sd, String sc) {
        id = name;
        serviceUUID = UUID.fromString(s);
        serviceDataUUID = UUID.fromString(sd);
        serviceConfigUUID = UUID.fromString(sc);
        value = new ArrayList<>();
        lastSampleTime = new Timestamp(0);
    }
    /**
     * Metodo che restituisce l'identificativo del servizio
     * @return identificativo del servizio
     */
    public String getName() {
        return id;
    }
    /**
     * Metodo che restituisce l'indirizzo UUID del servizio
     * @return l'indirizzo UUID del servizio
     */
    public UUID getService() {
        return serviceUUID;
    }

    /**
     * Metodo che restituisce l'indirizzo UUID contenente i valori dei sensori per il servizio
     * @return l'indirizzo UUID per i dati del servizio
     */
    public UUID getServiceData() { return serviceDataUUID; }

    /**
     * Metodo che restituisce l'indirizzo UUID contenente la configurazione per i sensori per il servizio
     * @return l'indirizzo UUID per la configurazione per i sensori per il servizio
     */
    public UUID getServiceConfig() {
        return serviceConfigUUID;
    }

    /**
     * Metodo che restituisce il valore dei dati estratti dai sensori
     * @return valori estratti dai sensori
     */
    public ArrayList<Double> getValue() {
        return value;
    }

    /**
     * Metodo per settare i valori alla struttura dati
     * @param v valore da settare
     */
    public void setValue(double v) {
        value.add(v);
    }

    /**
     * Metodo per settare i valori alla struttura dati
     * @param v valori da settare
     */
    public void setValue(double[] v) {
        for (int i=0; i<v.length; i++) {
            value.add(v[i]);
        }
    }

    public Timestamp getLastSampleTime() {
        return lastSampleTime;
    }

    public void setLastSampleTime(Timestamp time) {
        lastSampleTime = time;
    }

    public void printValue() {
        Log.i("VALUE",id + " value: " + value + " timestamp: " + lastSampleTime.toString());
    }
}
