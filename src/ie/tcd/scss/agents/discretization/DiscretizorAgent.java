package ie.tcd.scss.agents.discretization;

import java.util.ArrayList;

import ie.tcd.scss.agents.learning.LearnerAgent;
import ie.tcd.scss.agents.perception.Perception;
import ie.tcd.scss.learning.Action;
import ie.tcd.scss.learning.State;

public abstract class DiscretizorAgent extends LearnerAgent {

	protected Discretization<?> discretization;

	public DiscretizorAgent(Discretization<?> discretization, ArrayList<Action> actions) {
		super(actions);
		this.discretization=discretization;
	}
	
	
	public final Action decide(Perception p) {
		return this.decide(this.discretization.mapPerception(p));
	}

	public void learn(Perception previousPerception, Action a, double reward, Perception newPerception) {
		this.learn(this.discretization.mapPerception(previousPerception), a, reward, this.discretization.mapPerception(newPerception));
	}
	
	protected abstract void learn(State s, Action a, double reward, State snew);
	
	public Action getPolicy(Perception p) {
		return this.getPolicy(this.discretization.mapPerception(p));
	}
	
	
	protected abstract Action decide(State map);
	
	
	protected abstract Action getPolicy(State s);
	
	
	public State getState(Perception perception) {
		return this.discretization.mapPerception(perception);
	}
	
	public ArrayList<State> getAllStates(){
		
		return this.discretization.states;
	}
	
}
