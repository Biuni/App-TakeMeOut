package app_library.sharedstorage;

import java.util.ArrayList;

/**
 * Classe padre che permette di aggiungere una serie di subscriber alla lista (listeners) e metodo updateInformation che
 * permette di richiamare tutte le retrive per prendere una copia aggiornata.
 */

public class SharedData {

    protected ArrayList<DataListener> listeners = new ArrayList<DataListener>();

    public void addDataListener(DataListener listener) {
        listeners.add(listener);

    }

    public ArrayList<DataListener> getListeners() {
        return listeners;
    }

    public void updateInformation() {
        for (DataListener dataListener : listeners) {
            dataListener.retrive();
        }
    }
}
