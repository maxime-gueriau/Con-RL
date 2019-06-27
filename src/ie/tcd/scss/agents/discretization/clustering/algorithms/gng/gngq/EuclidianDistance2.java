package ie.tcd.scss.agents.discretization.clustering.algorithms.gng.gngq;

import java.util.Vector;

import ie.tcd.scss.agents.discretization.clustering.input.Signal;



public class EuclidianDistance2 extends Distance2 {

	public double computeSquareDistance(Signal s, GngQNode n) {
		
		
		double distance = 0.0;
		
		int position = 0;
		
		// check if given node as enough dimensions
		while(n.getPosition().size() < s.getData().size()){
			n.addDimension();
		}
		
		// compute distance
		for(double dimension : s.getData()){
			distance += Math.pow((n.getPosition().get(position) - dimension), 2);
			position++;
		}
		
		return distance;
	}

	@Override
	public Vector<Double> computeMiddlePosition(GngQNode node1, GngQNode node2) {

		Vector<Double> position = new Vector<Double>();
		
		if(node1.getDimensions() == node2.getDimensions()){
		
			// interpolate positions
			for(int i = 0; i< node1.getDimensions(); ++i) {
				position.add(new Double( (node1.getPosition().get(i) + node2.getPosition().get(i) ) / 2.0d));
			}
		
		}
		
		return position;
	}
	
}
