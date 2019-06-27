package ie.tcd.scss.agents.discretization.clustering.algorithms.gng.gngq;

import ie.tcd.scss.learning.Action;

public class GNGQValue {


	/**
	 * The state with which this Q-value is associated.
	 */
	public GngQNode n;

	/**
	 * The action with which this Q-value is associated
	 */
	public Action a;

	/**
	 * The numeric Q-value
	 */
	public double q;

	
	/**
	 * Creates a Q-value for the given state an action pair with the specified q-value
	 * @param s the state
	 * @param a the action
	 * @param q the initial Q-value
	 */
	public GNGQValue(GngQNode n, Action a, double q){
		this.n = n;
		this.a = a;
		this.q = q;
	}

	
}
