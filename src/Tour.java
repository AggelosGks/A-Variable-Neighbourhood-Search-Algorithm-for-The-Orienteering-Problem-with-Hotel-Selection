import java.util.ArrayList;


public class Tour {
	
	//class fields
	private static  double tour_length_budget;
	
	//instance fields
	private ArrayList<Trip> trips;
	private int tour_score;
	private double tour_length;
	private double fflow;
	

	public Tour() {
		this.trips = new ArrayList<Trip>();
		this.tour_score = 0;
		this.tour_length = 0;
		this.fflow=0;
	}
	//constructor for Tour given a finite hotel sequence used in MetaHeuristic VNS implementation
	public Tour(ArrayList<Hotel> sequence){
		this.trips = new ArrayList<Trip>();
		this.tour_score = 0;
		this.tour_length = 0;
		this.fflow=0;
		int number_of_trips=Trip.getNumber_of_trips();
		for(int i=0; i<number_of_trips; i++){
			Hotel start=sequence.get(i);
			Hotel end=sequence.get(i+1);
			Trip temp=new Trip(start,end,i);
			this.trips.add(temp);
		}
	}
	//setters and getters 

	public int getNumberOfTrips(){
		if(this.trips.isEmpty()){
			return 0;
		}else{
			return this.trips.size();
		}
	}
	public double getFflow() {
		return fflow;
	}

	public void setFflow(double fflow) {
		this.fflow = fflow;
	}

	public ArrayList<Trip> getTrips() {
		return trips;
	}
	public static double gettour_length_budget() {
		return tour_length_budget;
	}
	@Override
	public String toString() {
		int occ=0;
		ArrayList<Trip> infeasibles=new ArrayList<Trip>();
		for(Trip runner:this.trips){
			if(runner.isTripFeasible()){
				occ++;
				System.out.println(runner.toString());
			}else{
				infeasibles.add(runner);
			}
		}
		if(occ==Trip.getNumber_of_trips()){
			System.out.println("TOUR IS FEASIBLE!");
		}else{
			System.out.println("This is the infeasibles tours : ");
			for(Trip run:infeasibles){
				String cause="";
				if(run.getLength()>run.getLength_limit()){
					cause=" Due to length constraint";
				}
				 if(run.getId()==Trip.getNumber_of_trips()-1){
					if(run.getEnd()!=Hotel.getFinalDepot()){
						cause=cause+"Due to wrong final depot";
					}
				}
				 if(run.getLength_limit()==0.0){
					cause=cause+"Due to no available move constraint";
				}
				
				System.out.println(run.toString()+cause);
			}
			System.out.println("*********************TOUR IS INFEASIBLE!************************");
		}
		return  "[tour_score=" + tour_score
				+ ", tour_length=" + tour_length +"tour limit="+Tour.tour_length_budget+" POIS visited : "+Integer.toString(this.calcPOIVisited())+"]";
	}

