import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.*;

public class Instance {
	
	//class fields
	static boolean test=Initializer.isTestmode();
	static boolean overalltest=Initializer.isOverall_test();
	
	//instance fields
	private int number_of_trips;
	private int number_of_poi;
	private int number_of_extrahotels;
	private double tour_constraint;
	private ArrayList<Double> trips_constraints;
	private ArrayList<Double> cord_scor;
	
	public Instance(ArrayList<Double> data){
		this.number_of_poi=data.get(0).intValue()-2;
		this.number_of_extrahotels=data.get(1).intValue();
		this.number_of_trips=data.get(2).intValue();
		this.tour_constraint=data.get(3).intValue();
		this.trips_constraints=new ArrayList<Double>(data.subList(4, 4+number_of_trips));
		this.cord_scor=new ArrayList<Double>(data.subList(4+number_of_trips,data.size()));
	}
	
	public Instance(){
		
	}
	//setters and getters
	public int getNumber_of_trips() {
		return number_of_trips;
	}

	public void setNumber_of_trips(int number_of_trips) {
		this.number_of_trips = number_of_trips;
	}

	public int getNumber_of_poi() {
		return number_of_poi;
	}

	public void setNumber_of_poi(int number_of_poi) {
		this.number_of_poi = number_of_poi;
	}

	public int getNumber_of_extrahotels() {
		return number_of_extrahotels;
	}
	
	public void setNumber_of_extrahotels(int number_of_extrahotels) {
		this.number_of_extrahotels = number_of_extrahotels;
	}
	//end setters and getters
	/**
	 *This method created the current instance of the problem modified by the file given.
	 At start each static field of each class is computed.
	 Then hotel population is created.Hotels unique id's are represented by number from 0 to h+1, 
	 where h is the number of extra hotels.Start and final depot are represented by 0 and 1 respectively. 
	 After this, POI population is created.POI unique id's are represented by numbers from h+2 to h+n-1
	 In each population creation an integer variable is computed saving the upper bound of ids.
	 @variable index_counter: This variable indicates the index on list about x and y coordinates for each node.
		It is increased by 3(x,y,score) each time an instance is created.Hotel's score is zero so it is handled by the constructor of the class
	 */
	public void CreateInstance(){
		//set static fields of classes
		Trip.setNumber_of_trips(this.number_of_trips);//set number of trips allowed
		Hotel.setNumber_extra(this.number_of_extrahotels);//set number of extra hotels
		//POI.setNumber_poi(this.number_of_poi);//set number of points of interest
		Node.setNumber_nodes(2+this.number_of_extrahotels+this.number_of_poi);//set number of total nodes
		Tour.settour_length_budget(this.tour_constraint);
		int index_counter=0;
		/*
		 represents the enumeration of hotels including final and start depot
		numbers from 0 to h+1 , where h is the number of extra hotels
		*/
		int upper_bound_hids=2+this.number_of_extrahotels;
		/*
		 represents the enumeration of poi including
		numbers from h to h+n-1 , where h is the number of extra hotels
		*/
		int upper_bound_pids=upper_bound_hids+this.number_of_poi;
		//start hotel generation
		for(int hotels_id=0; hotels_id<upper_bound_hids; hotels_id++){//hotels_id is considered as a counter of unique id for each hotel
			double xc=cord_scor.get(index_counter);
			double yc=cord_scor.get(index_counter+1);
			new Hotel(xc,yc,hotels_id);
			index_counter=index_counter+3;
		}
		//start POI generation
		for(int poi_id=upper_bound_hids; poi_id<upper_bound_pids; poi_id++){
			double xc=cord_scor.get(index_counter);
			double yc=cord_scor.get(index_counter+1);
			int score=cord_scor.get(index_counter+2).intValue();
			new POI(xc,yc,poi_id,score);
			index_counter=index_counter+3;
		}
		
		ArrayList<Node> total_population=(ArrayList<Node>) Node.getTotal_population().clone();
		for(Node e:total_population){
			e.getNeighbourDistance();//assign a priority queue holding distance of neighbours
		}
		//save trip time budgets
		TreeMap<Integer,Double> id_time_trip=new TreeMap<Integer,Double>();
		for(int trip_id=0; trip_id<this.number_of_trips; trip_id++){
			id_time_trip.put(trip_id,this.trips_constraints.get(trip_id));
		}
		Trip.setTripids_TimeConstraints(id_time_trip);
		//testmode
		if(overalltest){
			ArrayList<Hotel> testlist=Hotel.getHotel_population();
			ArrayList<POI> testlist2=POI.getPoi_population();
			ArrayList<Node> testlist3=Node.getTotal_population();
			TreeMap<Integer,Double> testds=Trip.getTripids_TimeConstraints();
			assertTrue("Hotel Creation failed.", testlist.size()==2+this.number_of_extrahotels);//hotel assert
			assertTrue("POI creation failed", testlist2.size()==this.number_of_poi);//poi assert
			assertTrue("Node creation failed", testlist3.size()==(this.number_of_poi+this.number_of_extrahotels+2));//node assert
			assertTrue("Trip time constraint creation failed",testds.size()==this.number_of_trips);//trips assert 
			//print data
			System.out.println("------------------------------------------------------------------------------------------------------------------ ");
			for(Hotel e:testlist){
				//System.out.println("id:"+e.getNode_id()+" X:"+e.getX_cordinate()+" Y:"+e.getY_cordinate()+" Score:"+e.getScore());
				
			}
			System.out.println("*Hotel creation success* number of hotels created: "+testlist.size());
			
			for(POI e:testlist2){
				//System.out.println("id:"+e.getNode_id()+" X:"+e.getX_cordinate()+" Y:"+e.getY_cordinate()+" Score:"+e.getScore());
			}
			System.out.println("*POI creation success* number of POI created: "+testlist2.size());
			
			for(Map.Entry<Integer,Double> entry : id_time_trip.entrySet()) {
				//System.out.println("id:"+entry.getKey()+" Time budget:"+entry.getValue());
			}
			System.out.println("*Trips time constraint creation success* number of Trips allowed: "+id_time_trip.size());
			System.out.println("*Tour time constraint*"+Tour.gettour_length_budget());
			
			
		}
		
	}
}
