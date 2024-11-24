package bfst22.vector;

public class Distance {
    final int R = 6371;

    //Computes the straight-line distance between two pairs of lat/lon using Haversine formula.
    public float haversineFormula(PolyPoint start, PolyPoint target){
        final int R = 6371; //Earth's radius in kilometers.
        float dLat = toRadians(start.lat - target.lat);
        float dLon = toRadians(start.lon - target.lon);

        float a = (float)Math.sin(dLat / 2) * (float)Math.sin(dLat / 2) +
                (float)Math.cos(toRadians(start.lat)) * (float)Math.cos(toRadians(target.lat))
                        * (float)Math.sin(dLon / 2) * (float)Math.sin(dLon / 2);

        float c = 2 * (float)Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return R * c; //Returns d - which is the distance in kilometers.
    }

    public float toRadians(float value){
        return value * (float)Math.PI / 180;
    }

}
