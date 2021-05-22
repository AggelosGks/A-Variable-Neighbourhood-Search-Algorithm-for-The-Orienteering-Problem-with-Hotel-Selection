import java.util.ArrayList;
import java.util.PriorityQueue;




public class Node {
	//class fields
	static boolean test=Initializer.isTestmode();
	static boolean overalltest=Initializer.isOverall_test();
	private static int number_nodes=0;
	private static ArrayList<Node> total_population=new ArrayList<Node>();
	
	//instance fields
	protected int node_id;
	protected double x_cordinate;
	protected double y_cordinate;
	protected PriorityQueue<NodeWithDist> dist_to_nodes;
	protected ArrayList<Hotel> hood;
	protected ArrayList<POI> p_hood;
	
	public Node(double x,double y,int id){
		number_nodes++;
		this.node_id=id;
		this.x_cordinate=x;
		this.y_cordinate=y;
		this.dist_to_nodes=new PriorityQueue<NodeWithDist>();
		this.hood=new ArrayList<Hotel>();
		total_population.add(this);

	}
	public Node(){
		
	}
	public Node(double x,double y){
		this.x_cordinate=x;
		this.y_cordinate=y;
		this.node_id=-1;
	}
	//getters and setters

	public static int getNumber_nodes() {
		return number_nodes;
	}

	public static void setNumber_nodes(int number_nodes) {
		Node.number_nodes = number_nodes;
	}

	public int getNode_id() {
		return node_id;
	}

	public void setNode_id(int node_id) {
		this.node_id = node_id;
	}

	public double getX_cordinate() {
		return x_cordinate;
	}

	public void setX_cordinate(int x_cordinate) {
		this.x_cordinate = x_cordinate;
	}

	public double getY_cordinate() {
		return y_cordinate;
	}

	public void setY_cordinate(int y_cordinate) {
		this.y_cordinate = y_cordinate;
	}

	public static ArrayList<Node> getTotal_population() {
		return total_population;
	}

	public static void setTotal_population(ArrayList<Node> total_population) {
		Node.total_population = total_population;
	}
	public Hotel getNearestHotel(ArrayList<Hotel> hotel_population){
		
		PriorityQueue<NodeWithDist> temp=new PriorityQueue<NodeWithDist>(this.dist_to_nodes);//defensive copy of 
		Node c=null;
		boolean control=true;
		while(control){
			if(temp.peek()==null){
				throw new AssertionError("Elimination of Hotels to select");
				
			}
			c=temp.remove().node;
			
			if(c instanceof Hotel){//check if it is hotel
			
				if(hotel_population.contains(c)){//check that exists in current pop given
					control=false;
				
				}
			}
		}
		return (Hotel)c;
	}
	public POI getNearestPOI(ArrayList<POI> poi_population){
		PriorityQueue<NodeWithDist> temp=new PriorityQueue<NodeWithDist>(this.dist_to_nodes);//defensive copy of 
		Node c=null;
		boolean control=true;
		while(control){
			if(temp.peek()==null){
				c=null;
			}
			c=temp.remove().node;
			if(c instanceof POI){
				if(poi_population.contains(c)){
					control=false;
				}
			}
		}
		return (POI)c;
	}
	//Returns the original nearest poi of the node calling the function.The nearest in graph excluding case of existance in solution
	public POI getOriginalNearestPOI(){
		PriorityQueue<NodeWithDist> temp=new PriorityQueue<NodeWithDist>(this.dist_to_nodes);//defensive copy of 
		Node c=null;
		boolean control=true;
		while(control){
			c=temp.remove().node;
			if(c instanceof POI){
				if(c.node_id!=this.node_id){
					control=false;
				}
			}
		}
		return (POI)c;
	}
	public POI getOriginalNearestPOI(POI exclude_node){
		PriorityQueue<NodeWithDist> temp=new PriorityQueue<NodeWithDist>(this.dist_to_nodes);//defensive copy of 
		Node c=null;
		boolean control=true;
		while(control){
			c=temp.remove().node;
			if(c instanceof POI){
				if(c.node_id!=this.node_id&&c.node_id!=exclude_node.node_id){
					control=false;
				}
			}
		}
		return (POI)c;
	}
public Hotel getOriginalNearestHotel(){
	PriorityQueue<NodeWithDist> temp=new PriorityQueue<NodeWithDist>(this.dist_to_nodes);//defensive copy of 
	Node c=null;
	boolean control=true;
	while(control){
		c=temp.remove().node;
		if(c instanceof Hotel){
			if(c.node_id!=this.node_id){//in other methods we exclude differently itself existance by second list , or we want this to happen
				control=false;
			}
		}
	}
	return (Hotel)c;
	}
	
