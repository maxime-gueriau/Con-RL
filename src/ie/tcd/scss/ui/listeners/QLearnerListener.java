package ie.tcd.scss.ui.listeners;

import java.util.ArrayList;
import java.util.HashMap;

import ie.tcd.scss.learning.Action;
import ie.tcd.scss.learning.State;

public interface QLearnerListener {

	public void update(State s, long stateIndexX, long stateIndexY, Action a, long episode);

	public void updateMatching(ArrayList<State> states, HashMap<State, Long> xIndices, HashMap<State, Long> yIndices);

	public void updateDiscount(double epsilon);

	public void learn(State s, Action a, double reward, State snew);

	public void updateTrain();
	
	
	
}
