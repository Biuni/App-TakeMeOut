package app_library.dijkstra;

/**
 * Created by User on 05/07/2018.
 */

// nodo per l'algoritmo di Dijkstra che rappresenta una stanza con associato il relativo beacon
public class Vertex implements Comparable<Vertex> {

    // nome del nodo
    private final String name;

    // archi associati al nodo
    private Edge[] adjacencies;

    // distanza minima
    private double minDistance = Double.POSITIVE_INFINITY;

    // nodo precedente
    private Vertex previous;

    // costruttore a cui viene passato il nome del nodo
    public Vertex(String argName)
    {
        name = argName;
    }

    // ovveride del metodo toString
    public String toString() { return name; }

    // metodo di comparazione con un altro nodo di Comparable
    public int compareTo(Vertex other)
    {
        return Double.compare(minDistance, other.getMinDistance());
    }

    public String getName() {
        return name;
    }

    public Edge[] getAdjacencies() {
        return adjacencies;
    }

    public void setAdjacencies(Edge[] adjacencies) {
        this.adjacencies = adjacencies;
    }

    public double getMinDistance() {
        return minDistance;
    }

    public void setMinDistance(double minDistance) {
        this.minDistance = minDistance;
    }

    public Vertex getPrevious() {
        return previous;
    }

    public void setPrevious(Vertex previous) {
        this.previous = previous;
    }

}
