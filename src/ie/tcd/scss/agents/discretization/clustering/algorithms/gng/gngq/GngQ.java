package ie.tcd.scss.agents.discretization.clustering.algorithms.gng.gngq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ie.tcd.scss.agents.discretization.Discretization;
import ie.tcd.scss.agents.discretization.clustering.algorithms.QLearnerPerception;
import ie.tcd.scss.agents.discretization.clustering.input.Signal;
import ie.tcd.scss.learning.Action;
import ie.tcd.scss.learning.State;
/**
 * 
 * Growing Neural Gaz Implementation
 * from Fritzke, 1995
 * 
 * Steps of the algorithms : 
 * 0. Initialize with two random units
 * 1. Get an input signal
 * 2. Find the nearest neuron s1 and second-nearest neuron s2
 * 3. Increment the age of all edges emanating from s1
 * 4. 
 * 
 * @author Maxime Gu�riau
 *
 */
public class GngQ extends Discretization<QLearnerPerception> {

	/**
	 * Algorithm parameters
	 */

	/**
	 * "Elasticity" 
	 */

	//fraction of distance we move winner towards current signal
	protected static final double EPSILONb = 0.5d;//0.05d;//0.5d; // winner
	protected static final double EPSILONn = 0.1d;//0.006d;//0.1d; // winner's neighbors

	private final double alpha = 0.1; // learning rate
	private final double gamma = 0.95;//0.9;  // discount factor
	
	public ArrayList<GNGQValue> qvalues = new ArrayList<GNGQValue>();

	
	private int nodeIndex = 0;
	
	protected int maxNodes = 20;

	//gets removed after 200 without being a winner
	public static final int MAX_EDGE_AGE = 100;

	//number of times we call gng = number of gng learning steps
	private int signalNumber=0;
	/**
	 * The current number of runs to insert a new node (GNG).
	 */
	//if remove utility, need higher lamba, like 100
	protected final int LAMBDA = 1000;
	/**
	 * The value alpha for the GNG algorithm.
	 */

	private GngQNode lastWinner = null;
	
	//error - how was is node from signals it represents
	//calculated/updated every time the node is a winner, by the distance to specific signal - addistance, constantly increasing
	//alpha is used to forget old values of error
	protected final double ALPHA = 0.1d;//0.5d;

	/**
	 * The value beta for the GNG algorithm.
	 * The factor to forget old values.
	 */
	//discount for utility
	//utility is difference between second winner distance to signal and  winner distance to signal (positive)
	//forgetting old utilities
	protected final double BETA = 0.05;//0.05d;

	/**
	 * The list of neurons the topological graph of GNG is composed of
	 */
	private ArrayList<GngQNode> nodes = new ArrayList<GngQNode>();	

	private ArrayList<Edge> edges = new ArrayList<Edge>();
	
	private ArrayList<Action> actions;

	public GngQ(ArrayList<Action> actions) {
		this.actions = actions;
		

		Random rand = new Random();
		
		Vector<Double> pos = new Vector<Double>();
		
		pos.add(new Double(rand.nextDouble()*10.0));
		pos.add(new Double(rand.nextDouble()*10.0));

		GngQNode n = new GngQNode(this.nodeIndex++);
		n.setAction(this.getRandomAction());
		n.setPosition(pos);

		this.nodes.add(n);
			
			
		for(Action ac : actions) {
			this.qvalues.add(new GNGQValue(n, ac, 0.0d));
		}
			
			
		Vector<Double> pos2 = new Vector<Double>();
		
		pos2.add(new Double(rand.nextDouble()*10.0));
		pos2.add(new Double(rand.nextDouble()*10.0));

		GngQNode n2 = new GngQNode(this.nodeIndex++);
		n2.setPosition(pos2);
		n2.setAction(this.getRandomAction());
		
		this.nodes.add(n2);
			
			
		for(Action ac : actions) {
			this.qvalues.add(new GNGQValue(n2, ac, 0.0d));
		}
			

		this.initialized=true;
		
		
	}
	
