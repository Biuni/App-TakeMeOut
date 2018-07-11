package app_library.dijkstra;

/**
 * Created by User on 05/07/2018.
 */

// arco per l'algoritmo di Dijkstra che rappresenta un tratto che collega due beacon
public class Edge {

    // nodo finale arco
    private final Vertex target;

    // peso dell'arco
    private final double weight;

    // costruttore dell'arco
    public Edge(Vertex argTarget, double argWeight)
    {
        target = argTarget;
        weight = argWeight;
    }

    public Vertex getTarget() {
        return target;
    }

    public double getWeight() {
        return weight;
    }
}
