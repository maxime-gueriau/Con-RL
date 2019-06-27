package ie.tcd.scss.environment.implementations;

import ie.tcd.scss.learning.Action;

public class MountainCarAction extends Action {
	
	private final double value;
	
	public MountainCarAction(String name, double value, int index) {
		super(name, index);
		this.value=value;
	}

	public double getValue() {
		return value;
	}

}