	public GngQ(int maxNumberOfClasses, ArrayList<Action> actions) {
		this(actions);
		this.maxNodes = maxNumberOfClasses;
	}
	
	public GngQ(int maxNumberOfClasses, double K, ArrayList<Action> actions){
		this(maxNumberOfClasses,actions);
		
	}

	/**
	 * For debugging purposes
	 */
	public ArrayList<GngQNode> getNodes(){
		return this.nodes;
	}
	public ArrayList<Edge> getEdges(){
		return this.edges;
	}

	/**
	 * The distance computation use by the algorithm
	 */
	private Distance2 distance = new EuclidianDistance2();

	private boolean initialized = false;

	//same value as epsilon in q-learning
	private double discount = 1.0;
	
	public Action getPolicy(State s) {


		GngQNode n = this.getRegionalState(s);
		
		if(n==null) {
			System.err.println("forced to return random action as no matching node!");
			return this.getRandomAction();
		} 
		
		ArrayList<GNGQValue> qValuesForNewState = this.getQValuesForNode(n);
		java.util.Collections.shuffle(qValuesForNewState);

		
	    double maxValue = -1000000;
	    Action bestAction = null;
	  
    	for(GNGQValue q : qValuesForNewState) {
    		
    		if(q.q>=maxValue) {
    			maxValue = q.q;
    			bestAction = q.a;
    		}
    	}

    	
    	
    	return bestAction;
		
	}
	
	public Action getPolicy(GngQNode n) {

		if(n==null) {
			System.err.println("forced to return random action as no matching node!");
			return this.getRandomAction();
		} 
		
		ArrayList<GNGQValue> qValuesForNewState = this.getQValuesForNode(n);
		java.util.Collections.shuffle(qValuesForNewState);

		
	    double maxValue = -1000000;
	    Action bestAction = null;
	  
    	for(GNGQValue q : qValuesForNewState) {
    		
    		if(q.q>=maxValue) {
    			maxValue = q.q;
    			bestAction = q.a;
    		}
    	}

    	
    	
    	return bestAction;
		
	}
	
	
	private Action getRandomAction(){ //pick random action
		Random rand = new Random();
		return this.actions.get(rand.nextInt(this.actions.size()));

	}

	
	
	private GngQNode getRegionalState(State s) {

		
		for(GngQNode node : this.getNodes()) {
			
			for(State state : node.getMatchingStates()) {
				
				if(state == s) {
					return node;
				}
			}
			
		}
		
		
		//if there, means that there is no matching, so better fix that
		
		GngQNode nearestNode = null;
		double shortestDistance = Double.MAX_VALUE;
		
		for(GngQNode n : this.nodes){

			double distance = this.distance.computeDistance(s.getSignal(), n);
			
			if(distance<shortestDistance) {
				shortestDistance=distance;
				nearestNode = n;
			}
		}
		
		if(nearestNode!=null) {
			return nearestNode;
		}
		
		System.err.println("No corresponding regional state/node found!");
		
		return null;
	}

	public ArrayList<GNGQValue> getQValuesForNode(GngQNode n) {
		
			
		if(n==null) {
			System.err.println("Null node !");
			System.out.println("Nodes size = " + this.nodes.size());
		}
		
		ArrayList<GNGQValue> qvals = new ArrayList<GNGQValue>();
	
		for(int j=n.getIndex()*actions.size();j<(n.getIndex()*actions.size()+this.actions.size());++j) {
	
			qvals.add(this.qvalues.get(j));
		}

		return qvals;
	}
	
	
	public void updateMatching(ArrayList<State> states, HashMap<State, Long> xIndices, HashMap<State, Long> yIndices) {

		
		final ArrayList<GngQNode> nodes = new ArrayList<GngQNode>();
		//for(LayeredGrowingNeuralGaz gng : this.gngs) {
			
			for(GngQNode n : this.nodes) {	
				n.getMatchingStates().clear();
				nodes.add(n);
			}
		//}
		
		for(State s : states) {
			
			Double x = new Double(xIndices.get(s));
			Double y = new Double(yIndices.get(s));
			
			Signal sig = new Signal();
			sig.addData(x);
			sig.addData(y);
			
			double closestDist = Double.MAX_VALUE;
			GngQNode closestNode = null;
			
			for(GngQNode n : nodes) {
				double distance = this.distance.computeDistance(sig, n);
				if(distance<closestDist) {
					closestDist=distance;
					closestNode=n;
				}
			}
			
			closestNode.addMatchingState(s);
				
		}
	}
	
