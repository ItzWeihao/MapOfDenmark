package bfst22.vector;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DistanceTest {
    PolyPoint A;
    PolyPoint B;
    PolyPoint C;
    PolyPoint DenmarkCoords;
    PolyPoint NorwayCoords;
    PolyPoint LatLon1;
    PolyPoint LatLon2;

    Distance d = new Distance();

    @BeforeEach void setUp(){
        A = new PolyPoint(1,12,3);
        B = new PolyPoint(2, 10,4);
        C = new PolyPoint(3,8,6);
        DenmarkCoords = new PolyPoint(1,(float)57.0337,(float)9.9166);
        NorwayCoords = new PolyPoint(2,(float)59.284073,(float)11.109403);
        LatLon1 = new PolyPoint(1,12,5);
        LatLon2 = new PolyPoint(2,33,22);
    }

    @Test void DistanceTest(){
        assertEquals("247.73",String.format("%.2f",d.haversineFormula(A,B)));
    }

    @Test void DistanceTest2(){
        String result = String.format("%.0f",d.haversineFormula(LatLon1,LatLon2));
        assertEquals("2906",result);
    }

    @Test void DistanceDenmarkNorway(){
        String result = String.format("%.0f",d.haversineFormula(DenmarkCoords,NorwayCoords));
        assertEquals("260",result);
    }

    @AfterEach void tearDown(){
        A = B = C = DenmarkCoords = NorwayCoords = LatLon1 = LatLon2 = null;
    }

}