package bfst22.vector;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class DijkstraShortestPath {
    private Map<PolyPoint, Boolean> markMap;
    private Map<PolyPoint,Double> distanceMap;
    private Map<PolyPoint, Edge> edgeMap;
    private MinPQ<Edge> pq;

    private class compareDistance implements Comparator<Edge> {
        public int compare(Edge e, Edge f){
            double distance1 = distanceMap.get(e.getFrom()) + e.getWeight();
            double distance2 = distanceMap.get(f.getFrom()) + f.getWeight();
            return Double.compare(distance1, distance2);
        }
    }

    public DijkstraShortestPath(Graph g, PolyPoint start, VehicleType vehicleType ) {
        pq = new MinPQ<Edge>(new compareDistance());
        markMap = new HashMap<>();
        edgeMap = new HashMap<>();
        distanceMap = new HashMap();


        for (int v = 0; v < g.getVertexCount(); v++) {
            distanceMap.put(g.polyMap.get(v), Double.POSITIVE_INFINITY);
            markMap.put(g.polyMap.get(v),false);
        }
        distanceMap.put(start, 0.0);
        relax(g, start);

        while (!pq.isEmpty()) {
            Edge e = pq.delMin();
            PolyPoint v = e.getFrom(), w = e.getTo();
            if (!markMap.get(w)) relax(g, w);
        }
    }

    /*I haven't implemented the heruristic yet*/
    double h(PolyPoint start, PolyPoint target){
        Distance d = new Distance();
        return d.haversineFormula(start,target);
    }

        private void relax(Graph g, PolyPoint v){
            markMap.put(v, true);
            for(Edge e : g.adj(v)){
                PolyPoint w = e.getTo();
                /*
                if(distanceMap.get(w) == null){
                    distanceMap.put(w, Double.POSITIVE_INFINITY);
                }
                */
                if(distanceMap.get(w) > distanceMap.get(v) + e.getWeight()) {
                    distanceMap.put(w,distanceMap.get(v) + e.getWeight());
                    edgeMap.put(w,e);
                    pq.insert(e);
                }
            }
        }

        public double distTo(PolyPoint v){
            return distanceMap.get(v);
        }

        public boolean hasPathTo(PolyPoint v){
            return markMap.get(v);
        }

        public Iterable<Edge> pathTo(PolyPoint v){
            //if(!hasPathTo(v)) return null;
            Stack<Edge> path = new Stack<>();
            for(Edge e = edgeMap.get(v); e != null; e = edgeMap.get(e.getFrom())){
                path.push(e);
            }
            return path;
        }



    }



