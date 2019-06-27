package ie.tcd.scss.agents.discretization.clustering.input;

import java.util.ArrayList;

public class Signal {

	private ArrayList<Double> data = new ArrayList<Double>();

	public ArrayList<Double> getData() {
		return data;
	}

	public void addData(Double data) {
		this.data.add(data);
	}
	
}
