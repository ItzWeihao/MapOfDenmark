package bfst22.vector;
import java.util.*;

public class DijkstraSP {

    PolyPoint start;
    PolyPoint target;
    Graph g;

    Map<PolyPoint, Double> distanceMap;
    Map<PolyPoint, Edge> edgeMap;
    IndexMinPQ<Double> pq;

    public DijkstraSP(Graph g, PolyPoint start, PolyPoint target, VehicleType vehicleType){
        this.start = start;
        this.target = target;
        this.g = g;

        distanceMap = new HashMap<>();
        edgeMap = new HashMap<>();

        for(int v = 0; v < g.getVertexCount(); v++){
            if(distanceMap.get(g.polyMap.get(v)) == null){
                distanceMap.put(g.polyMap.get(v),Double.POSITIVE_INFINITY);
            }

        }
        distanceMap.put(start,0.0);

        pq = new IndexMinPQ<>(g.getVertexCount());
        pq.insert(g.indexMap.get(start),distanceMap.get(start));
        while (!pq.isEmpty()){
            PolyPoint v = g.polyMap.get(pq.delMin());
            switch(vehicleType){
                case MOTORCAR:
                    for(Edge e : g.adj(v)){
                        if(e.getFrom().motorVehicle = true){
                            relax(g,e,target);
                        }

                    }
                    break;
                case FOOT:
                    for(Edge e : g.adj(v)){
                        if(e.getFrom().foot = true){
                            relax(g,e,target);
                        }
                    }
                    break;
                case BICYCLE:
                    for(Edge e : g.adj(v)){
                        if(e.getFrom().bicycle = true){
                            relax(g,e,target);
                        }
                    }
                    break;
            }

        }

    }

    public float h(PolyPoint start, PolyPoint target){
        Distance d = new Distance();
        return d.haversineFormula(start,target);
    }

    public void relax(Graph g, Edge e, PolyPoint target){
        PolyPoint v = e.getFrom();
        PolyPoint w = e.getTo();
        if(distanceMap.get(w) != null && distanceMap.get(v) != null) {
            if (distanceMap.get(w)  > distanceMap.get(v) + e.getWeight()) {
                distanceMap.put(w, distanceMap.get(v) + e.getWeight());
                edgeMap.put(w, e);
                double priority = distanceMap.get(w) + h(w, target);
                if (pq.contains(g.indexMap.get(w))) {
                    pq.decreaseKey(g.indexMap.get(w), priority);
                } else {
                    pq.insert(g.indexMap.get(w), priority);
                }
            }
        }
    }


    public double distTo(PolyPoint v){
        return distanceMap.get(v);
    }

    public boolean hasPathTo(PolyPoint v){
        return distanceMap.get(v) < Double.POSITIVE_INFINITY;
    }

    public Iterable<Edge> pathTo(PolyPoint v){
        if(!hasPathTo(v)) return null;
        Stack<Edge> path = new Stack<>();
        for(Edge e = edgeMap.get(v); e != null; e = edgeMap.get(e.getFrom())){
            path.push(e);
        }
        return path;
    }

    public ArrayList<Edge> pathToList(Iterable<Edge> path){
        ArrayList<Edge> pathList = new ArrayList<>();
        for(Edge e : path){
            pathList.add(e);
        }
        return pathList;
    }

    /* Test Function */
    public Iterable<String> pathToString(Iterable<Edge> path){
        Stack<String> pathToString = new Stack<>();
        for(Edge e : path){
            long from = e.getFrom().id;
            long to = e.getTo().id;
            float weight = e.getWeight();
            String output = from + "->" + to + "  " + weight;
            pathToString.push(output);
        }
        return pathToString;
    }



}
