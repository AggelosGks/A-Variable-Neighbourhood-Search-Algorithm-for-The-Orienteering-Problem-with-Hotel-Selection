import java.util.ArrayList;


public class Algorithm {
	public static Tour InitialSolutionLengthOrienteedBacktrack(){
		Tour grlength_tour=new Tour();//initaliaze current tour 
		ArrayList<Hotel> hotel_population=(ArrayList<Hotel>) Hotel.getHotel_population().clone();
		ArrayList<POI> poi_population=(ArrayList<POI>) POI.getPoi_population().clone();
		Hotel final_hotel=Hotel.getFinalDepot();//tour must finish at final specific hotel , here incidate by id=1
		hotel_population.remove(final_hotel);
		double tour_time_budget=Tour.gettour_length_budget();	
		int allowed_trips=Trip.getNumber_of_trips();//get allowed trips for Tour
		while(grlength_tour.calcTourLength()<tour_time_budget&&grlength_tour.getTrips().size()<allowed_trips){
		Hotel start_hotel;
		if(grlength_tour.getTrips().isEmpty()){//if first trip of tour pick start depot 
			 start_hotel=final_hotel;
		}
		else{//else get the last visited hotel of the last trip on tour
			int lastTr_index=grlength_tour.getTrips().size()-1;//the index of last trip of tour in list 
			Trip last_trip=grlength_tour.getTrips().get(lastTr_index);//get last trip add to tour
			 start_hotel=last_trip.GetTripEnd();//get final hotel of current trip
		}
		//after Trip is constructed add to Tour List
		grlength_tour.addTriptoTour(constructTripLengthOriented(start_hotel,hotel_population,poi_population,tour_time_budget,grlength_tour.getNumberOfTrips()));
		}
		grlength_tour.ReverseTourMassiveNodes();
		//AddPOISLineOrienteed(grlength_tour);
		grlength_tour.calcTourScore();//calculate score once after Tour is constructed
		grlength_tour.calcTourLength();
		return grlength_tour;
	}

	public static Trip constructTripLengthOriented(Hotel start_hotel,ArrayList<Hotel> hotel_population,ArrayList<POI> poi_population,double tour_limit,int trips_constructed){
		Trip current_trip=new Trip(start_hotel,(Trip.getNumber_of_trips()-1)-trips_constructed);//current trip represents the trip to returned one way or another
		boolean control=true;//variable to control finish of trip if length trip must be less than length trip limit
		while(current_trip.CalcTripLength()<=current_trip.getLength_limit()&&control){
			double length_limit_trip=current_trip.getLength_limit()-current_trip.CalcTripLength();
			int trip_id=current_trip.getId();
			System.out.println(trip_id);
			Node next_node=null;
			if(current_trip.getPermutation().size()==1){
				 next_node=greedyHPNextVisitLengthOriented(start_hotel,length_limit_trip,hotel_population,poi_population,trip_id);
			}else{
				Node last_node=current_trip.getLastVisited();
				 next_node=greedyHPNextVisitLengthOriented(last_node,length_limit_trip,hotel_population,poi_population,trip_id);
			}
			
			if(next_node instanceof Hotel){//if next_node is instance hotel then this node is the final hotel of trip so while loop terminates
				control=false;
				current_trip.setScore(current_trip.CalcTripScore());
			}
			current_trip.addNodetoPermut(next_node);
		}
		System.out.println(current_trip.toString());
		return current_trip;
	}
	
	public static Node greedyHPNextVisitLengthOriented(Node start_node,double length_limit_trip,ArrayList<Hotel> hotel_population,ArrayList<POI> poi_population,int trip_id){
		Node to_add=null;//the node to return
		
		
		
		return to_add;
}//end method
}
