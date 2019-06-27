package ie.tcd.scss.agents.discretization.clustering.algorithms.gng;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ie.tcd.scss.agents.discretization.Discretization;
import ie.tcd.scss.agents.discretization.clustering.algorithms.QLearnerPerception;
import ie.tcd.scss.agents.discretization.clustering.distance.Distance;
import ie.tcd.scss.agents.discretization.clustering.distance.EuclidianDistance;
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
 * @author Maxime Gueriau
 *
 */
public class LayeredGrowingNeuralGaz extends Discretization<QLearnerPerception> {

	/**
	 * Algorithm parameters
	 */

	/**
	 * "Elasticity" 
	 */

	//fraction of distance we move winner towards current signal
	protected static final double EPSILONb = 0.5d; // winner
	protected static final double EPSILONn = 0.1d; // winner's neighbors


	protected int maxNodes = 10;

	//gets removed after 200 without being a winner
	public static final int MAX_EDGE_AGE = 200;

	//number of times we call gng = number of gng learning steps
	private int signalNumber=0;
	/**
	 * The current number of runs to insert a new node (GNG).
	 */
	protected final int LAMBDA = 10;
	/**
	 * The value alpha for the GNG algorithm.
	 */

	//error - how was is node from signals it represents
	//calculated/updated every time the node is a winner, by the distance to specific signal - addistance, constantly increasing
	//alpha is used to forget old values of error
	protected final double ALPHA = 0.5d; //no different with higher or lower?

	/**
	 * The value beta for the GNG algorithm.
	 * The factor to forget old values.
	 */
	//discount for utility
	//utility is difference between second winner distance to signal and  winner distance to signal (positive)
	//forgetting old utilities
	protected final double BETA = 0.05d;
	/**
	 * The utility factor for the GNG-U algorithm.
	 */
	//the higher the number, the less likely youre to delete a node based on utility value
	//if lower then network doesnt grow
	protected double K = 1000d;

	/**
	 * The list of neurons the topological graph of GNG is composed of
	 */
	private ArrayList<Node> nodes = new ArrayList<Node>();	

	private ArrayList<Edge> edges = new ArrayList<Edge>();

	public LayeredGrowingNeuralGaz() {
	}

	public LayeredGrowingNeuralGaz(int maxNumberOfClasses) {
		this();
		this.maxNodes = maxNumberOfClasses;
	}

	public LayeredGrowingNeuralGaz(int maxNumberOfClasses, double K){
		this(maxNumberOfClasses);
	}

	/**
	 * For debugging purposes
	 */
	public ArrayList<Node> getNodes(){
		return this.nodes;
	}
	public ArrayList<Edge> getEdges(){
		return this.edges;
	}

	/**
	 * The distance computation use by the algorithm
	 */
	private Distance distance = new EuclidianDistance();

	private boolean initialized = false;

	//same value as epsilon in q-learning
	private double discount = 1.0;

	public ActionDistance getPolicy(Signal s) {

		double bestSquareDistance  = Double.MAX_VALUE;

		Node winner = null;

		for(Node n : this.nodes) {

			// calculate squared distance to input signal
			double nNodeDistance = this.distance.computeSquareDistance(s, n);

			// Keep track of current first and second winner
			if (nNodeDistance <= bestSquareDistance) { 
				winner = n;
				bestSquareDistance = nNodeDistance;
			}
		}

		return new ActionDistance(winner.getAction(),bestSquareDistance) ;

	}

	public void learn(Signal s, State st, Action a) {


		if(initialized) {


			// number of signals recieved
			this.signalNumber++;
			// smaller distance
			double bestSquareDistance  = Double.MAX_VALUE;
			// 2nd smaller distance
			double secondBestSquareDistance = Double.MAX_VALUE;
			// maxError
			double maxError = 0.0d;
			// minUtility
			double minUtility = Double.MAX_VALUE;
			// Winner node
			Node winner = null;
			// Second-winner node
			Node secondWinner = null;
			// Max error Node
			Node maxErrorNode = null;
			// Min utility Node
			Node minUtilityNode = null;

			// STEP 2 : Locate the nearest node (winner) and the second-nearest (runner-up)
			for(Node n : this.nodes){

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
					minUtilityNode = n;
				}


			}


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

			// STEP 4 - U : Accumulate utility
			winner.setUtility(winner.getUtility() + (secondBestSquareDistance - bestSquareDistance));

			// STEP 5.1 : Adaptation of Winner 
			this.adapt(winner, s, EPSILONb*discount);

			// STEP 5.2 : Adaptation of Winner's neighbors
			this.adaptNeighbors(winner, s);

			// STEP 6 :  Connection of the two winning nodes
			this.addEdge(winner, secondWinner);

			// STEP 7 : Remove obsolete edges and remove isolated nodes
			this.removeTooOldEdges();

			// STEP 7 - U : Remove node with lowest Utility (satisfying conditions)
			if(this.nodes.size()>2)
				this.removeMinUtilityNode(minUtilityNode, maxError);

			// STEP 8 : Check inserting node and insert if necessary
			if ( (signalNumber % LAMBDA) == 0 && maxErrorNode.getNeighborsNumber()>0 && this.nodes.size() < this.maxNodes) {
				this.generateNode(maxErrorNode, this.maximumErrorNeighbor(maxErrorNode), a);
			}

			// STEP 9 : Decrease all error variables and utility (9 - U)
			for(Node n : this.nodes){
				// Decay error and utility
				n.updateError(1.0d - BETA);
				n.updateUtility(1.0d - BETA);
			}


		} else { // first to steps

			Vector<Double> pos = new Vector<Double>();
			for(double dimension : s.getData()){
				pos.add(new Double(dimension));
			}

			if(this.nodes.size()<2) {

				Node n = new Node(a);
				n.setPosition(pos);

				this.nodes.add(n);


			}

			if(this.nodes.size()>=2){
				this.initialized=true;
			}


		}

