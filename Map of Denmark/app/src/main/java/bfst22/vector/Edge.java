package bfst22.vector;

public class Edge {
    private PolyPoint from;
    private PolyPoint to;
    private float weight;

    public Edge(PolyPoint from, PolyPoint to, float weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    public PolyPoint getFrom(){
        return this.from;
    }

    public PolyPoint getTo(){
        return this.to;
    }

    public float getWeight(){
        return this.weight;
    }


}
