package bfst22.vector;

import javafx.scene.canvas.GraphicsContext;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PolyRelation implements Drawable, Serializable, SerialVersionIdentifiable {
	List<Drawable> parts; // List of what constitutes the relation.

	public PolyRelation(final List<Drawable> rel, final boolean isMultiPoly) {
		this.parts = new ArrayList<>(rel);

		if(isMultiPoly){
			Poly.stitch(this.parts);
			this.parts.forEach(poly -> Poly.winding((PolyGon) poly));
		}
	}

	// Traces the area that has to be drawn before drawing.
	@Override public void trace(final GraphicsContext gc) {
		this.parts.forEach(poly -> poly.trace(gc));
	}

	@Override public Drawable clone(){
		return new PolyRelation(this.parts.stream().map(Drawable::clone).toList(),false);
	}
}
