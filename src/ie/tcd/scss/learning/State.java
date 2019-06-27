package ie.tcd.scss.learning;

import ie.tcd.scss.agents.discretization.clustering.input.Signal;

public class State {


	private String name;

	private final int index;

	private Action lastActionGenerated = null;

	private long numberOfConsecutiveLastAction=0;

	private long numberOfVisits = 0;

	private Action policyAction;

	private double maxQValue;

	private Signal signal;


	public Signal getSignal() {
		return signal;
	}

	public State(int id) {
		this.index = id;
		this.name="State " + this.index;
	}

	public State(String name, int id) {
		this(id);
		this.name=name;
	}

	public String getName() {
		return name;
	}

	public int getIndex() {
		return this.index;
	}

	public void setPolicyAction(Action a) {
		this.policyAction=a;
	}

	public void setMaxQValue(double q) {
		this.maxQValue=q;
	}

	/***
	 * return the action the agent will select following its learnt policy
	 */
	public Action getPolicyAction() {
		return this.policyAction;
	}

	public void updateLastAction(Action a) {


		this.numberOfVisits++;

		if(lastActionGenerated==a) {
			numberOfConsecutiveLastAction++;
		} else {
			numberOfConsecutiveLastAction=0;
			lastActionGenerated=a;
		}

	}

	public long getNumberOfConsecutiveLastAction() {
		return numberOfConsecutiveLastAction;
	}

	public long getNumberOfVisits() {
		return numberOfVisits;
	}

	public double getMaxQValue() {
		return this.maxQValue;
	}

	public void setSignal(Signal s) {
		this.signal=s;
	}


}
