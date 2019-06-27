package ie.tcd.scss.agents.discretization;

import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ie.tcd.scss.agents.perception.Perception;
import ie.tcd.scss.learning.State;
import ie.tcd.scss.xml.XMLable;

public abstract class Discretization<T extends Perception> implements XMLable {
	
	protected final ArrayList<State> states = new ArrayList<State>();
	
	public State mapPerception(Perception p) {
		return this.map((T) p);
	}

	protected abstract State map(T p);
	
	public final ArrayList<State> getStates(){
		return this.states;
	}
	
	
	@Override
	public Element toXml(Document doc) {
		
		Element element = doc.createElement("States");
		
		for(State s : this.states) {
			Element stateElement = doc.createElement("State"); 
			stateElement.setAttribute("name", s.getName());
			stateElement.setAttribute("id", ""+s.getIndex());
			element.appendChild(stateElement);
		}
		
		return element;
	}

	
}