	public static void settour_length_budget(double tour_length_budget) {
		Tour.tour_length_budget = tour_length_budget;
	}
	public void setTrips(ArrayList<Trip> trips) {
		this.trips = trips;
	}
	public void addTriptoTour(Trip trip){
		this.trips.add(trip);
	}
	public int getTour_score() {
		return tour_score;
	}
	public void setTour_score(int tour_score) {
		this.tour_score = tour_score;
	}
	public double getTour_length() {
		return tour_length;
	}
	public ArrayList<Hotel> getHotelSequenceTour(){
		ArrayList<Hotel> h_sequence=new ArrayList<Hotel>();
		h_sequence.add(Hotel.getStartDepot());
		for(Trip runner:this.trips){
			if(runner.getId()<Trip.getNumber_of_trips()-1){
				Hotel end_add=runner.getEnd();
				h_sequence.add(end_add);
			}
		}
		
		h_sequence.add(Hotel.getFinalDepot());
	
		return h_sequence;
	}
	public void setTour_length(double tour_length) {
		this.tour_length = tour_length;
	}
	public Trip getLastTrip(){
		if(this.trips.isEmpty()){
			return null;
		}else{
			Trip last_trip=this.trips.get(this.trips.size()-1);
			return last_trip;
		}
	}
	public int calcPOIVisited(){
		int total_poi=0;
		for(Trip runner:this.trips){
			total_poi=total_poi+runner.calcNumOfPOI();
		}
		return total_poi;
	}
	public int calcTourScore(){
		int total_score=0;
		if(this.trips.isEmpty()){
			this.tour_score=0;
			return 0;
		}else{
			for(Trip e:this.trips){
				total_score=total_score+e.CalcTripScore();
			}
			this.tour_score=total_score;
			return total_score;
		}
	}
	public double calcTourLength(){
		double total_length=0;
		if(this.trips.isEmpty()){
			this.tour_length=0;
			return 0;
		}else{
			for(Trip e:this.trips){
				total_length=total_length+e.CalcTripLength();
			}
			this.tour_length=total_length;
			return total_length;
		}
	}
	public boolean isFeasible(){
		if(this.tour_length<=tour_length_budget){
			return true;
		}else{
			return false;
		}
	}
	public boolean isTourMaximum(){
		if(this.calcPOIVisited()==POI.getNumber_poi()){
			return true;
		}else{
			return false;
		}
	}
	public void calcTotalFree(){
		double total=0;
		for(Trip run:this.trips){
			total=total+run.CalcFreeFlow();
		}
		this.setFflow(total);
	}
	public void ReverseTourHotelsOnly(){
		int size=trips.size();
		//swap trips of tour 
		if(size%2==0){//even number of trips
			for(int j=0; j<=(size-1)/2; j++){
				int last_index=(size-1)-j;
				Trip last=this.trips.get(last_index);
				Trip first=this.trips.get(j);
				this.trips.set(last_index,first);
				this.trips.set(j,last);	
			}
		}
		else{//odd number of trips
			for(int j=0; j<(size-1)/2; j++){
				int last_index=(size-1)-j;
				Trip last=this.trips.get(last_index);
				Trip first=this.trips.get(j);
				this.trips.set(last_index,first);
				this.trips.set(j,last);	
			}
		}
		//swap start-finish hotel for each trip
		for(Trip runner:this.trips){
			 size=runner.getPermutation().size();
			if(runner.getPermutation().size()%2==0){//even
				for(int j=0; j<=(size-1)/2; j++){
					int last_index=(size-1)-j;
					Node last=runner.getPermutation().get(last_index);//save last visited hotel, actuall first for us due to backtracking
					Node first=runner.getPermutation().get(j);
					runner.getPermutation().set(last_index,first);
					runner.getPermutation().set(j,last);
					runner.setEnd((Hotel)first);
					runner.setStart((Hotel)last);
				}
			}else{
				for(int j=0; j<(size-1)/2; j++){
					int last_index=(size-1)-j;
					Node last=runner.getPermutation().get(last_index);
					Node first=runner.getPermutation().get(j);
					runner.getPermutation().set(last_index,first);
					runner.getPermutation().set(j,last);	
					runner.setEnd((Hotel)first);
					runner.setStart((Hotel)last);
				}
			}
		}
	}
	
	public void ReverseTourMassiveNodes(){
		int size=trips.size();
		//swap trips of tour 
		if(size%2==0){//even number of trips
			for(int j=0; j<=(size-1)/2; j++){
				int last_index=(size-1)-j;
				Trip last=this.trips.get(last_index);
				Trip first=this.trips.get(j);
				this.trips.set(last_index,first);
				this.trips.set(j,last);	
			}
		}
		else{//odd number of trips
			for(int j=0; j<(size-1)/2; j++){
				int last_index=(size-1)-j;
				Trip last=this.trips.get(last_index);
				Trip first=this.trips.get(j);
				this.trips.set(last_index,first);
				this.trips.set(j,last);	
			}
		}
		//swap start-finish hotel for each trip
		for(Trip runner:this.trips){
			 size=runner.getPermutation().size();
			if(runner.getPermutation().size()%2==0){//even
				for(int j=0; j<=(size-1)/2; j++){
					int last_index=(size-1)-j;
					Node last=runner.getPermutation().get(last_index);//save last visited hotel, actuall first for us due to backtracking
					Node first=runner.getPermutation().get(j);
					runner.getPermutation().set(last_index,first);
					runner.getPermutation().set(j,last);
					
				}
			}else{
				for(int j=0; j<(size-1)/2; j++){
					int last_index=(size-1)-j;
					Node last=runner.getPermutation().get(last_index);
					Node first=runner.getPermutation().get(j);
					runner.getPermutation().set(last_index,first);
					runner.getPermutation().set(j,last);	
				
				}
			}
		}
	}
	
	//end setters and getters
}
