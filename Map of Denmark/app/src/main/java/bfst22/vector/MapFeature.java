package bfst22.vector;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Wraps around the YAML file strcture and contains all its data inside its map for future use
public class MapFeature implements Serializable, SerialVersionIdentifiable {
	public featureDraw draw = new featureDraw();
	public Map<String, keyFeature> keyfeatures = new HashMap<>();

	public void addDrawable(final String keyFeature, final String valueFeature, Drawable value){
		if(this.keyfeatures.containsKey(keyFeature) && this.keyfeatures.get(keyFeature).valuefeatures.containsKey(valueFeature))
			this.keyfeatures.get(keyFeature).valuefeatures.get(valueFeature).drawable.add(value);
	}
}

// Contains all for one of the many generalised key features inside the map i.e water, natural, buildings etc.
// Draw is used for styling for features that are not styled on their own, which overrides the default styling
class keyFeature implements Serializable, SerialVersionIdentifiable {
	public featureDraw draw = new featureDraw();
	public Map<String, valueFeature> valuefeatures = new HashMap<>();
}

// Contains all for one of many specialised value features inside the map i.e. lake, vineyard, bicycle routes etc.
// Drawis used for styling for this specific feature, which overrides the default and key feature styling
class valueFeature implements Serializable, SerialVersionIdentifiable {
	public featureDraw draw = new featureDraw();
	public List<Drawable> drawable = new ArrayList<>();
}

// All the different options for a feature to be drawn
class featureDraw implements Serializable, SerialVersionIdentifiable {
	public boolean fill;
	public String fill_color;
	public String force_fill_color;
	public boolean stroke;
	public String stroke_color;
	public String force_stroke_color;
	public double line_width;
	public double dash_size;
	public int zoom_level;
	public boolean always_draw;
	public boolean hide;
	public boolean nodraw;
}