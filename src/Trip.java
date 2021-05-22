import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;


public class Trip {
	
	//static fields
	private static int number_of_trips;
	private static TreeMap<Integer,Double> tripids_TimeConstraints;//a data structure saving the time budget for a trip given its id
	private static int share=-1;


	//instance fields
	private int id;
	private double trip_length;
	private int trip_score;
	private ArrayList<Node> permutation;
	private Hotel start_hotel;
	private Hotel end_hotel;
	private double length_limit;
	
	public Trip(Hotel start){
		
	
		if(share==number_of_trips-1){
			share=-1;
		}
		share++;
		this.id=share;
		this.permutation=new ArrayList<Node>();
		this.permutation.add(start);
		this.start_hotel=start;
		this.end_hotel=null;
		this.trip_length=0;
		this.trip_score=0;
		this.length_limit=tripids_TimeConstraints.get(this.id);
		
	}
	public Trip(Hotel start,int id){

		this.id=id;
		this.permutation=new ArrayList<Node>();
		this.permutation.add(start);
		this.start_hotel=start;
		this.end_hotel=null;
		this.trip_length=0;
		this.trip_score=0;
		this.length_limit=tripids_TimeConstraints.get(this.id);
		
	}
	//constructor for Trip with know start-end hotel, used in MetaHeuristic VNS implementation
	public Trip(Hotel start,Hotel end,int id){
		this.id=id;
		this.permutation=new ArrayList<Node>();
		this.permutation.add(start);
		this.permutation.add(end);
		this.start_hotel=start;
		this.end_hotel=end;
		this.trip_length=0;
		this.trip_score=0;
		this.length_limit=tripids_TimeConstraints.get(this.id);
	}
	

	
	
	//getters and setters

	public static int getShare() {
		return share;
	}

	public double getLength_limit() {
		return length_limit;
	}
	//use it if current trip is not constructed yet and forecast needed,notice that this is a static method,may be called if no instances have be
	public static double getLength_limitById(int id){
		double limit=tripids_TimeConstraints.get(id);
		return limit;
	}
	public void addNodetoPermut(Node e){
		this.permutation.add(e);
	}



	public void setLength_limit(double length_limit) {
		this.length_limit = length_limit;
	}

	public static int getNumber_of_trips() {
		return number_of_trips;
	}

	public static void setNumber_of_trips(int number_of_trips) {
		Trip.number_of_trips = number_of_trips;
	}

	public static TreeMap<Integer, Double> getTripids_TimeConstraints() {
		return tripids_TimeConstraints;
	}

	public static void setTripids_TimeConstraints(
			TreeMap<Integer, Double> tripids_TimeConstraints) {
		Trip.tripids_TimeConstraints = tripids_TimeConstraints;
	}

	public static void setShare(int share) {
		Trip.share = share;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public double getLength() {
		
		return trip_length;
	}

	public void setLength(int length) {
		this.trip_length = length;
	}

	public int getScore() {
		return trip_score;
	}

	public void setScore(int score) {
		this.trip_score = score;
	}

	public ArrayList<Node> getPermutation() {
		return permutation;
	}

	public void setPermutation(ArrayList<Node> permutation) {
		this.permutation = permutation;
	}

	public Hotel getStart() {
		return start_hotel;
	}

	public void setStart(Hotel start) {
		this.start_hotel = start;
	}

	public Hotel getEnd() {
		return end_hotel;
	}

	public void setEnd(Hotel end) {
		this.end_hotel = end;
	}

	//end getters and setters
	public Hotel GetTripStart(){
		Hotel start=null;
		System.out.println("FDFDFDFDFDF");
		for(Node e:this.permutation){
			
			if(e instanceof Hotel){
				start=(Hotel) e;
				break;
			}
		}
		return start;
	}
	public Hotel GetTripEnd(){
		if(this.permutation.get(permutation.size()-1) instanceof Hotel){
			return (Hotel)permutation.get(permutation.size()-1);
		}
		else{
			System.err.println("Error on permutation about end hotel");
			return null;
		}
	}
	public double CalcTripLength(){
		if(this.permutation.isEmpty()){
			this.trip_length=0;
			return trip_length;
		}else{
			double total_dist=0;
			for(int i=0; i<this.permutation.size()-1;i++){
				Node from=this.permutation.get(i);
				Node to=this.permutation.get(i+1);
				total_dist=total_dist+Computation.EuclideianDistance(from, to);
			}
			this.trip_length=total_dist;
			return trip_length;
		}
		
	}
	public double CalcFreeFlow(){
		return this.length_limit-this.trip_length;
	}
	public int CalcTripScore(){
		int total_score=0;
		if(this.permutation.isEmpty()){
			return 0;
		}else{
			for(Node runner : permutation){
				if(runner instanceof POI){
					total_score=total_score+((POI) runner).getScore();
				}
			}
			return total_score;
		}
	
	}
	public double getTripTimeBudget(){
		return tripids_TimeConstraints.get(this.id);
	}
	public int calcNumOfPOI(){
		return (this.permutation.size()-2);
	}
	public boolean isTripFeasible(){
		if(this.id==number_of_trips-1){//if trip is last one
			if(this.GetTripEnd()==Hotel.getFinalDepot()){//if not ends with last depot then not feasible
				if(this.length_limit>=this.trip_length){
					return true;
				}else{
					return false;
				}
			}else{
				return false;
			}
		}else{
			if(this.length_limit>=this.trip_length){
				return true;
			}else{
				return false;
			}
		}
		
	}
	@Override
	public String toString() {
		String permut="";
		for(Node e:this.permutation){
			if(e instanceof POI){
				permut=permut+"-"+Integer.toString(e.node_id)+"-";
			}
			else{
				permut=permut+"<"+Integer.toString(e.node_id)+">";
			}
		}
		double fflow=this.CalcFreeFlow();
	
		return "Trip [id=" + id + ", trip_length=" + trip_length
				+ ", trip_score=" + trip_score + ", permutation=" + permut + ", length_limit=" + length_limit + " free flow :"+fflow+ "]";
	}
	public Node getLastVisited(){
		Node last_one;
		int last_index=this.permutation.size()-1;
		last_one=this.permutation.get(last_index);
		return last_one;
	}
	public Node getPreLastVisited(){
		if(this.permutation.size()==1){
			throw new AssertionError("Not possible permutation.Only one element exists");
		}
		Node pre_last_one;
		int pre_last_index=this.permutation.size()-2;
		pre_last_one=this.permutation.get(pre_last_index);
		return pre_last_one;
	}
	public void addPoiToPermutation(POI next_to_add){
		int last_index=this.permutation.size()-1;//get current last index of list , may differ thats why computed firstly
		Node temp=this.permutation.remove(last_index);//save index to be removed
		this.permutation.add(next_to_add);//add currently last
		this.permutation.add(temp);//re put end hotel
	}
	public static double SumUpConstraints(int trip_id){
		double length_consumed=0;
		for(Map.Entry<Integer,Double> entry : tripids_TimeConstraints.entrySet()) {
			if(entry.getKey()>trip_id){
				length_consumed=length_consumed+entry.getValue();
			}
		}
		return length_consumed;
	}
	
}
