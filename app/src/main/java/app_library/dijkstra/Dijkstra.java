package app_library.dijkstra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import app_library.MainApplication;

/**
 * Created by User on 05/07/2018.
 */

// classe per l'esecuzione dell'algoritmo di Dijkstra
public class Dijkstra {

    // inizializzazione dei percorsi a partire dal nodo sorgente
    private static void initializePathsFromSource(Vertex source)
    {
        source.setMinDistance(0.);
        PriorityQueue<Vertex> vertexQueue = new PriorityQueue<Vertex>();
        vertexQueue.add(source);

        while (!vertexQueue.isEmpty()) {
            Vertex u = vertexQueue.poll();

            // Visit each edge exiting u
            for (Edge e : u.getAdjacencies())
            {
                Vertex v = e.getTarget();
                double weight = e.getWeight();
                double distanceThroughU = u.getMinDistance() + weight;
                if (distanceThroughU < v.getMinDistance()) {
                    vertexQueue.remove(v);
                    v.setMinDistance(distanceThroughU);
                    v.setPrevious(u);
                    vertexQueue.add(v);
                }
            }
        }
    }

    // costruzione del percorso a costo minimo a partire dal nodo di destinazione e tornando ai nodi precedenti
    private static List<Vertex> computeShortestPathTo(Vertex target)
    {
        List<Vertex> path = new ArrayList<Vertex>();

        // si prende il nodo precedente dal corrente costruendo il percorso
        for (Vertex vertex = target; vertex != null; vertex = vertex.getPrevious())
            path.add(vertex);
        Collections.reverse(path);
        return path;
    }

    // calcolo del percorso a costo minimo a partire dal nodo sorgente al nodo di destinazione considerando tutti i nodi passati nel parametro array
    public static String[] getShortestPathFromTo(String sourceVertexName, String targetVertexName, Vertex[] vertexArray)
    {
        // copia dell'array dei nodi di input
        Vertex[] vertexArrayCopy = vertexArray.clone();

        // percorso minimo di output
        String[] stringPathArrayOutput;

        // indici array sorgente e destinazione non definiti
        int sourceVertexIndex = -1;
        int targetVertexIndex = -1;

        try
        {
            // si trovano gli indici dell'array riferiti a sorgente e destinazione
            for (int i = 0; i < vertexArrayCopy.length && (sourceVertexIndex == -1 || targetVertexIndex == -1); i++)
            {
                if (vertexArrayCopy[i].getName().equals(sourceVertexName))
                    sourceVertexIndex = i;

                if (vertexArrayCopy[i].getName().equals(targetVertexName))
                    targetVertexIndex = i;
            }

            // inizializzazione dei percorsi a partire dal nodo sorgente
            initializePathsFromSource(vertexArrayCopy[sourceVertexIndex]);

            // costruzione del percorso a partire dal nodo di destinazione e tornando ai nodi precedenti
            List<Vertex> vertexPath = computeShortestPathTo(vertexArrayCopy[targetVertexIndex]);

            // costruzione dell'array del percorso minimo di output
            stringPathArrayOutput = new String[vertexPath.size()];

            for (int i = 0; i < vertexPath.size(); i++)
                stringPathArrayOutput[i] = vertexPath.get(i).getName();
        }
        catch (Exception e)
        {
            //stringPathOutput = null;
            stringPathArrayOutput = null;
        }

        return stringPathArrayOutput;
    }

    // calcolo del costo del percorso a costo minimo a partire dal nodo sorgente al nodo di destinazione considerando tutti i nodi passati nel parametro array
    public static double getDistanceShortestPathFromTo(String sourceVertexName, String targetVertexName, Vertex[] vertexArray)
    {
        // copia dell'array dei nodi di input
        Vertex[] vertexArrayCopy = vertexArray.clone();

        // distanza minima non definita
        double distancePathOutput = -1.0;

        // indici array sorgente e destinazione non definiti
        int sourceVertexIndex = -1;
        int targetVertexIndex = -1;

        try
        {
            // si trovano gli indici dell'array riferiti a sorgente e destinazione
            for (int i = 0; i < vertexArrayCopy.length && (sourceVertexIndex == -1 || targetVertexIndex == -1); i++)
            {
                if (vertexArrayCopy[i].getName().equals(sourceVertexName))
                    sourceVertexIndex = i;

                if (vertexArrayCopy[i].getName().equals(targetVertexName))
                    targetVertexIndex = i;
            }

            // inizializzazione dei percorsi a partire dal nodo sorgente
            initializePathsFromSource(vertexArrayCopy[sourceVertexIndex]);

            // determinazione della distanza minima del percorso
            distancePathOutput = vertexArrayCopy[targetVertexIndex].getMinDistance();
        }
        catch (Exception e)
        {

        }

        return distancePathOutput;
    }

