package app_library.dijkstra;

/**
 * Created by User on 05/07/2018.
 */

public class Edge {

    private final Vertex target;

    private final double weight;

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
