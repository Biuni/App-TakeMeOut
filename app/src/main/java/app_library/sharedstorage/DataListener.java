package app_library.sharedstorage;

/**
 * Interfaccia che contiene due metodi:
 *
 * update: metodo che viene chiamato quando un subscriber deve aggiornare la struttura dati.
 * retrive: metodo chiamato in seguito ad una update che permette a tutti gli altri subscriber di prendersi la copia aggiornata del dato
 * contenuto nella struttura dati.
 */

public interface DataListener {

    public void update();

    public void retrive();
}