	public void learn(Signal s, State st, Action a, double reward, Signal snew, State stnew) {
	
	
		
		if(initialized) {

			// number of signals recieved
			this.signalNumber++;

			// smaller distance
			double bestSquareDistance  = Double.MAX_VALUE;
			
			// smaller distance
			double bestSquareDistanceNewState  = Double.MAX_VALUE;
			
			// 2nd smaller distance
			double secondBestSquareDistance = Double.MAX_VALUE;
			// maxError
			double maxError = 0.0d;
			// minUtility
			double minUtility = Double.MAX_VALUE;
			// Winner node
			GngQNode winner = null;
			// Second-winner node
			GngQNode secondWinner = null;
			// Max error Node
			GngQNode maxErrorNode = null;
			
			// Winner node for new state
			GngQNode nnprime = null;
			
			// STEP 2 : Locate the nearest node (winner) and the second-nearest (runner-up)
			for(GngQNode n : this.nodes){

				// update node dimension
				if(n.getDimensions() != s.getData().size()){
					n.setDimensions(s.getData().size());
				}

				// calculate squared distance to input signal
				double nNodeDistance = this.distance.computeSquareDistance(s, n);

				// Keep track of current first and second winner
				if (nNodeDistance <= bestSquareDistance) { 
					secondWinner = winner;
					winner = n;
					secondBestSquareDistance = bestSquareDistance;
					bestSquareDistance = nNodeDistance;
				}

				// Calculate node with maximal Error
				if (n.getError() >= maxError) {
					maxError = n.getError();
					maxErrorNode = n;
				}

				// Calculate node with mininimum utility (GNG-U)
				if (n.getUtility() < minUtility) {
					minUtility = n.getUtility();
				}
					
				
				// calculate squared distance to input signal
				double nNodeDistanceNewState = this.distance.computeSquareDistance(snew, n);
				
				// Keep track of current first and second winner
				if (nNodeDistanceNewState <= bestSquareDistanceNewState) { 
					nnprime = n;
					bestSquareDistanceNewState = nNodeDistanceNewState;
				}
				
			}


			this.lastWinner = winner;
			
			// STEP 2.2 : find second-winner
			if (winner == secondWinner) {
				secondWinner = this.nodes.get(this.nodes.indexOf(winner)+1);
				secondBestSquareDistance = Double.MAX_VALUE;
			}
			
			for (int i = this.nodes.indexOf(winner) + 1 ; i < this.nodes.size() ; i++) {
				final double distance = this.distance.computeSquareDistance(s, this.nodes.get(i));
				if ( distance < secondBestSquareDistance) {
					secondWinner = this.nodes.get(i);
					secondBestSquareDistance = distance;
				}
			}

			// STEP 3 : Calculate the age of the connected edges and delete too old edges
			this.updateEdgeAgeConnectedTo(winner);

			// STEP 4 : Accumulate square error
			winner.setError(winner.getError() + bestSquareDistance);//this.distance.computeSqDistance(s, winner));
			

			this.learn(winner, a, reward, nnprime);// secondWinner);
			
			
			// STEP 5 :  Connection of the two winning nodes
			this.addEdge(winner, secondWinner);

			winner.addMatchingState(st);
			
			// STEP 6 : Remove obsolete edges and remove isolated nodes
			this.removeTooOldEdges();

	
			// STEP 7 : Check inserting node and insert if necessary
			if ( (signalNumber % LAMBDA) == 0 && maxErrorNode.getNeighborsNumber()>0 && this.nodes.size() < this.maxNodes) {
				this.generateNode(maxErrorNode, this.maximumErrorNeighbor(maxErrorNode), a);
			}

			// STEP 8 : Decrease all error variables and utility (9 - U)
			for(GngQNode n : this.nodes){
				// Decay error
				n.updateError(1.0d - BETA);

			}
			
			
			;


		} 
		
//		}

	}


	
	public void adapationAtTheEndOfEpisode() {
		
		if(this.lastWinner!=null) {
		
			// STEP 5.1 : Adaptation of Winner 
			Vector<Double> regionalStateCentoid = this.adapt(lastWinner, EPSILONb*discount);

			// STEP 5.2 : Adaptation of Winner's neighbors
			this.adaptNeighbors(lastWinner, regionalStateCentoid);

		}
			
		//then clean regional states
		for(GngQNode n : this.getNodes()) {
			n.getMatchingStates().clear();
		}
		
		
		
	}

	
	/**
	 * Find the given node neighbor with the highest error.
	 * 
	 * @param node : the given node
	 * @return the neighbor node with highest error
	 */
	protected GngQNode maximumErrorNeighbor(GngQNode node) {

		GngQNode maxErrorNode = null;
		double maxError = Double.NEGATIVE_INFINITY;

		for(GngQNode neighbor : node.getNeighbors()){
			if(neighbor.getError() > maxError){
				maxError = neighbor.getError();
				maxErrorNode = neighbor;
			}
		}

		return maxErrorNode;
	}

