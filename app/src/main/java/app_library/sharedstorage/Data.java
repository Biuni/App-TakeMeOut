package app_library.sharedstorage;

/**
 * Classe statica che contiene le strutture dati statiche a cui accedereanno i vari subscribers
 */

public class Data {

        //struttura dati per memorizzare la posizione dell'utente
    private static UserPositions userPosition = new UserPositions();

    public static UserPositions getUserPosition() {
        return userPosition;
    }

}
