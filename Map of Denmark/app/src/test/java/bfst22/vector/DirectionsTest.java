package bfst22.vector;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import static org.junit.jupiter.api.Assertions.*;

class DirectionsTest {
    Distance distance = new Distance();
    Directions directions = new Directions();
    Graph g;
    DijkstraSP sp1;
    DijkstraSP sp2;
    List<PolyPoint> path1;
    List<PolyPoint> path2;
    ArrayList<Edge> pathList;
    VehicleType vehicleType;


    PolyPoint A = new PolyPoint(1,(float)12.6039900,(float)55.6390580);
    PolyPoint B = new PolyPoint(2,(float)12.6041791,(float)55.6385458);
    PolyPoint C = new PolyPoint(3,(float)12.5999974,(float)55.6380447);
    PolyPoint D = new PolyPoint(4, (float)12.5993906,(float)55.6397972);
    PolyPoint E = new PolyPoint(5,(float)12.6015500,(float)55.6400283);

    @BeforeEach void setUp(){
        A.address = "Street 1";
        B.address = "Street 2";
        C.address = "Street 3";
        D.address = "Street 4";
        E.address = "Street 5";
        path1 = new ArrayList<>();
        path2 = new ArrayList<>();
        path1.add(A);
        path1.add(B);
        path1.add(C);
        path1.add(D);
        path1.add(E);

        path2.add(E);
        path2.add(D);
        path2.add(C);
        path2.add(B);
        path2.add(A);

        g = new Graph();
        g.add(path1);
        g.add(path2);
        g.generate();


        g.addEdge(A,B, distance.haversineFormula(A,B));
        g.addEdge(B,C, distance.haversineFormula(B,C));
        g.addEdge(C,D, distance.haversineFormula(C,D));
        g.addEdge(D,E, distance.haversineFormula(D,E));

        g.addEdge(E,D, distance.haversineFormula(E,D));
        g.addEdge(D,C, distance.haversineFormula(D,C));
        g.addEdge(C,B, distance.haversineFormula(C,B));
        g.addEdge(B,A, distance.haversineFormula(B,A));

        pathList = new ArrayList<>();

    }

    @Test void path1Test(){
        sp1 = new DijkstraSP(g,A,E,vehicleType.MOTORCAR);
        for(Edge e : sp1.pathTo(E)){
                pathList.add(e);
        }
        float difference;
        for(int i = 0; i < pathList.size() - 1; i++){
            if(i == 0){
                difference = directions.getAngleDifference(pathList.get(i).getFrom(), pathList.get(i).getTo()
                        , pathList.get(i).getFrom(), pathList.get(i).getTo());
                System.out.println(directions.turn(directions.getAngle(pathList.get(i).getFrom(), pathList.get(i).getTo()), difference
                        , pathList.get(i+1).getFrom(), pathList.get(i+1).getTo()));

            }
            else if(i > 0) {
                difference = directions.getAngleDifference(pathList.get(i).getFrom(), pathList.get(i).getTo()
                        , pathList.get(i+1).getFrom(), pathList.get(i+1).getTo());
                System.out.println(directions.turn(directions.getAngle(pathList.get(i).getFrom(), pathList.get(i).getTo()), difference
                        , pathList.get(i+1).getFrom(), pathList.get(i+1).getTo()));
            }

        }
        assertEquals("idk",pathList.toString());
    }

    @Test void path2Test(){
        sp1 = new DijkstraSP(g,E,A,vehicleType.MOTORCAR);
        Stack<Edge> path = new Stack<>();
        for(Edge e : sp1.pathTo(A)){

            path.push(e);
        }
        assertEquals("idk",sp1.pathToString(path));

    }

    @AfterEach void tearDown (){

    }

}