	/**
	 * Create a new Node according to model 
	 * The new node is created at the middle position between given nodes
	 * The error attached to the newly created node is computed as the interpolation between the orginial nodes errors 
	 *  
	 * @param node
	 */
	private void generateNode(GngQNode node1, GngQNode node2, Action a) {

		// create the new Node r
		GngQNode newNode = new GngQNode(this.nodeIndex++);
		newNode.setAction(this.getRandomAction());
		for(Action ac : actions) {
			this.qvalues.add(new GNGQValue(newNode, ac, 0.0d));
		}
		// STEP 7.2 : compute the new Node offset (middle of given two nodes)
		final Vector<Double> offset = this.distance.computeMiddlePosition(node1, node2);

		// STEP 7.2 : interpolate coordinates
		newNode.setPosition(offset);

		// STEP 7.2 : add the node to the node set
		this.nodes.add(newNode);

		// STEP 7.3 : connects newNode to n1 and n2
		this.addEdge(node1, newNode);
		this.addEdge(node2, newNode);

		// STEP 7.3 : delete previous edge
		this.removeEdge(node1, node2);

		// reduce errors of neighbor nodes of the new unit
		node1.updateError(1.0d - ALPHA);
		node2.updateError(1.0d - ALPHA);

		// interpolate error from neighbors => from article : initialize with node 1 error
		newNode.setError((node1.getError() + node2.getError()) / 2.0d);
	}

	/**
	 * Update the age of edges linked to given Node
	 * Also delete too old edges after age increment
	 * @param node : the current node
	 */
	private void updateEdgeAgeConnectedTo(GngQNode node) {

		final ArrayList<Edge> linkedEdges = this.getAllEdgeLinkedTo(node); 

		// increment all edges
		for (Edge edge : linkedEdges) {
			edge.incrementAge();
		}

	}

	/**
	 * Removes edges which age exceed parameter and delete isolated nodes 
	 * @param node : the current node
	 */
	private void removeTooOldEdges() {

		// check for obsolesence
		Iterator<Edge> it = this.edges.iterator();
		while(it.hasNext()){
			Edge e = it.next();
			if(e.ageExceedLimit()){
				GngQNode n1 = e.getNode1();
				GngQNode n2 = e.getNode2();
				it.remove(); // remove too old edges
				this.updateNeighbors(n1);
				this.updateNeighbors(n2);

				
				//if(this.nodes.size()>2) {
				
					// delete isolated nodes
					if(n1.getNeighborsNumber()==0){
						this.nodes.remove(n1);
					}
	
					if(n2.getNeighborsNumber()==0){
						this.nodes.remove(n2);
					}
					
				//}

			}
		}

	}

