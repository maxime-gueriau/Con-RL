package ie.tcd.scss.agents.discretization.clustering.algorithms.gng;

import java.util.ArrayList;
import java.util.Vector;

import ie.tcd.scss.learning.Action;
import ie.tcd.scss.learning.State;

	
public class Node {
	/**
	 *  The nodes linked to this node by a segment
	 */
	private ArrayList<Node> neighbors = new ArrayList<Node>();
	
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
	
	private Action action;
	
	
	private double qValue = 0d;
	
	
	
	private ArrayList<State> matchingStates = new ArrayList<State>();
	
	public ArrayList<State> getMatchingStates() {
		return this.matchingStates;
	}
	
	public void addMatchingState(State s) {
		this.matchingStates.add(s);
	}
	
	public Node() {
		
	}
	
	public Node(Action a) {
		this.action=a;
	}
	
	/**
	 * Gets the neighbors of the node
	 * 
	 * @return the list of Nodes linked to current Node
	 */
	public ArrayList<Node> getNeighbors(){
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
	
	public void addNeighbor(Node n){
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

	public Action getAction() {
		return this.action;
	}

	public void setAction(Action a) {
		this.action=a;
	}

	public double getQValue() {
		return qValue;
	}

	public void setQValue(double qValue) {
		this.qValue = qValue;
	}
	
	
}
