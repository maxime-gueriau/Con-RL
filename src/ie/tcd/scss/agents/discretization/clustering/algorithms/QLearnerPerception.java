package ie.tcd.scss.agents.discretization.clustering.algorithms;

import ie.tcd.scss.agents.perception.Perception;
import ie.tcd.scss.learning.State;

public class QLearnerPerception extends Perception {

	public final State state;
	
	public QLearnerPerception(State s) {
		this.state=s;
	}
	
}
