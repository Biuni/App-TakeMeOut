package app_library.beacon;

import android.util.Log;
import app_library.MainApplication;

/**
    Classe utilizzata per gestire le modalità di scan per la ricerca di beacon
 */

public class Setup {
        //condizioni in cui può trovarsi lo scan (EMERGENCY, nel caso in cui si presenti un'emergenza,
        // SEARCHING, nel caso in cui si stia cercando una stanza, NORMAL in tutti gli altri casi
    public static final String normalCondition = "NORMAL";
    public static final String emergencyCondition = "EMERGENCY";
    public static final String searchCondition = "SEARCHING";

        //durate degli scan per ogni possibile setup
    public static final long scanPeriodNormal = 3000l;
    public static final long scanPeriodSearching = 1500l;
    public static final long scanPeriodEmergency = 1000l;

        //durate della pausa fra due scan consecutivi per ogni possibile setup
    public static final long periodBetweenScanNormal = 15000l;
    public static final long periodBetweenScanSearching = 5000l;
    public static final long periodBetweenScanEmergency = 3000l;

        //id del setup selezionato
    private String state;
        //duranta dello scan per il setup selezionato
    private long SCAN_PERIOD;
        //duranta della pausa fra scan consecutivi per il setup selezionato
    private long STOP_PERIOD;
        //flag che indica se vada effettuata o meno la connessione al beacon
    private boolean analyzeBeacon;

    /**
     * Costruttore il Setup per lo scan, con le impostazioni di default (NORMAL CONDITION)
     */
    public Setup() {
        state = normalCondition;

        /*if (MainApplication.getOnlineMode() && MainApplication.getScanParameters()!=null) {
            SCAN_PERIOD = MainApplication.getScanParameters().get("scanPeriodNormal");
            STOP_PERIOD = MainApplication.getScanParameters().get("periodBetweenScanNormal");
        }
        else {
            SCAN_PERIOD = scanPeriodNormal;
            STOP_PERIOD = periodBetweenScanNormal;
        }*/

        SCAN_PERIOD = scanPeriodNormal;
        STOP_PERIOD = periodBetweenScanNormal;
        analyzeBeacon = true;
    }

    /**
     * Costruttore il Setup per lo scan, settando le impostazioni in base alle condition passate
     * @param condition, indica il tipo di setup con cui va creato lo scan
     */
    public Setup(String condition) {
        switch (condition) {
            case ("NORMAL"):
                state = normalCondition;
                /*if (MainApplication.getOnlineMode() && MainApplication.getScanParameters()!=null) {
                    SCAN_PERIOD = MainApplication.getScanParameters().get("scanPeriodNormal");
                    STOP_PERIOD = MainApplication.getScanParameters().get("periodBetweenScanNormal");
                }
                else {
                    SCAN_PERIOD = scanPeriodNormal;
                    STOP_PERIOD = periodBetweenScanNormal;
                }*/

                SCAN_PERIOD = scanPeriodNormal;
                STOP_PERIOD = periodBetweenScanNormal;
                analyzeBeacon = true;
                break;
            case ("EMERGENCY"):
                state = emergencyCondition;
                /*if (MainApplication.getOnlineMode() && MainApplication.getScanParameters()!=null) {
                    SCAN_PERIOD = MainApplication.getScanParameters().get("scanPeriodEmergency");
                    STOP_PERIOD = MainApplication.getScanParameters().get("periodBetweenScanEmergency");
                }
                else {
                    SCAN_PERIOD = scanPeriodEmergency;
                    STOP_PERIOD = periodBetweenScanEmergency;
                }*/

                SCAN_PERIOD = scanPeriodEmergency;
                STOP_PERIOD = periodBetweenScanEmergency;
                analyzeBeacon = false;
                break;
            case ("SEARCHING"):
                state = searchCondition;
                /*if (MainApplication.getOnlineMode() && MainApplication.getScanParameters()!=null) {
                    SCAN_PERIOD = MainApplication.getScanParameters().get("scanPeriodSearching");
                    STOP_PERIOD = MainApplication.getScanParameters().get("periodBetweenScanSearching");
                }
                else {
                    SCAN_PERIOD = scanPeriodSearching;
                    STOP_PERIOD = periodBetweenScanSearching;
                }*/

                SCAN_PERIOD = scanPeriodSearching;
                STOP_PERIOD = periodBetweenScanSearching;
                analyzeBeacon = false;
                break;
            default:
                Log.e("ERROR", "stato sbagliato");
                break;
        }
    }

    /**
     * Restituisce la durata dello scan per il setup selezionato
     */
    public long getScanPeriod() {
        return SCAN_PERIOD;
    }

    /**
     * Metodo che restituisce la durata della pausa fra due scan per il setup selezionato
     */
    public long getPeriodBetweenScan() {
        return STOP_PERIOD;
    }

    /**
     * Metodo che restituisce lo stato del setup corrispondente
     */
    public String getState() {
        return state;
    }
    /**
     * Metodo che restituisce il flag per sapere se si debba connettere o meno al beacon
     */
    public boolean mustAnalyze() {
        return analyzeBeacon;
    }


}