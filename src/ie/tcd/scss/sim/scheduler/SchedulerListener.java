/**
 * 
 */
package ie.tcd.scss.sim.scheduler;

import ie.tcd.scss.agents.learning.LearnerAgent;

public interface SchedulerListener {

	public void update(long episodes, long steps, long bestScoreEver, LearnerAgent agent);
	
}
