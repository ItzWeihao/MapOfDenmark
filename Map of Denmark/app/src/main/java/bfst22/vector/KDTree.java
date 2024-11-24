package bfst22.vector;

import java.io.Serializable;
import java.util.*;

public class KdTree implements Serializable, SerialVersionIdentifiable {
	private final List<float[]> splits;
	protected final intNode root;
	protected List<Node> lines;

	public KdTree(){
		this.root = new intNode();
		this.lines = new ArrayList<>();
		this.splits = new ArrayList<>();
	}

	public void add(final PolyPoint element, Object owner) throws RuntimeException {
		if(this.lines == null) throw new RuntimeException("Unable to add element: KD-Tree already generated!");
		this.lines.add(new Node(element,owner));
	}

	public void add(final PolyLine element, Object owner) throws RuntimeException {
		if(this.lines == null) throw new RuntimeException("Unable to add element: KD-Tree already generated!");
		for(int i = 0; i < element.coords.length; i+=2)
			this.lines.add(new Node(element.coords[i],element.coords[i+1],owner));
	}

	public void add(PolyRelation element, Object owner) throws RuntimeException {
		if(this.lines == null) throw new RuntimeException("Unable to add element: KD-Tree already generated!");
		element.parts.forEach(poly -> {
			switch(poly.getClass().getName()){
				case "PolyPoint" -> this.add((PolyPoint) poly, owner);
				case "PolyLine" -> this.add((PolyLine) poly, owner);
				case "PolyRelation" -> this.add((PolyRelation) poly, owner);
			}
		});
	}

	// KD-Tree generic 'Breadth-first search' method
	private void bfs(bfsCall lambda){
		Queue<intNode> nodes = new LinkedList<>();
		nodes.add(this.root);
		int depth = 1;

		while(!nodes.isEmpty()){
			int queueSize = nodes.size();
			depth = depth==0?1:0;

			for(int i = 0; i < queueSize; i++)
				lambda.call(nodes,nodes.remove(),depth);
		}
	}

	public void generateTree() {
		this.root.elements = this.lines;
		this.lines = null;
		this.bfs((q,n,d) -> {
			if (n.elements.size() > 1000) {
				n.elements.sort((o1, o2) -> Float.compare(o1.get(d), o2.get(d)));

				n.point = n.elements.get(n.elements.size()/2).coords();
				n.min = n.elements.get(0).get(d);
				n.max = n.elements.get(n.elements.size()-1).get(d);

				q.add(n.left = new intNode());
				q.add(n.right = new intNode());

				for (Node node : n.elements) {
					if (node.get(d==1?0:1) > n.point[d==1?0:1]) n.right.elements.add(node);
					else n.left.elements.add(node);
				}
				n.objects = null;
				n.elements = null;
			} else for(Node e : n.elements) n.objects.add(e.obj);
		});
	}

	public Set<Object> rangeSearch(double[] min, double[] max) {
		Set<Object> allElements = new HashSet<>();
		this.bfs((q,n,d) -> {
			if (n.objects != null) allElements.addAll(n.objects);
			else {
				if (n.min < max[d==1?0:1]) q.add(n.left);
				if (n.max > min[d==1?0:1]) q.add(n.right);
			}
		});
		return allElements;
	}

	public void generateSplits() {
		this.bfs((q,n,d) -> {
			if (n.left != null && n.right != null) {
				this.splits.add(d==1 ? new float[]{n.point[0], n.min} : new float[]{n.min, n.point[1]});
				this.splits.add(d==1 ? new float[]{n.point[0], n.max} : new float[]{n.max, n.point[1]});
				q.add(n.left);
				q.add(n.right);
			}
		});
	}

	public List<float[]> getSplits(){
		return this.splits;
	}

	private interface bfsCall {
		void call(Collection<intNode> queue, intNode node, int depth);
	}

	protected static class intNode implements Serializable, SerialVersionIdentifiable {
		public float[] point;
		public float min, max;
		public intNode left, right;
		public List<Node> elements;
		public Set<Object> objects;

		public intNode(){
			this.elements = new ArrayList<>();
			this.objects = new HashSet<>();
		}

		public double distance(float[] point){
			return Math.sqrt(Math.pow(this.point[0]-point[0],2)+Math.pow(this.point[1]-point[1],2));
		}

		public double axisDistance(float[] point, int axis){
			return Math.abs(point[axis]-this.point[axis]);
		}
	}

	protected static class Node extends Point implements Serializable, SerialVersionIdentifiable {
		public Object obj;
		public VehicleType type;
		public int speedLimit;
		public boolean isOneway;
		public String address;

		public Node(float lat, float lon, Object objRef) {
			super(-1,lat,lon);
			this.obj = objRef;
		}

		public Node(PolyPoint element, Object objRef) {
			super(-1,element.lat,element.lon);
			this.obj = objRef;
			this.speedLimit = element.speedLimit;
			this.isOneway = element.isOneway;
			this.address = element.address;

			if(element.foot) this.type = VehicleType.FOOT;
			else if(element.bicycle) this.type = VehicleType.BICYCLE;
			else this.type = VehicleType.MOTORCAR;
		}

		public float get(int index){
			return (index == 0 ? super.lat : super.lon);
		}

		public float[] coords(){
			return new float[]{super.lat,super.lon};
		}

		public double distance(float[] point){
			return Math.sqrt(Math.pow(super.lat-point[0],2)+Math.pow(super.lon-point[1],2));
		}
	}
}