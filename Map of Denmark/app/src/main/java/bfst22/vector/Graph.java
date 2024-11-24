package bfst22.vector;

//Not done yet

import java.util.*;

public class Graph {
    int vertexCount; //Number of vertices.
    int edgeCount; //Number of edges
    List<PolyPoint> nodes = new ArrayList<>();

    Map<PolyPoint, LinkedList<Edge>> adjMap = new HashMap<>();
    Map<PolyPoint, Integer> indexMap = new HashMap<>();
    Map<Integer, PolyPoint> polyMap = new HashMap<>();
    int index = 0;

    public void add(List<PolyPoint> nodes){
        this.nodes.addAll(nodes);
    }

    public void generate(){
        vertexCount = this.nodes.size();
        for(int i = 0; i < vertexCount; i++){
            PolyPoint node = this.nodes.get(i);
            if(adjMap.get(node) == null){
                adjMap.put(node,new LinkedList<Edge>());
                indexMap.put(node, index);
                polyMap.put(index, node);
                index++;
            }

        }
    }

    public void clearList(){
        this.nodes.clear();
    }

    public void addEdge(PolyPoint from, PolyPoint to, float weight){
        Edge e = new Edge(from, to, setWeight(weight));

        if(!adjMap.containsKey(from)){
            addVertex(from);
            indexMap.put(from,++index);
            polyMap.put(index,from);

            vertexCount++;
        }
        if(!adjMap.containsKey(to)){
            addVertex(to);
            indexMap.put(from,++index);
            polyMap.put(index,from);

            vertexCount++;
        }
        adjMap.get(from).add(e);
        edgeCount++;
    }

    public void addVertex(PolyPoint node){
        adjMap.put(node,new LinkedList<Edge>());
    }

    public Iterable<Edge> edges() {
        Bag<Edge> bagList = new Bag<>();
        for(int v = 0; v < vertexCount; v++){
            if(polyMap.get(v) != null){
                for(Edge e : adjMap.get(polyMap.get(v))){
                    bagList.add(e);
            }

            }
        }
        return bagList;
    }

    public Iterable<Edge> adj(PolyPoint v){
        return adjMap.get(v);
    }


    // Getters & Setters method for edgeCount and vertexCount
    public void setNodecount(int nodecount){
        if(vertexCount == 0) vertexCount = nodecount;
    }

    public int getVertexCount(){
        return this.vertexCount;
    }

    public int getEdgeCount() {
        return this.edgeCount;
    }

        public Map<PolyPoint, LinkedList<Edge>> getAdjMap()
        {
            return adjMap;
        }

        public float setWeightDistance (PolyPoint from, PolyPoint to,float speedlimit){
            Distance d = new Distance();
            return d.haversineFormula(from, to) / speedlimit;
        }

        public float setWeight(float weight){
            return weight;
        }

    }
