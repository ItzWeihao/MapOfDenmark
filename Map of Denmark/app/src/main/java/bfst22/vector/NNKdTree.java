package bfst22.vector;

public class NNKdTree extends KdTree {
	public void add(final PolyPoint element, Object owner) throws RuntimeException {
		if(super.lines == null) throw new RuntimeException("Unable to add element: KD-Tree already generated!");
		this.lines.add(new Node(element,owner));
	}

	private Node NNSearch(float[] point, intNode node, Node closest, int depth, VehicleType type){
		if(node.objects != null) {
			for (Node obj : node.elements)
				if ((obj.distance(point) < closest.distance(point)) && obj.type == type)
					closest = obj;
		} else {
			if(point[(depth+1)%2] < node.point[(depth+1)%2]){
				closest = NNSearch(point, node.left, closest, depth+1, type);
				if(node.axisDistance(point,(depth+1)%2) < closest.distance(point))
					closest = NNSearch(point, node.right, closest, depth+1, type);

			} else {
				closest = NNSearch(point, node.right, closest, depth+1, type);
				if(node.axisDistance(point,(depth+1)%2) < closest.distance(point))
					closest = NNSearch(point, node.left, closest, depth+1, type);
			}
		}
		return closest;
	}

	public float[] findNN(float[] point, VehicleType type){
		return this.NNSearch(point,this.root,new Node(new PolyPoint(-1,999,999),null),0,type).coords();
	}
}
