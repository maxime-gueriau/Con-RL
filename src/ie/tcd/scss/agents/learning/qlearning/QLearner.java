package ie.tcd.scss.agents.learning.qlearning;

import java.util.ArrayList;
import java.util.Random;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ie.tcd.scss.agents.discretization.Discretization;
import ie.tcd.scss.agents.discretization.DiscretizorAgent;
import ie.tcd.scss.environment.implementations.MountainCar;
import ie.tcd.scss.environment.implementations.MountainCarStateSpaceGridDiscretization;
import ie.tcd.scss.learning.Action;
import ie.tcd.scss.learning.State;
import ie.tcd.scss.learning.qlearning.QValue;
import ie.tcd.scss.ui.listeners.QLearnerListener;

public class QLearner extends DiscretizorAgent {


	public ArrayList<QValue> qvalues = new ArrayList<QValue>();

	private long totalNumberOfInteractions = 0; // used in the decreasing epsilon-greedy exploration policy

	private double epsilon = 0.1; // epsilon for epsilon greedy exploration policy (i.e. prob of exploration)

	//this is used only if epsilon starts at 1
	private double E = 0.015; // exploration parameter (equals after how many episodes the agents definitely switches to full exploitation

	private final double alpha = 0.1; // learning rate
	private final double gamma = 0.9;  // discount factor

	private long numOfRandActions = 0;

	private long numOfDecisions;

	private long aggregationThreshold = 20; //once every how many steps should the aggregation be notified


	private boolean exploitation = false;

	protected ArrayList<QLearnerListener> listeners = new ArrayList<QLearnerListener>();

	public void addListener(QLearnerListener listener) {
		this.listeners.add(listener);
	}

	protected void fireListeners(State s, long stateIndexX, long stateIndexY, Action a, long episode) {
		for(QLearnerListener listener: this.listeners) {
			listener.update(s, stateIndexX, stateIndexY, a, episode);
			listener.updateMatching(this.discretization.getStates(), ((MountainCarStateSpaceGridDiscretization)this.discretization).getXIndices(), ((MountainCarStateSpaceGridDiscretization)this.discretization).getYIndices());
			listener.updateDiscount(this.epsilon);
		}
	}


	public QLearner(Discretization<?> discretization, ArrayList<Action> actions) {
		super(discretization, actions);

		for(State s : discretization.getStates()) {
			for(Action a : actions) {
				this.qvalues.add(new QValue(s, a, 0.0d));
			}
		}

		System.out.println("Created " + this.qvalues.size() + " QValues for QLearner Agent");

	}

	public QLearner(Discretization<?> discretization, MountainCar MCenv) {
		this(discretization, MCenv.getActions());
		this.trainingEnvironment=MCenv;
	}

	public QValue getQValue(State s, Action a) {

		return this.qvalues.get(s.getIndex()*(actions.size())+a.getIndex());
	}


	@Override
	protected Action decide(State s) {


		final Action action;

		this.setNumOfDecisions(this.getNumOfDecisions() + 1);

		double rand = Math.random();

		if(rand<epsilon) {
			this.setNumOfRandActions(this.getNumOfRandActions() + 1);
			action = this.getRandomAction();

			this.setConfidenceForLastAction(-1);

		} else {
			action = this.getPolicy(s);
		}

		s.updateLastAction(action);


		/***
		 * Tried to set maxQValue in state to be used for selecting nodes in gng after projection
		 */
		s.setMaxQValue(this.getMaxQValueForState(s));


		/***
		 * Triggers GNG based if action taken from this state was taken more than aggregationThreshold times
		 */
		if(s.getNumberOfConsecutiveLastAction()>=aggregationThreshold) {

			this.fireListeners(s,((MountainCarStateSpaceGridDiscretization)this.discretization).getStateXIndex(s), ((MountainCarStateSpaceGridDiscretization)this.discretization).getStateYIndex(s), this.getPolicy(s), this.episodeNumber);
			s.updateLastAction(null);
		}

		return action;

	}

	@Override
	protected Action getPolicy(State s){ //for update formula

		ArrayList<QValue> qValuesForNewState = this.getQValuesForState(s);
		java.util.Collections.shuffle(qValuesForNewState);

		double maxValue = -1000000;
		Action bestAction = null;

		for(QValue q : qValuesForNewState) {

			if(q.q>=maxValue) {

				maxValue = q.q;
				bestAction = q.a;
			}
		}


		this.setConfidenceForLastAction(s.getNumberOfConsecutiveLastAction());
		return bestAction;
	}

	private Action getRandomAction(){ //pick random action
		Random rand = new Random();
		return this.actions.get(rand.nextInt(this.actions.size()));

	}


	@Override
	protected void learn(State s, Action a, double reward, State snew) {

		if(!this.exploitation) {

			double oldQ = this.getQValue(s,a).q;


			double maxQ = getMaxQValueForState(snew);


			double newQ = oldQ+this.alpha*(reward+(this.gamma*maxQ)-oldQ);

			//update old Q value to new one
			this.getQValue(s, a).q=newQ;

			for(QLearnerListener listener : this.listeners) {
				listener.learn(s, a, reward, snew);
			}
		}



	}

	public double getMaxQValueForState(State s){ //for update formula

		ArrayList<QValue> qValuesForNewState = this.getQValuesForState(s);

		java.util.Collections.shuffle(qValuesForNewState);


		double maxValue = Double.MIN_VALUE;
		for(QValue q : qValuesForNewState) {
			if(q.q>maxValue)
				maxValue = q.q;
		}

		return maxValue;
	}



	public ArrayList<QValue> getQValuesForState(State snew) {
		ArrayList<QValue> qvals = new ArrayList<QValue>();


		for(int j=snew.getIndex()*actions.size();j<(snew.getIndex()*actions.size()+this.actions.size());++j) {			
			qvals.add(this.qvalues.get(j));
		}


		return qvals;
	}

	@Override
	public void reset() {


		//interaction is timestep, each time q value is updated. not reset per episode
		this.totalNumberOfInteractions++;


		//update of decrasing epsilon-greedy policy
		this.epsilon = Math.exp(-this.E*this.totalNumberOfInteractions);


		this.setNumOfRandActions(0);
		this.setNumOfDecisions(0);


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

		for(QValue qVal : this.qvalues) {
			Element stateElement = doc.createElement("QValue"); 
			stateElement.setAttribute("state", ""+qVal.s.getIndex());
			stateElement.setAttribute("action", ""+qVal.a.getIndex());
			stateElement.setAttribute("value", ""+qVal.q);
			qValueselement.appendChild(stateElement);
		}

		element.appendChild(qValueselement);


		return element;

	}

	public long getNumOfRandActions() {
		return numOfRandActions;
	}

	public void setNumOfRandActions(long numOfRandActions) {
		this.numOfRandActions = numOfRandActions;
	}

	public long getNumOfDecisions() {
		return numOfDecisions;
	}

	public void setNumOfDecisions(long numOfDecisions) {
		this.numOfDecisions = numOfDecisions;
	}


}