	/**
	 * Connnects the given two nodes
	 * 
	 * @param n1
	 * @param n2
	 */
	private void addEdge(GngQNode n1, GngQNode n2) {
		if(n1 != null && n2!=null && n1!=n2 ){
			if(this.hasEdge(n1,n2)) { // if the edge already exists, remove it to reset age
				this.removeEdge(n1, n2);
			} 

			this.edges.add(new Edge(n1, n2));
			this.updateNeighbors(n1);
			this.updateNeighbors(n2);
		}
	}

	/**
	 * Unconnnects the given two nodes
	 * 
	 * @param n1
	 * @param n2
	 */
	private void removeEdge(GngQNode n1, GngQNode n2) {

		Iterator<Edge> it = this.edges.iterator();
		while(it.hasNext()){
			Edge e = it.next();
			if((e.getNode1() == n1 && e.getNode2() == n2) || (e.getNode1() == n2 && e.getNode2() == n1)){
				it.remove();
			}
		}

		this.updateNeighbors(n1);
		this.updateNeighbors(n2);
	}

	/**
	 * Check if n1 and n2 are linked by an edge (direction doesn't matter)
	 * 
	 * @param n1 : Node 1
	 * @param n2 : Node 2
	 * @return true if there is an edge linking n1 and n2 (unoriented graph)
	 */
	private boolean hasEdge(GngQNode n1, GngQNode n2) {
		for(Edge e : this.edges){
			if((e.getNode1() == n1 && e.getNode2() == n2) || (e.getNode1() == n2 && e.getNode2() == n1))
				return true;
		}
		return false;
	}

	/**
	 * Gets the list of Nodes linked to given node
	 * @param n : the current node
	 * @return a list of corresponding edges
	 */
	private final ArrayList<Edge> getAllEdgeLinkedTo(GngQNode n){

		final ArrayList<Edge> linkedEdges = new ArrayList<Edge>();

		for(Edge edge : this.edges){
			if(edge.getNode1() == n || edge.getNode2() == n){
				linkedEdges.add(edge);
			}
		}

		return linkedEdges;
	}


	/**
	 * 
	 * @param n
	 */
	private void updateNeighbors(GngQNode n) {

		n.resetNeighbors();

		for(Edge e : this.edges){
			if(e.n1==n){
				n.addNeighbor(e.n2);
			}

			if(e.n2==n){
				n.addNeighbor(e.n1);
			}	
		}
	}

	
	private Vector<Double> adapt(GngQNode n, double epsilon){


		Vector<Double> centroid = new Vector<Double>();
		centroid.add(new Double(0));
		centroid.add(new Double(0));
		
		
		
		
		for(State s : n.getMatchingStates()) {
		
			
			centroid.set(0, new Double(centroid.get(0) + s.getSignal().getData().get(0)));
			centroid.set(1, new Double(centroid.get(1) + s.getSignal().getData().get(1)));
		}
		
		centroid.set(0, new Double(centroid.get(0)/n.getMatchingStates().size()));
		centroid.set(1, new Double(centroid.get(1)/n.getMatchingStates().size()));
		
		
		if(n.getMatchingStates().size()<=0) {
		
			System.out.println("Node is in ( " + n.getPosition().get(0) + " , " + n.getPosition().get(1) + " )");
			
			System.out.println("Node has " + n.getMatchingStates().size() + " matching states");
			
			System.out.println("Move node towards ( " + centroid.get(0) + " , " + centroid.get(1) + " )");
			
		}
		
		int position = 0;
		for(double dimension : centroid){
			double positionN = n.getPosition().get(position);
			double newPositionN = positionN + epsilon * (dimension - positionN);
			n.getPosition().set(position, newPositionN);
			position++;
		}
		
		
		return centroid;
	}