	public POI getNearestPOIDistLine(PriorityQueue<NodeWithDist> list_dist_from_line){
		PriorityQueue<NodeWithDist> temp=new PriorityQueue<NodeWithDist>(this.dist_to_nodes);//defensive copy of 
		NodeWithDist candidate=null;
		boolean control=true;
		while(control){
			if(temp.peek()==null){
				throw new AssertionError("Elimination of POIS");
			}
			candidate=temp.remove();
			if(candidate.node instanceof POI){
				if(list_dist_from_line.contains(candidate)){
					control=false;
				}
			}
		}
		return (POI)candidate.node;
	}
	//both itself added cause start and final depot may be used as intermediate
	public  void getNeighbourDistance(){
		for(Node runner:total_population){
			
				double dist=Computation.EuclideianDistance(this, runner);
				if(runner instanceof POI){
					NodeWithDist temp=new NodeWithDist(runner,dist,((POI)runner).getScore());
					this.dist_to_nodes.add(temp);
				}else{
					NodeWithDist temp=new NodeWithDist(runner,dist,0);
					this.dist_to_nodes.add(temp);
				}
			
		}
	}
	/**
	 @GetNeighboursUnderRadius
	 This method returns the neighborhood of a node(list) that are feasible for visiting under two constraints depending on two different radius.
	 The first start radius is the current length limit of the trip that is constructed in each iteration, whereas the finish radius is the sum of all limits of trips constructed
	  so far plus the current one deducted from the total limit of whole tour.Both radius are given as parameters to the function.
	  Afterwards, the method draws a virtual circle with center  the node calling the function  and radius equal to start_radius.
	  After this another circle is drawn virtually with center the start hotel of the tour(for us is the end hotel due to backtracking)
	  and radius equal to finish radius.After circles are drawn , an iteration through the current hotel population,(given as parameter in each iteration
	  ) starts , examining for each node its ability to satisfy two constraints.The first constraint is that the distance to the neighbour examined each time
	  from the node calling the function is feasible under the trip limit.The second constraint is that if we end up visiting this node theoretically , is it possible
	  to end to the start hotel (0) if needed.So actually what needed is the Euclidean distance to this neighbor be less or equal
	  to the current trip limit and also the Euclideian dIstance from this neighbour to the end is also feasible (less or equal) according
	  to total limit of tour left.If constraints are satisfied the neighbour exist in hood and added to list.Notice that equality in total left limit of tour
	  might be considered in case a trip is constructed with starting node the end hotel (start hotel) and return to it in prelast trip.
	  Total left limit might be equal to zero but so can be the distance of a node to itself.If this happens then edge  is acceptable.
	  Finally notice that a different approach is considered for end hotel (start for us).This hotel may be used as intermediate in any time durring the tour
	  and also must be the end of the whole tour.So if this hotel CALLS the function some time 
	@author A.Gick
	@param length_radius : Represents the length limit of  the trip constructed
	@param total_left : Represents to total remaining length limit of tour
	@param h_pop : Represents the active (non visited) Hotels in each trip construct iteration
	@return hood_under_radius : An arraylist of Nodes , that are feasible of visiting under radius constraint
*/
	public ArrayList<Hotel> GetHNeighboursUnderRadius(double length_radius,ArrayList<Hotel> h_pop,double total_left){
		ArrayList<Hotel> hood_under_radius=new ArrayList<Hotel>();//saves the neighborhood of hotels that are feasible under constraints
		if(this.node_id==1&&length_radius==0.0){//if start finish is the only solution, notice hotel 1 is not included in pop separate check
			hood_under_radius.add((Hotel)this);
		}else{
			for(Hotel runner: h_pop){//check through current hotel population for feasible
				double dist_to_runner=Computation.EuclideianDistance(this,runner);
				double dist_to_end=Computation.EuclideianDistance(runner, Hotel.getStartDepot());
					if(dist_to_runner<=length_radius&&dist_to_end<=total_left){//if possible to go there from start and also finish in total_left limit
						hood_under_radius.add(runner);//then its in feasible neighbourhood
					}
			}
		}
		return hood_under_radius;
	}
	public ArrayList<POI> GetPNeighboursUnderRadius(double length_radius,ArrayList<POI> p_pop,double total_left){
		ArrayList<POI> hood_under_radius=new ArrayList<POI>();//saves the neighborhood of hotels that are feasible under constraints
		if(this.node_id==1&&length_radius==0.0){//if start finish is the only solution, notice hotel 1 is not included in pop separate check
			POI to_add=this.getNearestPOI(p_pop);
			hood_under_radius.add(to_add);
		}else{
			for(POI runner: p_pop){//check through current hotel population for feasible
				double dist_to_runner=Computation.EuclideianDistance(this,runner);
				double dist_to_end=Computation.EuclideianDistance(runner, Hotel.getStartDepot());
					if(dist_to_runner<=length_radius&&dist_to_end<=total_left){//if possible to go there from start and also finish in total_left limit
						hood_under_radius.add(runner);//then its in feasible neighbourhood
					}
			}
		}
		return hood_under_radius;
	}
	


	

	
}
