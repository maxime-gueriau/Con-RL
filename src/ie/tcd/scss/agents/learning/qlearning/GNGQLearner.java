package ie.tcd.scss.agents.learning.qlearning;

import java.util.ArrayList;
import java.util.Random;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ie.tcd.scss.agents.discretization.DiscretizorAgent;
import ie.tcd.scss.agents.discretization.clustering.algorithms.gng.Node;
import ie.tcd.scss.agents.discretization.clustering.algorithms.gng.gngq.GngQ;
import ie.tcd.scss.agents.discretization.clustering.algorithms.gng.gngq.GngQNode;
import ie.tcd.scss.agents.discretization.clustering.input.Signal;
import ie.tcd.scss.agents.perception.Perception;
import ie.tcd.scss.environment.implementations.MountainCar;
import ie.tcd.scss.environment.implementations.MountainCarStateSpaceGridDiscretization;
import ie.tcd.scss.learning.Action;
import ie.tcd.scss.learning.State;
import ie.tcd.scss.ui.listeners.AggregatorListener;
import ie.tcd.scss.ui.listeners.QLearnerListener;
import ie.tcd.scss.ui.listeners.TrainableObserver;

public class GNGQLearner extends DiscretizorAgent {

	private ArrayList<AggregatorListener> agglisteners = new ArrayList<AggregatorListener>();

	
	private long totalNumberOfInteractions = 0; // used in the decreasing epsilon-greedy exploration policy

	private double epsilon = 0.1; // epsilon for epsilon greedy exploration policy (i.e. prob of exploration)

	private double E = 0.015; // exploration parameter (equals after how many episodes the agents definitely switches to full exploitation
	
	private boolean exploitation = false;
	
	protected ArrayList<QLearnerListener> listeners = new ArrayList<QLearnerListener>();
	
	private GngQ gngq;
	
	private long gridCorrespondingEpisode = 0;


	private long bestEpisodeCorrespondingGrid;
	
	private MountainCarStateSpaceGridDiscretization discretization;

	
	public void addListener(QLearnerListener listener) {
		this.listeners.add(listener);
	}
	
	public GNGQLearner(MountainCarStateSpaceGridDiscretization discretization, ArrayList<Action> actions) {
		super(discretization, actions);

		
		this.gngq=new GngQ(actions);
		
		this.discretization=discretization;
		
		this.gngq.updateMatching(this.discretization.getStates(), ((MountainCarStateSpaceGridDiscretization)this.discretization).getXIndices(), ((MountainCarStateSpaceGridDiscretization)this.discretization).getYIndices());
		
	}
	
	public GNGQLearner(MountainCarStateSpaceGridDiscretization discretization, MountainCar MCenv) {
		this(discretization, MCenv.getActions());
		this.trainingEnvironment=MCenv;
	
	}
	
	


	@Override
	protected Action decide(State s) {
	

		final Action action;
		
		double rand = Math.random();

		
		if(rand<epsilon) {
action = this.getRandomAction();
			
			this.setConfidenceForLastAction(-1);
			
		} else {
			action = this.getPolicy(s);
		}
		
		s.updateLastAction(action);
		
		
		return action;

	}


	@Override
	protected Action getPolicy(State s){ //for update formula
			
		return this.gngq.getPolicy(s);
		
		
	}

	private Action getRandomAction(){ //pick random action
		Random rand = new Random();
		return this.actions.get(rand.nextInt(this.actions.size()));

	}


	protected void learn(State s, Action a, double reward, State snew) {
		
		if(!this.exploitation) {
		
			Signal s1 = new Signal();
			s1.addData(new Double(this.discretization.getStateXIndex(s)));
			s1.addData(new Double(this.discretization.getStateYIndex(s)));
			
			Signal s2 = new Signal();
			s2.addData(new Double(this.discretization.getStateXIndex(snew)));
			s2.addData(new Double(this.discretization.getStateYIndex(snew)));
			
			this.gngq.learn(s1,s, a, reward, s2, snew);
			
		}
	    
	//	this.fireListeners();
	}


	@Override
	public void reset() {
	
		this.gngq.adapationAtTheEndOfEpisode();
		
		this.fireListeners();
	
		//interaction is timestep, each time q value is updated. not reset per episode
		this.totalNumberOfInteractions++;
	
			//update of decrasing epsilon-greedy policy
			this.epsilon = Math.exp(-this.E*this.totalNumberOfInteractions);
			
	}

	@Override
	public Element toXml(Document doc) {
			
		Element element = doc.createElement("QLearner");
		
		Element actionsElement = doc.createElement("Actions");
		
		for(Action a : this.actions) {
			Element actionElement = doc.createElement("Action");
			actionElement.setAttribute("id", ""+a.getIndex());
			actionElement.setAttribute("name", ""+a.getName());
			actionsElement.appendChild(actionElement);
		}
		
		element.appendChild(actionsElement);
		
		element.appendChild(this.discretization.toXml(doc));
		
		Element qValueselement = doc.createElement("QValues");
		
		
		element.appendChild(qValueselement);
		

		return element;
	
	}

	
	public void addAggregatorListener(AggregatorListener listener) {
		this.agglisteners.add(listener);
	}
	
	

	
	@Override 
	public long train() {
	
		long steps=0;
		this.trainingEnvironment.reset();

		do{
			steps++;
			
			Perception p  = this.trainingEnvironment.getCurrentPerception();
			
			Action a = this.decide(p);
	
			double reward = this.trainingEnvironment.applyAction(a);
			
			Perception newp  = this.trainingEnvironment.getCurrentPerception();
			
			this.learn(p, a, reward, newp);
			
		}  while(!trainingEnvironment.hasReachedGoal() && steps<10000);
		
		this.gridCorrespondingEpisode++;
		
		if(steps<bestScoreEver) {
			bestScoreEver=steps;
			bestEpisodeEver=episodeNumber;
			this.bestEpisodeCorrespondingGrid = this.gridCorrespondingEpisode;
		}

		for(TrainableObserver listener : this.trainObservers) {
		
			listener.trainingResult(this.gridCorrespondingEpisode,steps, bestScoreEver,-1);
		}

		
		return steps;
		
	}
	
	
	protected void fireTrainObservers(long episode, long steps, long bestScoreEver, int representation) {
		for(TrainableObserver listener : this.trainObservers) {
			listener.updatePolicy(this);
		}
	}
	
	
	@Override
	public Action getPolicy(Perception p) {
		
		State state = this.discretization.mapPerception(p);

		return this.gngq.getPolicy(state);

	}
	

	
	private void fireListeners() {
	
		final ArrayList<Node> nodes = new ArrayList<Node>();
		for(GngQNode n : this.gngq.getNodes()) {
			Node node = new Node();
			node.setPosition(n.getPosition());
			node.setAction(n.getAction());
			nodes.add(node);
		}
		
		for(AggregatorListener listener : this.agglisteners) {
			listener.update(nodes);
		}
		
	}

	public long getBestEpisodeCorrespondingGrid() {
		return bestEpisodeCorrespondingGrid;
	}


	public void updateTrain() {
	}
	
}
