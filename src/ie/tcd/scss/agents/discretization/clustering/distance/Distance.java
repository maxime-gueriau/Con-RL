package ie.tcd.scss.agents.discretization.clustering.distance;

import java.util.Vector;

import ie.tcd.scss.agents.discretization.clustering.algorithms.gng.Node;
import ie.tcd.scss.agents.discretization.clustering.input.Signal;


/**
 * Describes a generic distance between a generic Node and an generic given Signal
 * 
 * @author Maxime Guériau
 *
 */
public abstract class Distance {

	/**
	 * Computes the squared distance between a Signal position and a Node position
	 * 
	 * @param s : the given signal
	 * @param n : the given node
	 * @return the squared distance
	 */
	public abstract double computeSquareDistance(Signal s, Node n);
	
	/**
	 *  Computes the distance between a Signal position and a Node position
	 * 
	 * @param s : the given signal
	 * @param n : the given node
	 * @return the distance
	 */
	public double computeDistance(Signal s, Node n){
		
		//System.out.println("Computed distance from clustering = " + Math.sqrt(this.computeSquareDistance(s, n)));
		
		return Math.sqrt(this.computeSquareDistance(s, n));
	}

	/**
	 * Computes an interpolated positions at the middle of given node1 and node2 
	 * 
	 * @param node1
	 * @param node2
	 * @return a position in the same dimensions as node1 and node2
	 */
	public abstract Vector<Double> computeMiddlePosition(Node node1, Node node2);

	/**
	 * Translates the given position by the given offset vector
	 * @param position
	 * @param offset
	 * @return the new position
	 */
	public Vector<Double> translate(Vector<Double> position, Vector<Double> offset) {
		
		Vector<Double> newPosition = new Vector<Double>();
		
		if(position.size() == offset.size()){

			for(int i = 0; i< position.size(); ++i) {
				newPosition.add(new Double( position.get(i) - offset.get(i)));
			}
			
		}
		
		return newPosition;
	}

	public Vector<Double> normalize(Vector<Double> vector) {
		final double norm = norm(vector);
		Vector<Double> position = new Vector<Double>();
		for(int i = 0; i< vector.size(); ++i) {
			position.add(vector.get(i) / norm);
		}
		return position;
	}
	
	private final double norm(Vector<Double> vector) {
		double norm = 0.0;
		
		for(int i = 0; i< vector.size(); ++i) {
			norm += vector.get(i) * vector.get(i);
		}
		
		return Math.sqrt(norm);
	}
	
	
}