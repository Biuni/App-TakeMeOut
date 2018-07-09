package app_library.utility;


/**
 * Questa classe astratta è stata usata per modellare le macchine a stati
 */

public abstract class StateMachine {
        //stato attuale in cui si trova
    protected int currentState;
        //stato immediatamente precedente
    protected int previousState;
        //indica che la state machine è in funzione o meno
    protected boolean running;

    /**
     * Costruttore della macchina a stati
     */
    public StateMachine () {
        currentState = 0;
        previousState = 0;
        running = true;
    }

    /**
     * Metodo che restituisce lo stato in cui si trova la macchina
     * @return stato in cui si trova la macchina
     */
    public int getState() {
        return currentState;
    }

    /**
     * In base allo stato attuale ed alle condizioni in cui si trova la macchina, viene valutato lo stato successivo
     * @return stato successivo che dovrà assumere la macchina
     */
    protected int nextState() {
        int n = 0;
        return n;
    }

    /**
     * Vengono modificati i valori relativi allo stato attuale ed a quello precedente della macchina
     */
    protected void changeState(int next) {
        previousState = currentState;
        currentState = next;
    }

    /**
     * Vengono eseguite le istruzioni relative allo stato attuale
     */
    protected void executeState() {

    }


}
