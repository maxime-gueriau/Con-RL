package ie.tcd.scss.sim;

import ie.tcd.scss.agents.learning.LearnerAgent;
import ie.tcd.scss.agents.perception.Perception;
import ie.tcd.scss.environment.Environment;
import ie.tcd.scss.learning.Action;
import ie.tcd.scss.sim.scheduler.Scheduler;
import ie.tcd.scss.ui.charts.StepsView;

public class Simulator extends Scheduler {

	public Simulator(Environment environment, String outputFile) {
		super(environment, outputFile, 1);
		
	}

	@Override
	protected void runAgent(LearnerAgent agent) {

		Perception p  = this.environment.getCurrentPerception();
		
		Action a = agent.decide(p);

//		System.out.println("Action = " + a.getName());
		
		double reward = this.environment.applyAction(a);
		
		Perception newp  = this.environment.getCurrentPerception();
		
		agent.learn(p, a, reward, newp);		
	
	}

	@Override
	protected void backUpAgents(double time) {
//		QLearner agent = (QLearner) this.agent;
//		
//		
//		System.out.println("Number of Q Values is " + agent.qvalues.size());
//		
//		for(QValue q : agent.qvalues) {
//			System.out.println(q.s.getName() + " --- " + q.a.getName() + " = " + q.q);
//		}
	}

}
