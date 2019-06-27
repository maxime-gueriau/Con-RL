package ie.tcd.scss.environment;

import java.util.ArrayList;

import ie.tcd.scss.agents.perception.Perception;
import ie.tcd.scss.learning.Action;

public abstract class Environment {

	
	protected ArrayList<Action> actions = new ArrayList<Action>();
	
	public final ArrayList<Action> getActions() {
		return actions;
	}

	public abstract Perception getCurrentPerception();

	public abstract double applyAction(Action a);

	public abstract boolean hasReachedGoal();

	public abstract void reset();

	public abstract void resetRandom();

	public abstract ArrayList<Double> getVariables();
	
}
