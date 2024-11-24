package bfst22.vector;

import javafx.scene.canvas.GraphicsContext;

public abstract class MoveableObj extends PolyPoint {
	private final double radius;
	private boolean moveable;
	protected boolean inRadius, isDragging;

	public MoveableObj(final long id, final float lat, final float lon, final double radius, final boolean moveable){
		super(id,lat,lon);
		this.radius = radius;
		this.moveable = moveable;
		this.inRadius = false;
		this.isDragging = false;
	}

	public boolean inRadius(final float[] mousePos, final double zoom){
		if(!this.isDragging) this.inRadius = Math.sqrt(Math.pow(mousePos[0] - this.lat,2) + Math.pow(mousePos[1] - this.lon,2)) < (this.radius/zoom);
		return this.inRadius;
	}

	public boolean isMovable(){
		return this.moveable;
	}

	public void setMovableState(boolean state){
		this.moveable = state;
	}

	public void move(final float[] newPos, final boolean state){
		this.isDragging = state;
		if(this.inRadius && this.moveable && state){
			super.lat = newPos[0];
			super.lon = newPos[1];
		}
	}

	public abstract void draw(final GraphicsContext gc, final double zoom, final float[] mousePos);
}