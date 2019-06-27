package ie.tcd.scss.ui.listeners;

import ie.tcd.scss.agents.Agent;

public interface TrainableObserver {
	
	public void trainingResult(long episode, long steps, long bestScoreEver, int representation);
	
	public void updatePolicy(Agent agent);
	
	public void save(String title);
	
}
