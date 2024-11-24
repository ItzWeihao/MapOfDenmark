
package bfst22.vector;

import javafx.scene.canvas.GraphicsContext;

// Defines what a node is in the OSM file and its properties.
public class PolyPoint extends Point implements Drawable {
    //Fields for roads and vehicle types
    public boolean foot = false;
    public boolean bicycle = false;
    public boolean motorVehicle = true;
    public int speedLimit = 0; //Speed limit in Denmark within towns
    public boolean isOneway = false;
    public String address;

    public PolyPoint(final long id, final float lat, final float lon) {
        super(id,lat,lon);
    }

    @Override public void trace(GraphicsContext gc) {
        gc.moveTo(this.lat, this.lon);
    }

    @Override public Drawable clone(){
        return new PolyPoint(this.id,this.lat,this.lon);
    }
}
