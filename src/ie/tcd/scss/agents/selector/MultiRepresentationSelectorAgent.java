package ie.tcd.scss.agents.selector;

import ie.tcd.scss.agents.Agent;
import ie.tcd.scss.agents.learning.LearnerAgent;
import ie.tcd.scss.agents.perception.Perception;
import ie.tcd.scss.environment.Environment;
import ie.tcd.scss.learning.Action;

public class MultiRepresentationSelectorAgent extends SelectorAgent {

	public MultiRepresentationSelectorAgent(Environment trainingEnvironment) {
		super(trainingEnvironment);
	}


	@Override
	public void learn(Perception previousPerception, Action a, double reward, Perception newPerception) {

		for(Agent agent: this.representations) {

			if(agent instanceof LearnerAgent) {
				((LearnerAgent)agent).learn(previousPerception, a, reward, newPerception);
			}

		}
	}


	@Override
	public Action getPolicy(Perception p) {


		Action action = null;

		/***
		 * Strategy: take the action with highest confidence
		 */

		double maxConfidence = -10000;

		//SO ITS BASICALLY: IF THERES GNG IN EACH, GET GNG, THEN GNG WITH BIGGER CONFIDENCE WINS
		for(Agent agent: this.representations) {

			if(agent instanceof SelectorAgent) {
				Action proposedAction = agent.getPolicy(p);
				double confidence = agent.getConfidenceForLastAction();
				if(confidence>maxConfidence) {
					maxConfidence=confidence;
					action = proposedAction;
				}

			}
		}


		if(action==null)
			System.err.println("MultiRepresentation policy returned null action !!!");


		return action;
	}

	@Override
	public Action decide(Perception p) {

		/**
		 * Simple version does random between two (but execute both anyway)
		 */

		Action action = null;

		/***
		 * Strategy test number 1: take the action with highest confidence
		 */

		double maxConfidence = -10000;

		for(Agent agent: this.representations) {

			if(agent instanceof SelectorAgent) {
				Action proposedAction = agent.decide(p);
				double confidence = agent.getConfidenceForLastAction();
				if(confidence>maxConfidence) {
					maxConfidence=confidence;
					action = proposedAction;
				}

			}
		}


		if(action==null)
			System.err.println("MultiRepresentation decision returned null action !!!");


		return action;
	}


	@Override
	public long train() {

		for(Agent agent: this.representations) {
			agent.train();
		}

		long steps = 0;

		return steps;
	}


}
