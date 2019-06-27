package ie.tcd.scss.agents.learning;

import java.util.ArrayList;

import ie.tcd.scss.agents.Agent;
import ie.tcd.scss.agents.perception.Perception;
import ie.tcd.scss.environment.Environment;
import ie.tcd.scss.learning.Action;


public abstract class LearnerAgent extends Agent {


	public LearnerAgent(ArrayList<Action> actions) {
		super(actions);
	}
	
	public LearnerAgent(Environment trainingEnvironment) {
		super(trainingEnvironment);
	}

	public abstract void learn(Perception previousPerception, Action a, double reward, Perception newPerception);
	


	
	
}