		//		}

	}


	/**
	 * Remove the given node according to utility algorithm
	 * 
	 * @param node : the minUtilityNode
	 * @param error : the maxError
	 */
	private void removeMinUtilityNode(Node node, double error) {

		// remove node if MaxError / MinNodeUtility > K
		if(error > (node.getUtility() * K ) ){
			this.nodes.remove(node);
			// remove all edges linked to removed node
			ArrayList<Edge> edgesLinkedToNode = this.getAllEdgeLinkedTo(node);
			for(Edge e : edgesLinkedToNode){
				this.edges.remove(e);
			}
		}
	}


	/**
	 * Find the given node neighbor with the highest error.
	 * 
	 * @param node : the given node
	 * @return the neighbor node with highest error
	 */
	protected Node maximumErrorNeighbor(Node node) {

		Node maxErrorNode = null;
		double maxError = Double.NEGATIVE_INFINITY;

		for(Node neighbor : node.getNeighbors()){

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
	private void generateNode(Node node1, Node node2, Action a) {

		// create the new Node r
		Node newNode = new Node(a);
		// STEP 8.2 : compute the new Node offset (middle of given two nodes)
		final Vector<Double> offset = this.distance.computeMiddlePosition(node1, node2);

		// STEP 8.2 : interpolate coordinates
		newNode.setPosition(offset);

		// STEP 8.2 : add the node to the node set
		this.nodes.add(newNode);

		// STEP 8.3 : connects newNode to n1 and n2
		this.addEdge(node1, newNode);
		this.addEdge(node2, newNode);

		// STEP 8.3 : delete previous edge
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
	private void updateEdgeAgeConnectedTo(Node node) {

		final ArrayList<Edge> linkedEdges = this.getAllEdgeLinkedTo(node); 

		// increment all edges
		for (Edge edge : linkedEdges) {
			edge.incrementAge();
		}

		//this.updateNeighbors(node);
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
				Node n1 = e.getNode1();
				Node n2 = e.getNode2();
				it.remove(); // remove too old edges
				this.updateNeighbors(n1);
				this.updateNeighbors(n2);

				// delete isolated nodes
				if(n1.getNeighborsNumber()==0){
					this.nodes.remove(n1);
				}

				if(n2.getNeighborsNumber()==0){
					this.nodes.remove(n2);
				}

			}
		}

	}

	/**
	 * Connnects the given two nodes
	 * 
	 * @param n1
	 * @param n2
	 */
	private void addEdge(Node n1, Node n2) {
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
	private void removeEdge(Node n1, Node n2) {

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
	private boolean hasEdge(Node n1, Node n2) {
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
	private final ArrayList<Edge> getAllEdgeLinkedTo(Node n){

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
	private void updateNeighbors(Node n) {

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

	/**
	 * Adapts the position of the given Node according to GNG algorithm adaptation process 
	 *  
	 * @param n
	 * @param s
	 */
	private void adapt(Node n, Signal s, double epsilon){

		int position = 0;
		for(double dimension : s.getData()){
			double positionN = n.getPosition().get(position);
			double newPositionN = positionN + epsilon * (dimension - positionN);
			n.getPosition().set(position, newPositionN);

			position++;
		}

	}


	/**
	 * Adapts the positions of the neighbors of the given Node according to GNG algorithm neighbors adaptation process
	 * 
	 * @param n
	 * @param s
	 */
	private void adaptNeighbors(Node n, Signal s) {

		for(Node neighbor : n.getNeighbors()){
			this.adapt(neighbor, s, EPSILONn*discount);
		}

	}


	public class Edge {

		private final Node n1;
		private final Node n2;

		private double age = 0.0d;

		public Edge(Node n1, Node n2){
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

		public Node getNode1(){
			return this.n1;
		}

		public Node getNode2(){
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

	public void learn(Signal s1, Action a, double reward, Signal s2) {

		if(this.initialized) {
			// Winner node
			Node winnerS1 = null;
			Node winnerS2 = null;

			double bestSquareDistanceS1 = Double.MAX_VALUE;
			double bestSquareDistanceS2 = Double.MAX_VALUE;


			for(Node n : this.nodes){


				// calculate squared distance to input signal
				double nNodeDistance1 = this.distance.computeSquareDistance(s1, n);

				// Keep track of current first and second winner
				if (nNodeDistance1 <= bestSquareDistanceS1) { 
					winnerS1 = n;
					bestSquareDistanceS1 = nNodeDistance1;
				}


				// calculate squared distance to input signal
				double nNodeDistance2 = this.distance.computeSquareDistance(s2, n);

				// Keep track of current first and second winner
				if (nNodeDistance2 <= bestSquareDistanceS2) { 
					winnerS2 = n;
					bestSquareDistanceS2 = nNodeDistance2;
				}


			}

			if(winnerS1!=null && winnerS2!=null) {

				final double alpha=0.1;
				final double gamma=0.9;

				double oldQ = winnerS1.getQValue();


				double maxQ = winnerS2.getQValue();

				double newQ = oldQ+alpha*(reward+(gamma*maxQ)-oldQ);

				//update old Q value to new one
				winnerS1.setQValue(newQ);

			}

		}
	}

	public void removeNode(Node n) {
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

	public void addNode(Node n) {
		// new node will be removed so must link it with closest one
		this.nodes.add(n);
		//find closest one
		Node closestNode = null;
		double minDist = Double.MAX_VALUE;
		for(Node node : this.nodes) {
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


}
