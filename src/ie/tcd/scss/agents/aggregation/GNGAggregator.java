package ie.tcd.scss.agents.aggregation;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ie.tcd.scss.agents.Agent;
import ie.tcd.scss.agents.discretization.clustering.algorithms.gng.LayeredGrowingNeuralGaz;
import ie.tcd.scss.agents.discretization.clustering.algorithms.gng.LayeredGrowingNeuralGaz.ActionDistance;
import ie.tcd.scss.agents.discretization.clustering.algorithms.gng.Node;
import ie.tcd.scss.agents.discretization.clustering.distance.Distance;
import ie.tcd.scss.agents.discretization.clustering.distance.EuclidianDistance;
import ie.tcd.scss.agents.discretization.clustering.input.Signal;
import ie.tcd.scss.agents.perception.Perception;
import ie.tcd.scss.environment.implementations.MountainCar;
import ie.tcd.scss.environment.implementations.MountainCarStateSpaceGridDiscretization;
import ie.tcd.scss.learning.Action;
import ie.tcd.scss.learning.State;
import ie.tcd.scss.ui.listeners.AggregatorListener;
import ie.tcd.scss.ui.listeners.QLearnerListener;
import ie.tcd.scss.ui.listeners.TrainableObserver;

public class GNGAggregator /*extends LearnerAgent */extends Agent implements QLearnerListener {

	private ArrayList<AggregatorListener> agglisteners = new ArrayList<AggregatorListener>();
	

	protected ArrayList<LayeredGrowingNeuralGaz> gngs = new ArrayList<LayeredGrowingNeuralGaz>();


	private MountainCarStateSpaceGridDiscretization discretization;

	private boolean isInitialized = false;
	
	private Distance distance = new EuclidianDistance();


	private double discount = 1.0;


	private long gridCorrespondingEpisode = 0;


	private long bestEpisodeCorrespondingGrid;
	
	public boolean isInitialized() {
		return isInitialized;
	}


	public GNGAggregator(MountainCar mountainCar, MountainCarStateSpaceGridDiscretization discretization) {

		super(mountainCar);
		
		for(@SuppressWarnings("unused") Action a : this.actions) {
			this.gngs.add(new LayeredGrowingNeuralGaz());
		}
		
		this.discretization=discretization;
	}


	@Override
	public void update(State st, long stateIndexX, long stateIndexY, Action a, long episode) {

		Signal s = new Signal();
		s.addData(new Double(stateIndexX));
		s.addData(new Double(stateIndexY));
		
		
		LayeredGrowingNeuralGaz gng = this.gngs.get(a.getIndex());
		gng.updateDiscount(this.discount);
		gng.learn(s, st, a);
			
		
		this.gridCorrespondingEpisode  = episode;
		
		if(this.isInitialized) {

			this.fireListeners();
			
			
		} else {
			final ArrayList<Node> nodes = new ArrayList<Node>();
			for(LayeredGrowingNeuralGaz lgng : this.gngs) {
				nodes.addAll(lgng.getNodes());
			}
			
			this.isInitialized =(nodes.size()>=this.gngs.size()*2);
		}
		
	
	}

	
	@Override 
	public long train() {
		
		if(this.isInitialized){
		
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
				this.bestEpisodeCorrespondingGrid = this.gridCorrespondingEpisode;
			}
			
			
			this.scoreExploitation = steps;
			
			this.fireTrainObservers(this.gridCorrespondingEpisode, steps, bestScoreEver, 1);
			
			
			episodeNumber++;
			
			return steps;
		}
		
		return -1;
	
		} else 
		
			for(TrainableObserver listener : this.trainObservers) {
				listener.trainingResult(this.gridCorrespondingEpisode,10000, bestScoreEver,1);
			}
			
		return 10000;
		
		
	}
	
	@Override
	public void learn(State sold, Action a, double reward, State snew) {

		if(this.isInitialized) {
			
			
			Signal s1 = new Signal();
			s1.addData(new Double(this.discretization.getStateXIndex(sold)));
			s1.addData(new Double(this.discretization.getStateYIndex(sold)));
			
			Signal s2 = new Signal();
			s2.addData(new Double(this.discretization.getStateXIndex(snew)));
			s2.addData(new Double(this.discretization.getStateYIndex(snew)));

		}
		
	}
	
	
	
	@Override
	public Action getPolicy(Perception p) {
		
		State state = this.discretization.mapPerception(p);
		
		Signal s = new Signal();
		s.addData(new Double(this.discretization.getStateXIndex(state)));
		s.addData(new Double(this.discretization.getStateYIndex(state)));
		
		double closestDist = Double.MAX_VALUE;
		Action action = null;
		for(LayeredGrowingNeuralGaz gng : this.gngs) {
			
			if(gng.isInitialized()) {
				ActionDistance ad = gng.getPolicy(s);
				
				if(ad.distance<closestDist) {
					action = ad.action;
					closestDist=ad.distance;
				}
			}
		}
		
		this.setConfidenceForLastAction(closestDist);

		return action;

	}
	
	

	public void addAggregatorListener(AggregatorListener listener) {
		this.agglisteners.add(listener);
	}
	
	
	private void fireListeners() {
		
		final ArrayList<Node> nodes = new ArrayList<Node>();
		for(LayeredGrowingNeuralGaz gng : this.gngs) {
			nodes.addAll(gng.getNodes());
		}
	
		for(AggregatorListener listener : this.agglisteners) {
			listener.update(nodes);
		}
	}


	@Override
	public Element toXml(Document doc) {
		for(AggregatorListener l: this.agglisteners) {
			l.save(System.currentTimeMillis() + "_GngNodes");
		}
		return null;
	}


	@Override
	public Action decide(Perception p) {
		return this.getPolicy(p);
	}


	@Override
	public void reset() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void updateMatching(ArrayList<State> states, HashMap<State, Long> xIndices, HashMap<State, Long> yIndices) {

		
		final ArrayList<Node> nodes = new ArrayList<Node>();
		for(LayeredGrowingNeuralGaz gng : this.gngs) {
			
			for(Node n : gng.getNodes()) {	
				n.getMatchingStates().clear();
				nodes.add(n);
			}
		}
		
		for(State s : states) {
			
			Double x = new Double(xIndices.get(s));
			Double y = new Double(yIndices.get(s));
			
			Signal sig = new Signal();
			sig.addData(x);
			sig.addData(y);
			
			double closestDist = Double.MAX_VALUE;
			Node closestNode = null;
			
			for(Node n : nodes) {
				double distance = this.distance.computeDistance(sig, n);
				if(distance<closestDist) {
					closestDist=distance;
					closestNode=n;
				}
			}
			
			closestNode.addMatchingState(s);
				
		}
	}


	@Override
	public void updateDiscount(double epsilon) {
		this.discount  = epsilon;
	}


	public long getBestEpisodeCorrespondingGrid() {
		return bestEpisodeCorrespondingGrid;
	}


	@Override
	public void updateTrain() {
			this.train();
	}



	

}
