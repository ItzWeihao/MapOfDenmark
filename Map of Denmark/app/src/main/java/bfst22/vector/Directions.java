package bfst22.vector;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class Directions {
    private float distance;
    Distance d = new Distance();

    public float getAngleDifference(PolyPoint previous1, PolyPoint previous2,PolyPoint current1, PolyPoint current2){
        float angle1 = getAngle(previous1, previous2);
        float angle2 = getAngle(current1, current2);
        return angle2 - angle1;
    }

    /* Returns a String with the description of the next turn */
    public HBox turn(int row, float angle, float angledifference, PolyPoint current1, PolyPoint current2, MapCanvas canvas, boolean end){
        HBox TurnEntry = (HBox) Controller.smartFXMLLoader(this,"RouteListEntry.fxml");

        Label RouteNumberLabel      = (Label) TurnEntry.lookup("#RouteNumber");
        Text RouteSymbolLabel       = (Text) TurnEntry.lookup("#RouteSymbol");
        Label RouteDescriptionLabel = (Label) TurnEntry.lookup("#RouteDescription");

        String description, symbol;

        if(current2 == null && !end){
            symbol = String.valueOf('\uF041');
            description = "You begin your journey here!";
        } else if(current2 == null && end) {
            symbol = String.valueOf('\uF041');
            description = "You arrive at your destination!";
        } else if(angledifference >= 90 && angle > 180){ //Right turn
            symbol = String.valueOf('\uF30B');
            description = "Turn right onto " + current2.address + " and continue for " + stringDistance(current1,current2) + " meters.";
        } else if(angledifference >= 90 && angle < 180){
            symbol = String.valueOf('\uF30A');
            description = "Turn left onto " + current2.address + " and continue for " + stringDistance(current1,current2) + " meters.";
        } else if(angledifference <= -90 && angle < 180) { //Left turn
            symbol = String.valueOf('\uF30A');
            description = "Turn left onto " + current2.address + " and continue for " + stringDistance(current1,current2) + " meters.";
        } else if (angledifference <= -90 && angle > 180){
            symbol = String.valueOf('\uF30B');
            description = "Turn right onto " + current2.address + " and continue for " + stringDistance(current1,current2) + " meters.";
        } else {
            symbol = String.valueOf('\uF30C');
            description = "Continue along " + current2.address + " for " + stringDistance(current1,current2) + " meters.";
        }

        TurnEntry.setOnMousePressed(e -> canvas.goToPosAbsolute(new float[]{current1.lat,current1.lon}));
        RouteNumberLabel.setText(String.format("%03d",row));
        RouteSymbolLabel.setFont(new Font("Font Awesome 5 Free Solid",18));
        RouteSymbolLabel.setText(symbol);
        RouteDescriptionLabel.setText(description);

        return TurnEntry;
    }

    /* Returns the angle between two nodes (Start of the road to the end of the road) */
    public float getAngle(PolyPoint from, PolyPoint to){
        /*
        public float getAngle(PolyPoint from, PolyPoint fixed, PolyPoint to){
        float angle1 = (float)Math.atan2(from.lat - fixed.lat, from.lon - fixed.lon);
        float angle2 = (float)Math.atan2(to.lat - fixed.lat, to.lon - fixed.lon);
        float result = (float)Math.toDegrees(angle1-angle2);

        if(result < 0) result += 360;
         */

        if(to.lat > from.lat){
            return (float)(Math.toDegrees(Math.atan2(to.lat - from.lat,from.lon - to.lon)));
        }
        if(to.lat < from.lat){
            return (float)(360 - Math.toDegrees(Math.atan2(from.lat - to.lat, from.lon - to.lon)));
        }
        else return (float)Math.toDegrees(Math.atan2(0,0));
    }

    public String stringDistance(PolyPoint start, PolyPoint target){
        this.distance = d.haversineFormula(start,target) * 1000; //Gives distance and converts it to meter
        return String.format("%.0f",distance); //Converting into 0 floating point
    }


}
