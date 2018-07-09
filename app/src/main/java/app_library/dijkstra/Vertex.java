package app_library.dijkstra;

/**
 * Created by User on 05/07/2018.
 */

public class Vertex implements Comparable<Vertex> {

    private final String name;

    private Edge[] adjacencies;

    private double minDistance = Double.POSITIVE_INFINITY;

    private Vertex previous;

    public Vertex(String argName)
    {
        name = argName;
    }

    public String toString() { return name; }

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
