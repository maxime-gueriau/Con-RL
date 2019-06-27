/**
 * 
 */
package ie.tcd.scss.ui.listeners;

import ie.tcd.scss.learning.Action;

/**
 * @author Maxime
 *
 */
public interface SchedulerListener {

	public void update(double time, Action lastAction);
	
}