	/**
	 * Adapts the positions of the neighbors of the given Node according to GNG algorithm neighbors adaptation process
	 * 
	 * @param n
	 * @param s
	 */
	private void adaptNeighbors(GngQNode n, Vector<Double> centroid) {

		for(GngQNode neighbor : n.getNeighbors()){
			this.adaptNeighbourg(neighbor, EPSILONn*discount, centroid);
		}

	}


	private void adaptNeighbourg(GngQNode neighbor, double epsilon, Vector<Double> centroid) {
		int position = 0;
		for(double dimension : centroid){
			double positionN = neighbor.getPosition().get(position);
			double newPositionN = positionN + epsilon * (dimension - positionN);
			neighbor.getPosition().set(position, newPositionN);
			position++;
		}
	}


	public class Edge {

		private final GngQNode n1;
		private final GngQNode n2;

		private double age = 0.0d;

		public Edge(GngQNode n1, GngQNode n2){
			this.n1=n1;
			this.n2=n2;
			this.age=0.0d;
		}

		public boolean ageExceedLimit() {
			return this.age >= MAX_EDGE_AGE;
		}

		public void incrementAge() {
			this.age++;
		}

		public GngQNode getNode1(){
			return this.n1;
		}

		public GngQNode getNode2(){
			return this.n2;
		}
	}


	@Override
	public Element toXml(Document doc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected State map(QLearnerPerception p) {
		// TODO Auto-generated method stub
		return null;
	}


	public class ActionDistance {
		
		public Action action;
		public double distance;
		
		public ActionDistance(Action a, double distance) {
			this.action=a;
			this.distance=distance;
		}
	}


	public boolean isInitialized() {
		return this.initialized;
	}

	public void updateDiscount(double discount) {
		this.discount = discount;
	}


	public void removeNode(GngQNode n) {
		//remove node, then remove edges
		this.nodes.remove(n);
		// remove all edges linked to removed node
		ArrayList<Edge> edgesLinkedToNode = this.getAllEdgeLinkedTo(n);
		for(Edge e : edgesLinkedToNode){
			this.edges.remove(e);
		}
		
		if(this.nodes.size()<2) {
			this.initialized=false;
		}
	}

	public void addNode(GngQNode n) {
		// new node will be removed so must link it with closest one
		this.nodes.add(n);
		//find closest one
		GngQNode closestNode = null;
		double minDist = Double.MAX_VALUE;
		for(GngQNode node : this.nodes) {
			Signal s = new Signal();
			for(Double d : node.getPosition()) {
				s.addData(d);
			}
			double dist = this.distance.computeSquareDistance(s, n);
			if(dist<minDist) {
				minDist=dist;
				closestNode = node;
			}
		}
		//create link
		this.edges.add(new Edge(n, closestNode));
	}

		
	public GNGQValue getQValue(GngQNode n, /*State s, */Action a) {
			
			return this.qvalues.get(/*s.getIndex()*/n.getIndex()*(actions.size())+a.getIndex());
		}

	
	
	private void learn(GngQNode nn, Action a, double reward, GngQNode nnew) {
		
		double oldQ = this.getQValue(nn,a).q;

	    double maxQ = this.getMaxQValueForRegionalState(nnew);
	    
	    double newQ = oldQ+this.alpha*(reward+(this.gamma*maxQ)-oldQ);

	    //update old Q value to new one
	    this.getQValue(nn, a).q=newQ;
	    
	    
	    nn.setAction(this.getPolicy(nn));
	    
	}
	
	
	public double getMaxQValueForRegionalState(GngQNode n){ //for update formula
	
		ArrayList<GNGQValue> qValuesForNewState = this.getQValuesForNode(n);
		
		java.util.Collections.shuffle(qValuesForNewState);
		
	    double maxValue = Double.MIN_VALUE;
		for(GNGQValue q : qValuesForNewState) {
			if(q.q>maxValue)
				maxValue = q.q;
		}
		
		return maxValue;
	}

}
