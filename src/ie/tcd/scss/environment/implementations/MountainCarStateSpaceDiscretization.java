package ie.tcd.scss.environment.implementations;

import ie.tcd.scss.agents.discretization.Discretization;


public abstract class MountainCarStateSpaceDiscretization extends Discretization<MountainCarPerception> {

	protected final double positionMin = -1.2d; 
	protected final double positionMax = 0.6d;
	
	protected final double speedMin = -0.07d;
	protected final double speedMax = 0.07d;
	

}
