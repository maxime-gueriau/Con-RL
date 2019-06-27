package ie.tcd.scss.sim.scheduler;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import com.sun.org.apache.xml.internal.serializer.OutputPropertiesFactory;

import ie.tcd.scss.agents.learning.LearnerAgent;
import ie.tcd.scss.agents.selector.GridGngSelectorAgent;
import ie.tcd.scss.environment.Environment;
import ie.tcd.scss.ui.listeners.TrainableObserver;


public abstract class Scheduler extends Thread {

	protected final Environment environment;

	private long numberOfEpisodes = -1;

	protected long episode = 0;

	protected long bestEpisodeEver = 0;

	protected long timeStep = 0;

	private ArrayList<SchedulerListener> listeners = new ArrayList<SchedulerListener>();

	private long startTime;

	protected ArrayList<LearnerAgent> agents = new ArrayList<LearnerAgent>();


	private long bestScoreEver = Long.MAX_VALUE;

	private ArrayList<TrainableObserver> observers = new ArrayList<TrainableObserver>();

	public long getEpisode(){
		return this.episode;
	}


	/**
	 * @param environment
	 * @param actions 
	 */
	public Scheduler(Environment environment, String outputFile, long seed) {
		this.environment=environment;
	}

	public void addAgent(LearnerAgent agent) {
		this.agents.add(agent);
	}

	protected abstract void runAgent(LearnerAgent a);

	@Override
	public void run() {
		//System.out.println("------------ START -----------");
		this.startTime = System.currentTimeMillis();
		while(((this.numberOfEpisodes<0 || this.episode<this.numberOfEpisodes))){


			for(LearnerAgent agent : this.agents) {

				this.episode++;
				this.resetAgent(agent);

				this.environment.reset();


				do{
					this.timeStep++;


					this.runAgent(agent);


				}  while(!environment.hasReachedGoal() && this.timeStep<100000);


				agent.train();

				if(this.timeStep<bestScoreEver) {
					bestScoreEver = this.timeStep;
					bestEpisodeEver = episode;
				}

				if(agent instanceof LearnerAgent) {
					this.fireListeners(this.episode,this.timeStep, this.bestScoreEver, agent);
				}

				if(agent instanceof GridGngSelectorAgent){
					((GridGngSelectorAgent) agent).endOfEpisode(this.episode);
				}

				int representation = 0;
				if(agent instanceof GridGngSelectorAgent){
					representation = ((GridGngSelectorAgent)agent).getLastUsedRepresentation();
				}

				if(representation!=-1)
					this.fireObservers(this.episode, this.timeStep, this.bestScoreEver, representation);


				this.timeStep=0;

				this.updateIndicators();

			}

		}

		this.backUpAgents(this.episode);
		for(LearnerAgent agent : this.agents) {
			this.save(agent);		
		}
		final long elapsedTime = System.currentTimeMillis() - this.startTime;

		System.out.println("elapsed time = " + (int)Math.floor((elapsedTime/1000.0/60.0)) + " mins " +  (int)Math.floor(((elapsedTime/1000.0)%60)) + " secs");

	}


	public long getBestEpisodeEver() {
		return bestEpisodeEver;
	}


	public long getBestScoreEver() {
		return bestScoreEver;
	}


	private void fireObservers(long episode, long steps, long bestScoreEver, int representation) {
		for(TrainableObserver observer : this.observers) {
			observer.trainingResult(episode, steps, bestScoreEver, representation);
		}
	}


	private void resetAgent(LearnerAgent agent) {
		agent.reset();
	}


	public synchronized void save(LearnerAgent agent) {

		String outputFile = "QLearner.xml";


		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder;
		try {
			docBuilder = docFactory.newDocumentBuilder();

			// root elements
			Document doc = docBuilder.newDocument();

			doc.appendChild(agent.toXml(doc));

			// write the content into xml file
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputPropertiesFactory.S_KEY_INDENT_AMOUNT, "4");
			DOMSource source = new DOMSource(doc);

			StreamResult result = new StreamResult(new File(outputFile));
			transformer.transform(source, result);

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}


	}


	/**
	 * 
	 */
	private void updateIndicators() {

	}


	/**
	 * 
	 */
	protected abstract void backUpAgents(double time);


	/**
	 * @param i
	 */
	public void setNumberOfEpisodes(int i) {
		this.numberOfEpisodes  = i;
	}

	/**
	 * @param environmentController
	 */
	public void addListener(SchedulerListener listener) {
		this.listeners.add(listener);
	}

	public void fireListeners(long episode, long steps, long bestScoreEver, LearnerAgent agent){
		for(SchedulerListener listener : this.listeners){
			listener.update(episode, steps, bestScoreEver, agent);
		}
	}



	public void addTrainObserver(TrainableObserver observer) {
		this.observers.add(observer);
	}



}
