package app_library.sharedstorage;

/**
 * Classe statica che contiene le strutture dati statiche a cui accedereanno i vari subscribers
 */

public class Data {

        //struttura dati per memorizzare la posizione dell'utente
    private static UserPositions userPosition = new UserPositions();
        //struttura dati per memorizzare le diverse notifiche
    //private static Notification notification = new Notification();

    public static UserPositions getUserPosition() {
        return userPosition;
    }

    /*public static Notification getNotification() {
        return notification;
    }*/
}
