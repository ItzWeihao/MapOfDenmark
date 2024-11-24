package bfst22.vector;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import java.util.*;

public class DijkstraSPTest {
    List<PolyPoint> nodes = new ArrayList<>();
    Graph g;
    Distance d;
    DijkstraSP sp;
    int speedLimit = 75;
    VehicleType vehicleType = VehicleType.MOTORCAR;

    /*PolyPoint objects*/
        PolyPoint A = new PolyPoint(1,12,3);
        PolyPoint B = new PolyPoint(2, 10,4);
        PolyPoint C = new PolyPoint(3,8,6);
        PolyPoint D = new PolyPoint(4,7,8);
        PolyPoint E = new PolyPoint(5, 5,10);
        PolyPoint F = new PolyPoint(6,10,2);
        PolyPoint G = new PolyPoint(7,7,2);
        PolyPoint H = new PolyPoint(8, 7,4);
        PolyPoint I = new PolyPoint(9, 5,3);
        PolyPoint J = new PolyPoint(10, 5,6);
        PolyPoint K = new PolyPoint(11, 4,9);

    @BeforeEach void setUp(){
        System.out.println(nodes.isEmpty());
        d = new Distance();
        g = new Graph();

        nodes.add(A);
        nodes.add(B);
        nodes.add(C);
        nodes.add(D);
        nodes.add(E);
        nodes.add(F);
        nodes.add(G);
        nodes.add(H);
        nodes.add(I);
        nodes.add(J);
        nodes.add(K);

        g.add(nodes);
        g.generate();

        System.out.println("Vertex Count: " + g.getVertexCount());
        System.out.println("Edge Count: " + g.getEdgeCount());

        g.addEdge(g.nodes.get(0), g.nodes.get(1),g.setWeightDistance(g.nodes.get(0), g.nodes.get(1),speedLimit));
        g.addEdge(g.nodes.get(1), g.nodes.get(2),g.setWeightDistance(g.nodes.get(1), g.nodes.get(2),speedLimit));
        g.addEdge(g.nodes.get(2), g.nodes.get(3),g.setWeightDistance(g.nodes.get(2), g.nodes.get(3),speedLimit));
        g.addEdge(g.nodes.get(3), g.nodes.get(4),g.setWeightDistance(g.nodes.get(3), g.nodes.get(4),speedLimit));
        g.addEdge(g.nodes.get(0), g.nodes.get(5),g.setWeightDistance(g.nodes.get(0), g.nodes.get(5),speedLimit));
        g.addEdge(g.nodes.get(5), g.nodes.get(6),g.setWeightDistance(g.nodes.get(5), g.nodes.get(6),speedLimit));
        g.addEdge(g.nodes.get(6), g.nodes.get(7),g.setWeightDistance(g.nodes.get(6), g.nodes.get(7),speedLimit));
        g.addEdge(g.nodes.get(0), g.nodes.get(3),g.setWeightDistance(g.nodes.get(0), g.nodes.get(3),speedLimit));
        g.addEdge(g.nodes.get(0), g.nodes.get(7),g.setWeightDistance(g.nodes.get(0), g.nodes.get(7),speedLimit));
        g.addEdge(g.nodes.get(6), g.nodes.get(8),g.setWeightDistance(g.nodes.get(6), g.nodes.get(8),speedLimit));
        g.addEdge(g.nodes.get(6), g.nodes.get(9),g.setWeightDistance(g.nodes.get(6), g.nodes.get(9),speedLimit));
        g.addEdge(g.nodes.get(9), g.nodes.get(10),g.setWeightDistance(g.nodes.get(9), g.nodes.get(10),speedLimit));
        g.addEdge(g.nodes.get(8), g.nodes.get(9),g.setWeightDistance(g.nodes.get(8), g.nodes.get(9),speedLimit));
        g.addEdge(g.nodes.get(10), g.nodes.get(4),g.setWeightDistance(g.nodes.get(10), g.nodes.get(4),speedLimit));
        g.addEdge(g.nodes.get(7), g.nodes.get(4),g.setWeightDistance(g.nodes.get(7), g.nodes.get(4),speedLimit));
        g.addEdge(g.nodes.get(7), g.nodes.get(2),g.setWeightDistance(g.nodes.get(7), g.nodes.get(2),speedLimit));


        System.out.println("Vertex Count: " + g.getVertexCount());
        System.out.println("Edge Count: " + g.getEdgeCount());


    }
    @Test void dijkstraTest0to4(){
        sp = new DijkstraSP(g, g.nodes.get(0),g.nodes.get(4),vehicleType.MOTORCAR);
        System.out.println(sp.pathToString(sp.pathTo(g.nodes.get(4))));
        assertEquals("[4->5  4.1818423, 1->4  10.410109]","" + sp.pathToString(sp.pathTo(g.nodes.get(4))));
    }

    @Test void dijkstraTest0to10(){
        sp = new DijkstraSP(g,g.nodes.get(0),g.nodes.get(10),vehicleType.MOTORCAR);
        assertEquals("[10->11  4.675328, 7->10  6.601054, 6->7  4.447797, 1->6  3.3030636]","" + sp.pathToString(sp.pathTo(g.nodes.get(10))));
    }

    @Test void dijkstaTestNull(){
        sp = new DijkstraSP(g,g.nodes.get(1),g.nodes.get(7),vehicleType.MOTORCAR);
        assertNull(sp.pathTo(nodes.get(7)));
    }

    @Test void dijkstaTestFootNull(){
        sp = new DijkstraSP(g,g.nodes.get(0),g.nodes.get(10),vehicleType.FOOT);
    }

    @AfterEach void tearDown(){
        g = null;
        nodes = null;
        sp = null;
    }

}
