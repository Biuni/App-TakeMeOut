package app_library.validation;

/**
 * Classe che contiene tutti i pattern per compiere un controllo RegEx sui campi inseriti nelle varie form dell'utente
 */

public abstract class FormControl {

        //lunghezza minima della password
    private static final int pass_lenght = 8;

        //controlla che ci siano solamente lettere e spazi
    public static boolean letterControl(String s) {
        boolean b;
        String pattern= "^[a-zA-Z ]*$";
        if(s.matches(pattern)) b = true;
        else b = false;
        return b;
    }

    //controllo dell'indirizzo ip
    public static boolean ipControl(String s) {
        boolean b;
        String pattern = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
        if(s.matches(pattern)) b=true;
        else b = false;
        return b;
    }

    //controlla che ci siano solamente lettere, @ e punto
    public static boolean mailControl(String s) {
        boolean b;
        String pattern= "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        if(s.matches(pattern)) b = true;
        else b = false;
        return b;
    }

    //controllo della password sulla lunghezza
    public static boolean passwordControl(String s) {
        boolean b;
        if (s.length()<pass_lenght) b = false;
        else b = true;
        return b;
    }
}
