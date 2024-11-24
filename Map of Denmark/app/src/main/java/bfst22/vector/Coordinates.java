package bfst22.vector;

public class Coordinates {
    private float minX, minY, maxX, maxY;

    public Coordinates(float minX, float minY, float maxX, float maxY){
        //Makes sure that min values are actually min and vice versa for max.
        if(minX < maxY){
            this.minX = minX;
            this.maxX = maxX;
            }
        else {
            this.minX = maxX;
            this.maxX = minX;
        }
        if(minY < maxY){
            this.minY = minY;
            this.maxY = maxY;
        }
        else {
            this.minY = maxY;
            this.maxY = minY;
        }
    }

    public float getMinX(){
        return this.minX;
    }

    public float getMinY(){
        return this.minY;
    }

    public float getMaxX(){
        return this.maxX;
    }

    public float getMaxY(){
        return this.maxY;
    }

    public float[] getMinXY(){
        return new float[]{minX,minY};
    }

    public float[] getMaxXY(){
        return new float[]{maxX,maxY};
    }

    public float[] getCenterPoint(){
        return new float[]{(this.minX+this.maxX)/2, (this.minY+this.maxY)/2};
    }
}