package app_library.user;

/**
 * Classe che contiene tutte le informazioni di un utente
 */

public class UserProfile {

    // mail dell'utente
    String mail;

    // nome e cognome utente
    String nameSurname;

    // uuid dell'utente
    String uuid;

    //classe costruttore di un'istanza di un profilo di un utente
    public UserProfile(String mail, String nameSurname, String uuid){

        this.mail = mail;
        this.nameSurname = nameSurname;
        this.uuid = uuid;
    }


    public String getMail() {
        return mail;
    }

    public String getNameSurname() {
        return nameSurname;
    }

    public String getUuid() {
        return uuid;
    }
}
