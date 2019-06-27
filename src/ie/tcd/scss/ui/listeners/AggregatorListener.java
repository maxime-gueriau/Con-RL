package ie.tcd.scss.ui.listeners;

import java.util.ArrayList;

import ie.tcd.scss.agents.discretization.clustering.algorithms.gng.Node;

public interface AggregatorListener {

	void update(ArrayList<Node> nodes);

	void save(String title);

}