    // restituzione della lista contenente i nomi dei nodi ovvero delle stanze
    public static ArrayList<String> getListNodeRoomName()
    {
        // lista di output con i nomi delle stanze
        ArrayList<String> listNodeRoomNameOutput = null;

        try
        {
            listNodeRoomNameOutput = new ArrayList<>();
            Iterator iterator = MainApplication.getFloors().entrySet().iterator();

            // si scorre l'hasmap dei piani della classe MainApplication costruendo la lista con i nomi delle stanze di output
            while (iterator.hasNext())
            {
                Map.Entry pair = (Map.Entry) iterator.next();

                ArrayList<String> listNodeRoomNameCurrentFloor = MainApplication.getFloors().get(pair.getKey().toString()).getListNameRoomOrBeacon(true);

                for (int i = 0; i < listNodeRoomNameCurrentFloor.size(); i++)
                {
                    listNodeRoomNameOutput.add(listNodeRoomNameCurrentFloor.get(i));
                }
            }
        }
        catch (Exception e)
        {
            listNodeRoomNameOutput = null;
        }

        return listNodeRoomNameOutput;
    }


    // calcolo del percorso a costo minimo a partire dal nodo sorgente al nodo di destinazione utilizzato per la modalità offline
    public static String[] getShortestPathOfflineFromTo(String sourceVertexName, String targetVertexName, ArrayList<String> listNodeRoomNameOutput, ArrayList<String[]> listRouteData)
    {
        // percorso minimo di output
        String[] stringPathArrayOutput;

        try
        {
            // si crea sia l'array dei nodi che degli archi con numero di elementi che dipende dalla dimensione del parametro lista di nodi
            Vertex[] vertexArray = new Vertex[listNodeRoomNameOutput.size()];
            List<Edge>[] edgeArray = new ArrayList[listNodeRoomNameOutput.size()];

            for (int i = 0; i < listNodeRoomNameOutput.size(); i++)
            {
                vertexArray[i] = new Vertex(listNodeRoomNameOutput.get(i));
                edgeArray[i] = new ArrayList<>();
            }

            // si aggiungono all'array degli archi tutti gli archi in avanti e all'indietro che presenta un nodo in base alla lista di input degli archi
            for (int i = 0; i < listRouteData.size(); i++)
            {
                // si estraggono le informazioni dell'arco corrente
                String currentNodeStart = listRouteData.get(i)[0];
                String currentNodeEnd = listRouteData.get(i)[1];

                int currentPeople = Integer.parseInt(listRouteData.get(i)[2]);
                int currentLOS = Integer.parseInt(listRouteData.get(i)[3]);
                int currentV = Integer.parseInt(listRouteData.get(i)[4]);
                int currentR = Integer.parseInt(listRouteData.get(i)[5]);
                int currentK = Integer.parseInt(listRouteData.get(i)[6]);
                double currentL = Double.parseDouble(listRouteData.get(i)[7]);
                double currentPv = Double.parseDouble(listRouteData.get(i)[8]);
                double currentPr = Double.parseDouble(listRouteData.get(i)[9]);
                double currentPk = Double.parseDouble(listRouteData.get(i)[10]);
                double currentPl = Double.parseDouble(listRouteData.get(i)[11]);

                int currentPLOS;

                // meccanismo che permette di lasciare o rimuovere archi per il calcolo del percorso
                if (currentPeople >= currentLOS)
                    currentPLOS = 1000;
                else
                    currentPLOS = 0;

                // calcolo del peso dell'arco
                double currentEdgeWeight = currentPLOS + (currentPv * currentV) + (currentPr * currentR) + (currentPk * currentK) + (currentPl * currentL);

                // inserimento dell'arco in avanti e all'indietro nell'array degli archi
                edgeArray[listNodeRoomNameOutput.indexOf(currentNodeStart)].add(new Edge(vertexArray[listNodeRoomNameOutput.indexOf(currentNodeEnd)], currentEdgeWeight));
                edgeArray[listNodeRoomNameOutput.indexOf(currentNodeEnd)].add(new Edge(vertexArray[listNodeRoomNameOutput.indexOf(currentNodeStart)], currentEdgeWeight));
            }

            for (int i = 0; i < vertexArray.length; i++)
                vertexArray[i].setAdjacencies(edgeArray[i].toArray(new Edge[edgeArray[i].size()]));

            // calcolo del percorso a costo minimo
            stringPathArrayOutput = Dijkstra.getShortestPathFromTo(sourceVertexName, targetVertexName, vertexArray);

        }
        catch (Exception e)
        {
            //stringPathOutput = null;
            stringPathArrayOutput = null;
        }

        return stringPathArrayOutput;
    }

}
