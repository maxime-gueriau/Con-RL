package ie.tcd.scss.learning.qlearning;

import ie.tcd.scss.learning.Action;
import ie.tcd.scss.learning.State;

public class QValue {


	/**
	 * The state with which this Q-value is associated.
	 */
	public State s;

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
	public QValue(State s, Action a, double q){
		this.s = s;
		this.a = a;
		this.q = q;
	}



}
