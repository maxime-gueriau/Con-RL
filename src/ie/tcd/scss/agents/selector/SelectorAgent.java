package ie.tcd.scss.agents.selector;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ie.tcd.scss.agents.Agent;
import ie.tcd.scss.agents.learning.LearnerAgent;
import ie.tcd.scss.environment.Environment;

public abstract class SelectorAgent extends LearnerAgent {

	protected ArrayList<Agent> representations = new ArrayList<Agent>();

	
	public SelectorAgent(Environment trainingEnvironment) {
		super(trainingEnvironment);
	}

	public void addRepresentation(Agent representation) {
		this.representations.add(representation);
	}

	@Override
	public Element toXml(Document doc) {
	
		for(Agent agent: this.representations) {
			
			if(agent instanceof LearnerAgent) {
				return agent.toXml(doc);
			}
			
		}
		return null;
		
	}

	@Override
	public void reset() {
		for(Agent agent: this.representations) {
			
			agent.reset();
			
		}
	}

	
	

}
