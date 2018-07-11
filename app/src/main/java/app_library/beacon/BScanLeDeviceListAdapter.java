package app_library.beacon;

/**
 * Created by User on 09/07/2018.
 */

import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import app_library.MainApplication;

/**
 Classe di supporto a BeaconScanner utilizzata per la gestione e l'interpretazione dei dispositivi rintracciati dallo scan
 */

// Adapter per gestire i dispositivi identificati durante lo scanner.
public class BScanLeDeviceListAdapter {

    //beacon più vicino all'utente
    private BluetoothDevice currentBeacon;

    private String TAG2 = "LeDeviceAdapter";
    //Hashmap di dispositivi estratti dallo scan (K: potenza del segnali RSSI, dispositivo trovato dallo scan)
    private TreeMap<Integer,BluetoothDevice> mLeDevices;

    /**
     * Costruisce l'adapter per identificare i dispositivi estratti dallo scan
     */
    public BScanLeDeviceListAdapter() {
        super();
        mLeDevices = new TreeMap<>(Collections.<Integer>reverseOrder());
    }
    /**
     * Metodo per aggiungere un dispositivo alla lista di quelli trovati (solamente nel caso in cui non sia già presente)
     */
    public void addDevice(BluetoothDevice device, int rssi) {
        if (!mLeDevices.containsValue(device)) {
            mLeDevices.put(rssi,device);
            Log.i(TAG2,"device: " + device.getAddress() + " rssi: " + rssi);
        }
    }

    /**
     * Metodo che calcola e restituisce il beacon più vicino fra quelli trovati
     */
    public BluetoothDevice selectedDevice() {

        BluetoothDevice b = null;

        Iterator it = mLeDevices.entrySet().iterator();

        //scandisce la lista in base alla distanza rispetto all'utente, finchè non trova un beacon presente nel CSV o finchè
        //non termina la lista
        while (b==null && it.hasNext()) {

            Map.Entry entry = (Map.Entry) it.next();
            BluetoothDevice dev = (BluetoothDevice) entry.getValue();

            Iterator iterator = MainApplication.getFloors().entrySet().iterator();
            boolean beaconFound = false;

            while (!beaconFound && iterator.hasNext())
            {
                Map.Entry pair = (Map.Entry) iterator.next();

                ArrayList<String> listBeaconFloor = MainApplication.getFloors().get(pair.getKey().toString()).getListNameRoomOrBeacon(false);

                for (int i = 0; i < listBeaconFloor.size() && !beaconFound; i++)
                {
                    if (listBeaconFloor.get(i).equals(dev.getAddress()))
                        beaconFound = true;
                }
            }

            //valuta se il beacon trovato è presente nella lista di quelli salvati nel CSV. Se si allora si è trovato il
            //dispositivo più vicino
            if(beaconFound) {
                b = dev;
            }
            else {
                Toast.makeText(MainApplication.getCurrentActivity().getApplicationContext(),
                        " Si è individuato un sensore non presente nel documento, " +
                                " dovresti aggiornare il file", Toast.LENGTH_SHORT).show();
            }
        }

        currentBeacon = b;
        return b;
    }

    /**
     * Metodo che restituisce il beacon più vicino rispetto all'utente
     * @return beacon più vicino rispetto all'utente
     */
    public BluetoothDevice getCurrentBeacon() {
        return currentBeacon;
    }
    /**
     * Metodo che setta il dispositivo più vicino
     * @param b, dispositivo bluetooth da impostare con currentBeacon
     */
    public void setCurrentBeacon(BluetoothDevice b) {
        currentBeacon = b;
    }
    /**
     * Metodo cancella tutti i dispositivi trovati dalla lista
     */
    public void clear() {
        mLeDevices.clear();
    }

    /**
     * Metodo che restituisce il numero di dispositivi trovati
     * @return numero di dispositivi trovati
     */
    public int getCount() {
        return mLeDevices.size();
    }
}
