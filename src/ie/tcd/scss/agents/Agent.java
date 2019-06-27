package ie.tcd.scss.agents;

import java.util.ArrayList;

import ie.tcd.scss.agents.perception.Perception;
import ie.tcd.scss.environment.Environment;
import ie.tcd.scss.learning.Action;
import ie.tcd.scss.ui.listeners.TrainableObserver;
import ie.tcd.scss.xml.XMLable;

public abstract class Agent implements XMLable {
	
	protected ArrayList<Action> actions;
	
	
	protected ArrayList<TrainableObserver> trainObservers = new ArrayList<TrainableObserver>();
	
	private double confidenceForLastAction;
	
	
	public double getConfidenceForLastAction() {
		return confidenceForLastAction;
	}

	public void setConfidenceForLastAction(double confidenceForLastAction) {
		this.confidenceForLastAction = confidenceForLastAction;
	}

	protected long bestScoreEver=10000;
	
	protected long bestEpisodeEver=0;
	
	protected long episodeNumber = 0;
	
	public long getBestScoreExploitation() {
		return bestScoreEver;
	}
	
	public long getBestEpisode() {
		return bestEpisodeEver;
	}

	protected Environment trainingEnvironment; 



	protected long scoreExploitation;
	
	
	public long getScoreExploitation() {
		return scoreExploitation;
	}
	
	public Agent(Environment trainingEnvironment) {
		this.actions=trainingEnvironment.getActions();
		this.trainingEnvironment=trainingEnvironment;
	}
	
	public Agent(ArrayList<Action> actions) {
		this.actions=actions;
	}

	
	public long train() {
		
		if(this.trainingEnvironment!=null) {
			
			
			long steps = 0;
			
			this.trainingEnvironment.reset();
			
			
			do{
				steps++;
			
				this.trainingEnvironment.applyAction(this.getPolicy(this.trainingEnvironment.getCurrentPerception()));
								
			}  while(!trainingEnvironment.hasReachedGoal() && steps<10000);
			

			if(steps<bestScoreEver) {
				bestScoreEver=steps;
				bestEpisodeEver=episodeNumber;
			}
			
			
			this.scoreExploitation = steps;
			
			this.fireTrainObservers(episodeNumber, steps, bestScoreEver, 0);
			
			
			episodeNumber++;
			
			return steps;
		}
		
		return -1;
	
	}

	public abstract Action getPolicy(Perception p);
	
	public abstract Action decide(Perception p);
	
	
	protected void fireTrainObservers(long episode, long steps, long bestScoreEver, int representation) {
		for(TrainableObserver listener : this.trainObservers) {
			listener.trainingResult(episode, steps, bestScoreEver, representation);
			listener.updatePolicy(this);
		}
	}
	

	public void addTrainObserver(TrainableObserver observer) {
		this.trainObservers.add(observer);
	}
	

	public abstract void reset();

}
