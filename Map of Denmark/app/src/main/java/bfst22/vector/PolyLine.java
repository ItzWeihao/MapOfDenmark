package bfst22.vector;

import java.io.Serializable;
import java.util.List;
import javafx.scene.canvas.GraphicsContext;

// Defines the lines intended to draw the polygons on the map; typically used for ways.
public class PolyLine implements Drawable, Serializable, SerialVersionIdentifiable {
    public long[] ids;
    public float[] coords;

    // Constructs the line based on the given nodes for the particular polygon.
    public PolyLine(final List<PolyPoint> nodes) {
        this.coords = new float[nodes.size() * 2];
        this.ids = new long[nodes.size()];
        int i = 0, j = 0;
        for (PolyPoint node : nodes) {
            this.coords[i++] = node.lat;
            this.coords[i++] = node.lon;
            this.ids[j++] = node.id;
        }
    }

    public PolyLine(final long[] ids, final float[] coords){
        this.ids = ids;
        this.coords = coords;
    }

    // traces the are needed to be drawn before drawing.
    @Override public void trace(GraphicsContext gc) {
        gc.moveTo(this.coords[0], this.coords[1]);
        for (int i = 0; i < this.coords.length; i += 2)
            gc.lineTo(coords[i], coords[i+1]);
    }

    @Override public Drawable clone(){
        long[] ids = new long[this.ids.length];
        float[] coords = new float[this.coords.length];
        System.arraycopy(this.ids,0,ids,0,this.ids.length);
        System.arraycopy(this.coords,0,coords,0,this.coords.length);
        return new PolyLine(ids,coords);
    }
}