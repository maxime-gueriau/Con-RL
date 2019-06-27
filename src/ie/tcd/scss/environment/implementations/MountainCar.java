package ie.tcd.scss.environment.implementations;

import java.util.ArrayList;
import java.util.Random;

import ie.tcd.scss.agents.perception.Perception;
import ie.tcd.scss.environment.Environment;
import ie.tcd.scss.learning.Action;

public class MountainCar extends Environment {

	
	private double currentPosition = -0.5;
	private double currentSpeed = 0.0;
	private boolean isEndOfEpisode = false;
	
	private double rewardAtGoal = 99999;
	private double defaultReward = -1;
		
	public MountainCar() {
		super();
		
		this.actions.add(new MountainCarAction("ACTION_BACKWARDS",-1, 0));
		this.actions.add(new MountainCarAction("ACTION_COAST",0, 1));
		this.actions.add(new MountainCarAction("ACTION_FORWARD",1, 2));
	
	}
	
	public MountainCar copy() {
		MountainCar copy = new MountainCar();
		copy.actions=this.actions;
		return copy;
		
	}

	@Override
	public Perception getCurrentPerception() {
		return new MountainCarPerception(currentPosition, currentSpeed);
	}

	@Override
	public double applyAction(Action a) {
		
		double action = ((MountainCarAction) a).getValue();

		currentSpeed+= action * (0.001) + (-0.0025) * Math.cos(3*currentPosition);
		
		if(currentSpeed>0.07) {
			currentSpeed=0.07;
		}
		
		if(currentSpeed<-0.07) {
			currentSpeed=-0.07;
		}
		
		currentPosition+= currentSpeed;
		
		if(currentPosition<-1.2) {
			currentPosition=-1.2;
		}
		
		if(currentPosition>0.6) {
			currentPosition=0.6;
			this.isEndOfEpisode = true;
			return rewardAtGoal;
		} else {		
			return defaultReward;
		}
	}


	@Override
	public boolean hasReachedGoal() {
		return isEndOfEpisode;
	}

	@Override
	public void reset() {
		//reset to start position
		this.currentPosition = -0.5;
		this.currentSpeed = 0.0;
	
		this. isEndOfEpisode = false;
	}
	
	@Override
	public void resetRandom() {
		
		Random rand = new Random();
				
		double randPos = rand.nextDouble() * (0.6-(-1.2)) - 1.2;
		double randSpeed = rand.nextDouble() * (0.07-(-0.07)) - 0.07;

		System.out.println("new random pos = " + randPos);
		System.out.println("new random speed = " + randSpeed);
		
		this.currentPosition = randPos;
		this.currentSpeed = randSpeed;
		
		this. isEndOfEpisode = false;
	}

	@Override
	public ArrayList<Double> getVariables() {
		ArrayList<Double> variables = new ArrayList<Double>();
		variables.add(new Double(this.currentPosition));
		variables.add(new Double(this.currentSpeed));
		return variables;
	}


	
}
