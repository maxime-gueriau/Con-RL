package ie.tcd.scss.environment.implementations;

import java.util.HashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ie.tcd.scss.agents.discretization.clustering.input.Signal;
import ie.tcd.scss.learning.State;

public class MountainCarStateSpaceGridDiscretization extends MountainCarStateSpaceDiscretization {

	private final double intervalX;
	private final double intervalY;
	
	private final int numberOfCellsX;
	private final int numberOfCellsY;
	
	private HashMap<State, Long> xIndex = new HashMap<State, Long>();
	private HashMap<State, Long> yIndex = new HashMap<State, Long>();
	
	public HashMap<State, Long> getXIndices(){
		return this.xIndex;
	}
	
	public HashMap<State, Long> getYIndices(){
		return this.yIndex;
	}
	
	public MountainCarStateSpaceGridDiscretization(int numberOfCellsX, int numberOfCellsY) {
		
		this.numberOfCellsX=numberOfCellsX;
		this.numberOfCellsY=numberOfCellsY;
		
		this.intervalX = (this.positionMax - this.positionMin) / (double)this.numberOfCellsX;
		this.intervalY = (this.speedMax - this.speedMin) / (double)this.numberOfCellsY;
		
		
		long xId = 0;
		long yId = 0;
		
		

		int statesIndex = 0;
		
		for(double x = positionMin;Math.round(x*100d)/100d<positionMax;x+=intervalX) {
				
			yId=0;
			for(double v = speedMin;Math.round(v*1000d)/1000d<speedMax;v+=intervalY) {

				double xl =Math.round(x*100d)/100d;
				double xr =Math.round((x+intervalX)*100d)/100d;
				
				double vl =Math.round(v*1000d)/1000d;
				double vr =Math.round((v+intervalY)*1000d)/1000d;
				
				State s = new State(xl+".."+ xr + ";" + vl + ".."+ vr, statesIndex++);

				
				Signal si = new Signal();
				si.addData(new Double(xId));
				si.addData(new Double(yId));
				s.setSignal(si);
				
				this.states.add(s);
				
				this.xIndex.put(s, xId);
				this.yIndex.put(s, yId);
				
				++yId;
			}
			
			++xId;
		}
		
		
		System.out.println(this.states.size() + " states have been created!");
			
	}
	
	@Override
	protected State map(MountainCarPerception p) {
		
		
		
		double xd = ((p.getCurrentPosition()-this.positionMin));
	
		int i = (int) Math.floor((xd/intervalX));
			
		if(i>=numberOfCellsX)
			i = numberOfCellsX-1;
		
		double yd = ((p.getCurrentSpeed()-this.speedMin));
		
		int j = (int) Math.floor((yd/intervalY));

		if(j>=numberOfCellsY)
			j = numberOfCellsY-1;
	

		try{ 
			this.states.get(i*(numberOfCellsY-1)+j) ;
		
		} catch (Exception e) {
			
			System.out.println("p X = " + p.getCurrentPosition());
			System.out.println("p v = " + p.getCurrentSpeed());
			System.out.println("problem");
			System.out.println("i = " + i  );
			System.out.println("j = " + j  );
			System.out.println("floor xd = " + Math.floor((xd/intervalX)));
			System.out.println("floor yd = " + Math.floor((yd/intervalY)));
			System.out.println("xd = " + xd);
			System.out.println("yd = " + yd);
		}
		
		return this.states.get(i*(numberOfCellsY-1)+j);
	}

	@Override
	public Element toXml(Document doc) {
		Element element = doc.createElement("Discretization");
		element.setAttribute("type", "MountainCarStateSpaceGridDiscretization");
		element.setAttribute("granularityX", ""+numberOfCellsX);
		element.setAttribute("granularityY", ""+numberOfCellsY);

		element.appendChild(super.toXml(doc));
		return element;
	}

	public long getStateXIndex(State s) {
		
		return this.xIndex.get(s);
	}

	public long getStateYIndex(State s) {
		
		return this.yIndex.get(s);
	}

}
