package ie.tcd.scss.agents.selector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ie.tcd.scss.agents.Agent;
import ie.tcd.scss.agents.aggregation.GNGAggregator;
import ie.tcd.scss.agents.learning.qlearning.QLearner;
import ie.tcd.scss.agents.perception.Perception;
import ie.tcd.scss.environment.Environment;
import ie.tcd.scss.learning.Action;

public class GridGngSelectorAgent extends SelectorAgent {


	private int numberOfTimesQlearner;
	private int numberOfTimesGNG;
	private int lastRepresentationUsed;

	private String filename;

	protected BufferedWriter writer;
	private String representationSequence="";

	protected static String SEPARATOR = ",";



	public GridGngSelectorAgent(Environment trainingEnvironment) {
		super(trainingEnvironment);


		try { 
			this.filename = "selector.csv";
			File file = new File(this.filename);
			this.writer = new BufferedWriter(new FileWriter(file));

		} catch (IOException e) {
			e.printStackTrace();
		}



		//first write the header:
		String line = "";

		line += "episode";
		line += SEPARATOR;

		line += "grid0gng1";
		line += SEPARATOR;

		//end the line
		line+="\n";

		try {
			this.writer.append(line);
			this.writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	@Override
	public Element toXml(Document doc) {

		System.out.println("Number of time GNG is " + numberOfTimesGNG + " vs Qlearner " + numberOfTimesQlearner);

		return super.toXml(doc);
	}


	@Override
	public void learn(Perception previousPerception, Action a, double reward, Perception newPerception) {

		for(Agent agent: this.representations) {

			if(agent instanceof QLearner) {
				((QLearner)agent).learn(previousPerception, a, reward, newPerception);
			}

		}


	}


	@Override
	//take q-learner unless theres gng
	public Action getPolicy(Perception p) {

		Action action = null;

		for(Agent agent: this.representations) {
			if(agent instanceof QLearner) {
				action = agent.getPolicy(p);

			}
		}

		for(Agent agent: this.representations) {
			if(agent instanceof GNGAggregator) {
				Action a = agent.getPolicy(p);
				if(a != null) {
					action = a;
				}
			}
		}



		if(action==null)
			System.err.println("GridGngSelector policy returned null action !!!");


		return action;
	}



	/***
	 * Second version of selection process,
	 * this time making use of updated information related to matching between gng states and grid states
	 */
	@Override

	public Action decide(Perception p) {

		Action action = null;

		for(Agent agent: this.representations) {

			if(agent instanceof QLearner) {
				action = agent.decide(p);
				this.setConfidenceForLastAction(agent.getConfidenceForLastAction());
				this.lastRepresentationUsed=2;
			}
		}

		for(Agent agent: this.representations) {
			if(agent instanceof GNGAggregator) {
				Action a = agent.decide(p);
				if(a != null) {
					action = a;
					this.setConfidenceForLastAction(agent.getConfidenceForLastAction());
					this.lastRepresentationUsed=3;
				}
			}
		}

		if(action==null)
			System.err.println("GridGngSelector decision returned null action !!!");

		this.representationSequence += "" + lastRepresentationUsed;

		return action;
	}


	public void endOfEpisode(long episode){
		//start with an empty line
		String line = "";

		// fill it with data
		line += episode;
		line += SEPARATOR;

		line += this.representationSequence;
		line += SEPARATOR;

		//end the line
		line+="\n";
		try {
			this.writer.append(line);
			this.writer.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.representationSequence="";
	}

	@Override
	public long train() {

		for(Agent agent: this.representations) {
			agent.train();
		}

		long steps = super.train();

		return steps;
	}


	public int getLastUsedRepresentation() {
		return this.lastRepresentationUsed;
	}

}
