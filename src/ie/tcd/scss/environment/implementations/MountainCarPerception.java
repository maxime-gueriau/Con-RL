package ie.tcd.scss.environment.implementations;

import ie.tcd.scss.agents.perception.Perception;

public class MountainCarPerception extends Perception {

	private final double currentPosition;
	private final double currentSpeed;
	
	public MountainCarPerception(double currentPosition, double currentSpeed) {
		this.currentPosition=currentPosition;
		this.currentSpeed=currentSpeed;
	}

	public double getCurrentPosition() {
		return currentPosition;
	}

	public double getCurrentSpeed() {
		return currentSpeed;
	}
	
}
