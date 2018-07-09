package app_library.dijkstra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import app_library.MainApplication;

/**
 * Created by User on 05/07/2018.
 */

public class Dijkstra {

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

    private static List<Vertex> computeShortestPathTo(Vertex target)
    {
        List<Vertex> path = new ArrayList<Vertex>();
        for (Vertex vertex = target; vertex != null; vertex = vertex.getPrevious())
            path.add(vertex);
        Collections.reverse(path);
        return path;
    }

    public static String[] getShortestPathFromTo(String sourceVertexName, String targetVertexName, Vertex[] vertexArray)
    {
        Vertex[] vertexArrayCopy = vertexArray.clone();

        //List<String> stringPathOutput = new ArrayList<>();
        String[] stringPathArrayOutput;
        int sourceVertexIndex = -1;
        int targetVertexIndex = -1;

        try
        {
            for (int i = 0; i < vertexArrayCopy.length && (sourceVertexIndex == -1 || targetVertexIndex == -1); i++)
            {
                if (vertexArrayCopy[i].getName().equals(sourceVertexName))
                    sourceVertexIndex = i;

                if (vertexArrayCopy[i].getName().equals(targetVertexName))
                    targetVertexIndex = i;
            }

            initializePathsFromSource(vertexArrayCopy[sourceVertexIndex]);

            List<Vertex> vertexPath = computeShortestPathTo(vertexArrayCopy[targetVertexIndex]);

            stringPathArrayOutput = new String[vertexPath.size()];

            /*for (int i = 0; i < vertexPath.size(); i++)
                stringPathOutput.add(vertexPath.get(i).getName());*/

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

    public static double getDistanceShortestPathFromTo(String sourceVertexName, String targetVertexName, Vertex[] vertexArray)
    {
        Vertex[] vertexArrayCopy = vertexArray.clone();

        double distancePathOutput = -1.0;
        int sourceVertexIndex = -1;
        int targetVertexIndex = -1;

        try
        {
            for (int i = 0; i < vertexArrayCopy.length && (sourceVertexIndex == -1 || targetVertexIndex == -1); i++)
            {
                if (vertexArrayCopy[i].getName().equals(sourceVertexName))
                    sourceVertexIndex = i;

                if (vertexArrayCopy[i].getName().equals(targetVertexName))
                    targetVertexIndex = i;
            }

            initializePathsFromSource(vertexArrayCopy[sourceVertexIndex]);
            distancePathOutput = vertexArrayCopy[targetVertexIndex].getMinDistance();
        }
        catch (Exception e)
        {

        }

        return distancePathOutput;
    }

    public static HashMap<String,Integer> getHashMapsNodeIndex()
    {
        HashMap<String,Integer> hashMapsNodeIndex = null;

        try
        {
            hashMapsNodeIndex = new HashMap<>();
            Iterator iterator = MainApplication.getFloors().entrySet().iterator();
            int index = 0;

            while (iterator.hasNext())
            {
                Map.Entry pair = (Map.Entry) iterator.next();

                ArrayList<String> listNameRoomCurrentFloor = MainApplication.getFloors().get(pair.getKey().toString()).getListNameRoomOrBeacon(true);

                for (int i = 0; i < listNameRoomCurrentFloor.size(); i++)
                {
                    hashMapsNodeIndex.put(listNameRoomCurrentFloor.get(i), index);
                    index++;
                }
            }
        }
        catch (Exception e)
        {
            hashMapsNodeIndex = null;
        }

        return hashMapsNodeIndex;
    }


    // hashMapsNodeIndex contiene un'associazione codice_stanza-indice_array_nodi
    public static String[] getShortestPathOfflineFromTo(String sourceVertexName, String targetVertexName, HashMap<String,Integer> hashMapsNodeIndex, ArrayList<String[]> listRouteData)
    {
        /*Vertex v1 = new Vertex("150WC1");
        Vertex v2 = new Vertex("150RAM");
        Vertex v3 = new Vertex("150S1");
        Vertex v4 = new Vertex("150R1");
        Vertex v5 = new Vertex("150EMRL");

        Vertex v6 = new Vertex("150BIB");
        Vertex v7 = new Vertex("150A3");
        Vertex v8 = new Vertex("150RL");



        v1.adjacencies = new Edge[]{ new Edge(v2, 35.69)};
        v2.adjacencies = new Edge[]{ new Edge(v6, 14.2496), new Edge(v3, 47.6839), new Edge(v1, 35.69)};
        v3.adjacencies = new Edge[]{ new Edge(v4, 11.192), new Edge(v7, 47.83), new Edge(v2, 47.6839)};
        v4.adjacencies = new Edge[]{ new Edge(v8, 34.57), new Edge(v5, 51.402), new Edge(v3, 11.192)};

        v5.adjacencies = new Edge[]{ new Edge(v4, 51.402) };
        v6.adjacencies = new Edge[]{ new Edge(v2, 14.2496) };
        v7.adjacencies = new Edge[]{ new Edge(v3, 47.83)};
        v8.adjacencies = new Edge[]{ new Edge(v4, 34.57) };

        Dijkstra.computePaths(v1);
        String outputMess = "Distanza: " + v5.minDistance;
        outputMess += (" Percorso: " + Dijkstra.getShortestPathTo(v5));


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(outputMess).setCancelable(false).setTitle("Percorso").setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        AlertDialog alertDialogMessage = builder.create();

        alertDialogMessage.show();*/







        /*List<Vertex> vertexList = new ArrayList<>();

        vertexList.add(new Vertex("150WC1"));
        vertexList.add(new Vertex("150RAM"));
        vertexList.add(new Vertex("150S1"));
        vertexList.add(new Vertex("150R1"));
        vertexList.add(new Vertex("150EMRL"));

        vertexList.add(new Vertex("150BIB"));
        vertexList.add(new Vertex("150A3"));
        vertexList.add(new Vertex("150RL"));



        vertexList.get(0).setAdjacencies(new Edge[]{ new Edge(vertexList.get(1), 35.69)});
        vertexList.get(1).setAdjacencies(new Edge[]{ new Edge(vertexList.get(5), 14.2496), new Edge(vertexList.get(2), 47.6839), new Edge(vertexList.get(0), 35.69)});
        vertexList.get(2).setAdjacencies(new Edge[]{ new Edge(vertexList.get(3), 11.192), new Edge(vertexList.get(6), 47.83), new Edge(vertexList.get(1), 47.6839)});
        vertexList.get(3).setAdjacencies(new Edge[]{ new Edge(vertexList.get(7), 34.57), new Edge(vertexList.get(4), 51.402), new Edge(vertexList.get(2), 11.192)});

        vertexList.get(4).setAdjacencies(new Edge[]{ new Edge(vertexList.get(3), 51.402) });
        vertexList.get(5).setAdjacencies(new Edge[]{ new Edge(vertexList.get(1), 14.2496) });
        vertexList.get(6).setAdjacencies(new Edge[]{ new Edge(vertexList.get(2), 47.83)});
        vertexList.get(7).setAdjacencies(new Edge[]{ new Edge(vertexList.get(3), 34.57) });


        List<String> stringPathOutput = Dijkstra.getShortestPathFromTo("150WC1", "150EMRL", vertexList);
        double distancePathOutput = Dijkstra.getDistanceShortestPathFromTo("150WC1", "150EMRL", vertexList);

        String outputMess = "Distanza: " + distancePathOutput + " Percorso: ";

        if (stringPathOutput != null)
        {
            for (int i = 0; i < stringPathOutput.size(); i++)
                outputMess += (stringPathOutput.get(i) + "-");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(outputMess).setCancelable(false).setTitle("Percorso").setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        AlertDialog alertDialogMessage = builder.create();

        alertDialogMessage.show();*/


        //List<String> stringPathOutput;
        String[] stringPathArrayOutput;

        try
        {
            Vertex[] vertexArray = new Vertex[hashMapsNodeIndex.keySet().size()];
            List<Edge>[] edgeArray = new ArrayList[hashMapsNodeIndex.keySet().size()];

            Iterator iterator = hashMapsNodeIndex.entrySet().iterator();
            int index = 0;

            while (iterator.hasNext())
            {
                Map.Entry pair = (Map.Entry) iterator.next();

                vertexArray[index] = new Vertex(pair.getKey().toString());
                index++;
            }

            for (int i = 0; i < edgeArray.length; i++)
                edgeArray[i] = new ArrayList<>();

            for (int i = 0; i < listRouteData.size(); i++)
            {
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

                if (currentPeople >= currentLOS)
                    currentPLOS = 1000;
                else
                    currentPLOS = 0;

                double currentEdgeWeight = currentPLOS + (currentPv * currentV) + (currentPr * currentR) + (currentPk * currentK) + (currentPl * currentL);

                edgeArray[hashMapsNodeIndex.get(currentNodeStart)].add(new Edge(vertexArray[hashMapsNodeIndex.get(currentNodeEnd)], currentEdgeWeight));
                edgeArray[hashMapsNodeIndex.get(currentNodeEnd)].add(new Edge(vertexArray[hashMapsNodeIndex.get(currentNodeStart)], currentEdgeWeight));
            }

            for (int i = 0; i < vertexArray.length; i++)
                vertexArray[i].setAdjacencies(edgeArray[i].toArray(new Edge[edgeArray[i].size()]));

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
