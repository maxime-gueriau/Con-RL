package ie.tcd.scss;

import java.awt.GridLayout;
import java.util.Date;

import javax.swing.JFrame;

import com.sun.corba.se.spi.orbutil.fsm.Guard.Result;

import ie.tcd.scss.agents.aggregation.GNGAggregator;
import ie.tcd.scss.agents.learning.qlearning.GNGQLearner;
import ie.tcd.scss.agents.learning.qlearning.QLearner;
import ie.tcd.scss.environment.implementations.MountainCar;
import ie.tcd.scss.environment.implementations.MountainCarStateSpaceGridDiscretization;
import ie.tcd.scss.output.ResultsGenerator;
import ie.tcd.scss.sim.Simulator;
import ie.tcd.scss.ui.charts.GNGView;
import ie.tcd.scss.ui.charts.MergedStepsView;
import ie.tcd.scss.ui.charts.PolicyView;
import ie.tcd.scss.ui.charts.StepsView;
import ie.tcd.scss.agents.selector.*;

public class Simulation {


	public static void main(String[] args) {


		/***
		 * Create the current mountain car environment
		 */
		MountainCar MCenv = new MountainCar();

		/***
		 * Agents 
		 */

		final int granularityXA = 10;
		final int granularityYA = granularityXA;


		MountainCarStateSpaceGridDiscretization discretizationA = new MountainCarStateSpaceGridDiscretization(granularityXA, granularityYA);
		QLearner learnerA = new QLearner(discretizationA, MCenv.copy()); // Mountain car should be a copy, cause it needs the same set of actions but a different version as it's used for policy trials only (full exploitation, not for learning)

		GNGAggregator aggregatorA = new GNGAggregator(MCenv.copy(), discretizationA);
		learnerA.addListener(aggregatorA);

		MountainCarStateSpaceGridDiscretization discretizationB = new MountainCarStateSpaceGridDiscretization(granularityXA, granularityYA);

		GNGQLearner learnerB = new GNGQLearner(discretizationB, MCenv.copy());


		SelectorAgent gngSelectorA = new GridGngSelectorAgent(MCenv.copy());
		gngSelectorA.addRepresentation(learnerA);
		gngSelectorA.addRepresentation(aggregatorA);

		gngSelectorA.addRepresentation(learnerB);


		/***
		 * MC Simulation		
		 */
		Simulator simulator = new Simulator(MCenv, "");
		simulator.setNumberOfEpisodes(500);

		simulator.addAgent(gngSelectorA);

		/***
		 * UI
		 */
		JFrame frame = new JFrame();
		frame.setLayout(new GridLayout(4, 2));

		PolicyView actionView1 = new PolicyView("Policy of sensor-based state space", MCenv.getActions(), granularityXA, granularityYA, -1.2, 0.6, -0.07, 0.07);
		learnerA.addTrainObserver(actionView1);
		frame.add(actionView1);

		MergedStepsView mergedStepsView = new MergedStepsView();

		StepsView qLearnerStepView1 = new StepsView("Grid agent performance", "");
		learnerA.addTrainObserver(qLearnerStepView1);
		learnerA.addTrainObserver(mergedStepsView);
		frame.add(qLearnerStepView1);


		GNGView gngView1 = new GNGView(MCenv.getActions(), granularityXA, granularityYA);
		aggregatorA.addAggregatorListener(gngView1);
		frame.add(gngView1);

		StepsView stepsView1 = new StepsView("GNG agent performance", " (as triggered by QLearning agent)");
		aggregatorA.addTrainObserver(stepsView1);
		aggregatorA.addTrainObserver(mergedStepsView);
		frame.add(stepsView1);


		PolicyView gngActionView1 = new PolicyView("Policy of ML-GNG state space", MCenv.getActions(), granularityXA, granularityYA, -1.2, 0.6, -0.07, 0.07);
		aggregatorA.addTrainObserver(gngActionView1);
		frame.add(gngActionView1);

		StepsView stepsViewSelector1 = new StepsView("Selector agent performance", " (when using the representation selector)");
		gngSelectorA.addTrainObserver(stepsViewSelector1);
		frame.add(stepsViewSelector1);


		JFrame finalFrame = new JFrame();
		finalFrame.setLayout(new GridLayout(1, 2));

		StepsView stepsViewReality = new StepsView("Environment actual performance", "");
		simulator.addTrainObserver(stepsViewReality);
		simulator.addTrainObserver(mergedStepsView);
		frame.add(stepsViewSelector1);				
		finalFrame.add(stepsViewReality);
		finalFrame.add(mergedStepsView);

		finalFrame.pack();
		finalFrame.setVisible(true);

		GNGView gngView2 = new GNGView(MCenv.getActions(), granularityXA, granularityYA);
		learnerB.addAggregatorListener(gngView2);
		frame.add(gngView2);

		StepsView stepsViewGngQ = new StepsView("GNGQ agent performance", "");
		learnerB.addTrainObserver(stepsViewGngQ);
		frame.add(stepsViewGngQ);

		learnerB.addTrainObserver(mergedStepsView);



		frame.setSize(640*2, 640*6);
		frame.setVisible(true);


		/***
		 * Run simulation
		 */
		simulator.run();

		long time = System.currentTimeMillis();
		mergedStepsView.save(time + "_Final");
		gngView1.save(time + "_GngNodes");
		actionView1.save(time + "_Grid");
		gngActionView1.save(time + "_Gng");

		gngView2.save(time + "_GngQ");

		System.out.println("Grid did "+ learnerA.getBestScoreExploitation()+ " (episode "+learnerA.getBestEpisode()+")");
		System.out.println("GNG did "+ aggregatorA.getBestScoreExploitation()+ " (episode "+aggregatorA.getBestEpisode()+")");
		System.out.println("GNGQ did "+learnerB.getBestScoreExploitation() + " (episode "+learnerB.getBestEpisode()+")");
		

	}

}
