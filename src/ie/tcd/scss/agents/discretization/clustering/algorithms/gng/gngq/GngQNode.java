package ie.tcd.scss.agents.discretization.clustering.algorithms.gng.gngq;

import java.util.ArrayList;
import java.util.Vector;

import ie.tcd.scss.learning.Action;
import ie.tcd.scss.learning.State;

	
public class GngQNode {
	
	/**
	 *  The nodes linked to this node by a segment
	 */
	private ArrayList<GngQNode> neighbors = new ArrayList<GngQNode>();
	
	/**
	 *  The position of the node in the n-dimensional space 
	 */
	private Vector<Double> position = new Vector<Double>();
	
	/**
	 * The error associated to the node 
	 */
	private double error = 0.0d;
	
	/**
	 * The utility associated to the node (GNG-U)
	 */
	private double utility = 0.0d;
	
	
	
	private ArrayList<State> matchingStates = new ArrayList<State>();

	private final int index;

	private Action action;
	
	public Action getAction() {
		return this.action;
	}
	
	public ArrayList<State> getMatchingStates() {
		return this.matchingStates;
	}
	
	public void addMatchingState(State s) {
		
		if(!this.matchingStates.contains(s))
			this.matchingStates.add(s);
	}
	
	public GngQNode(int index) {
		this.index = index;
	}
	

	/**
	 * Gets the neighbors of the node
	 * 
	 * @return the list of Nodes linked to current Node
	 */
	public ArrayList<GngQNode> getNeighbors(){
		return this.neighbors;
	}
	
	/**
	 * Gets the number of neighbors of the node
	 * 
	 *  @return the number of neighbors the node is linked to (according to the graph topology)
	 */
	public int getNeighborsNumber(){
		return this.neighbors.size();
	}

	public void resetNeighbors(){
		this.neighbors.clear();
	}
	
	public void addNeighbor(GngQNode n){
		this.neighbors.add(n);
	}

	/**
	 * Gets the node position
	 * 
	 * @return the position of the node along the n-dimensional space
	 */
	public Vector<Double> getPosition() {
		if(this.position==null)
			this.position=new Vector<Double>();
		
		return this.position;
	}

	/**
	 * Updates the position of the node in the n-dimensional space
	 * 
	 * @param position
	 */
	public void setPosition(Vector<Double> position) {
		this.position = position;
	}
	
	/**
	 * Adds a dimension to the position vector
	 */
	public void addDimension(){
		this.addDimension(0.0d);
	}
	
	private void addDimension(Double value) {
		this.position.add(value);
	}



	/**
	 * 
	 * @return the size of the position vector
	 */
	public int getDimensions() {
		return this.position.size();
	}



	/** 
	 * Update position dimensions from given size
	 * 
	 */
	public void setDimensions(int size) {
		while(this.position.size()<size){
			this.position.add(new Double(0.0d));
		}
	}

	/**
	 * Gets the error of the Node
	 * @return the node error
	 */
	public double getError() {
		return this.error;
	}

	/**
	 * Set the error of the node
	 * 
	 * @param error factor
	 */
	public void setError(double error) {
		this.error = error;
	}
	
	/**
	 * Updates the error of the node by multiplying it by the given factor
	 * 
	 * @param multiplyFactor
	 */
	public void updateError(double multiplyFactor){
		this.error*=multiplyFactor;
	}
	
	/**
	 * Gets the utility of the Node
	 * @return the node error
	 */
	public double getUtility() {
		return this.utility;
	}

	/**
	 * Set the utility of the node
	 * 
	 * @param error factor
	 */
	public void setUtility(double utility) {
		this.utility = utility;
	}
	
	/**
	 * Updates the utility of the node by multiplying it by the given factor
	 * 
	 * @param multiplyFactor
	 */
	public void updateUtility(double multiplyFactor){
		this.utility*=multiplyFactor;
	}

	public int getIndex() {
		return this.index;
	}

	public void setAction(Action policy) {
		this.action = policy;
	}


	
}
