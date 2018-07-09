package app_library.validation;

/**
 * Classe che contiene tutti i pattern per compiere un controllo RegEx sui campi inseriti nelle varie form dell'utente
 */

public abstract class FormControl {
        //lunghezza minima del numero di telefono
    private static final int phone_lenght = 8;
        //lunghezza minima della password
    private static final int pass_lenght = 8;
        //lunghezza stringa provincia
    private static final int provence_lenght = 2;

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

    //controllo del codice fiscale
    public static boolean PersonalNumberControl(String s) {
        boolean b;
        String pattern= "^[A-Z]{6}[0-9]{2}[A-Z][0-9]{2}[A-Z][0-9]{3}[A-Z]$";
        if(s.matches(pattern)) b = true;
            else b = false;
        return b;
    }

    //controlla che ci siano solamente numeri
    public static boolean numberControl(String s) {
        boolean b;
        String pattern= "^[0-9]*$";
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

    //controllo della lunghezza della provincia, cioè se è nella forma AA (due lettere)
    public static boolean provenceControl(String s) {
        boolean b;
        if (s.length()!=provence_lenght) b = false;
        else b = true;
        return b;
    }

    //controllo del telefono (saranno tutti numeri) se la lunghezza è minore di phone_lenght
    public static boolean phoneControl(String s) {
        boolean b;
        if (s.length()<phone_lenght) b = false;
        else b = true;
        return b;
    }
}
