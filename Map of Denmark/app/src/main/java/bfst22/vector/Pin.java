package bfst22.vector;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class Pin extends MoveableObj {
	public HBox listEntry;
	public String title, description;

	public Pin(HBox listEntry, final float lat, final float lon, final double radius, final boolean moveable, final String title, final String description){
		super(-1,lat,lon,radius,moveable);
		this.title = title;
		this.description = description;
		this.listEntry = listEntry;
	}

	public void setContent(final String title, final String description){
		this.title = title;
		this.description = description;
	}

	@Override public void draw(final GraphicsContext gc, final double zoom, final float[] mousePos){
		gc.setFill(Color.BLACK);
		gc.setTextAlign(TextAlignment.CENTER);
		gc.fillText(this.title.length() > 20 ? this.title.substring(0,20) + "..." : this.title, super.lat, super.lon-40/zoom);
		if(this.inRadius(mousePos,zoom)) gc.setFill(Color.YELLOW);
		gc.setTextAlign(TextAlignment.LEFT);
		gc.setFont(new Font("Font Awesome 5 Free Solid",40/zoom));
		gc.fillText(String.valueOf('\uF041'),super.lat-15/zoom,super.lon);
		gc.setFont(new Font("Arial",11/zoom));
	}

	@Override public boolean inRadius(final float[] mousePos, final double zoom){
		boolean inside = super.inRadius(mousePos,zoom);
		if(inside) listEntry.setStyle("-fx-background-color:yellow;");
		else listEntry.setStyle("-fx-background-color:transparent;");
		return inside;
	}
}
