import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.TreeMap;


public class Computation {

	static boolean test=Initializer.isTestmode();
	static boolean overalltest=Initializer.isOverall_test();

	//Score Orienteed
	/**
	 @InitialSolutionScoreOrienteed
	This method constructs a feasible greedy score-oriented solution to be the initial one.
	At start if not trip is created then the start depot is picked otherwise the last visited hotel of the last trip is picked
	to be the starting one in the next trip.After picking up the starting hotel the passes the parameter to method responsible for constructing
	a trip.The hotel and POI population are copied defensively in lists once at start and passed as arguments to the next methods called.
	Thats because any change to the above lists in any of the methods called affect continuously the heuristic process.
	Finally the method return a feasible tour greedy score orienteed
	@author A.Gick
 */
	public static Tour InitialSolutionScoreOrienteed(){
		Tour greedy_tour=new Tour();
		//get defensive copy of both population 
		ArrayList<Hotel> hotel_population=(ArrayList<Hotel>) Hotel.getHotel_population().clone();
		ArrayList<POI> poi_population=(ArrayList<POI>) POI.getPoi_population().clone();
		double tour_time_budget=Tour.gettour_length_budget();	
		int allowed_trips=Trip.getNumber_of_trips();//get allowed trips for Tour
		while(greedy_tour.calcTourLength()<tour_time_budget&&greedy_tour.getTrips().size()<allowed_trips){
			Hotel start_hotel;
			if(greedy_tour.getTrips().isEmpty()){//if first trip of tour pick start depot 
				 start_hotel=Hotel.getStartDepot();
				
			}
			else{//else get the last visited hotel of the last trip on tour
				Trip last_trip=greedy_tour.getTrips().get(greedy_tour.getTrips().size()-1);//get last trip 
				 start_hotel=last_trip.GetTripEnd();//get final hotel of last trip
			}
			greedy_tour.addTriptoTour(constructTripScoreOriented(start_hotel, poi_population,hotel_population));
			
		}
		greedy_tour.calcTourScore();
		return greedy_tour;
	}
	/**
	 @constructTripScoreOriented
	@author A.Gick
	@param start_hotel : Represents the starting hotel of the trip
	@param poi_population : Represents the active (non visited) POI's in each trip construct iteration
	@param hotel_population : Represents the active (non visited) Hotels in each trip construct iteration
 */
	public static Trip constructTripScoreOriented(Hotel start_hotel,ArrayList<POI> poi_population,ArrayList<Hotel> hotel_population){
		Trip current_trip=new Trip(start_hotel);//current trip represents the trip to returned one way or another
		boolean control=true;//variable to control finish of trip if length trip must be less than length trip limit
		while(current_trip.CalcTripLength()<current_trip.getLength_limit()&&control){
			int lastindex=current_trip.getPermutation().size()-1;//get index of last Node visited in this trip
			double length_available=current_trip.getLength_limit()-current_trip.getLength();//calculate available length flow
			Node starting_node=current_trip.getPermutation().get(lastindex);//get last visited Node for this trip,notice might be either hotel or POI
			Node next_node=greedyNextVisitScoreOriented(starting_node,poi_population,length_available,hotel_population);
			if(next_node instanceof Hotel){//if next_node is instance hotel then this node is the final hotel of trip so while loop terminates
				control=false;
				current_trip.setScore(current_trip.CalcTripScore());
			}
			current_trip.addNodetoPermut(next_node);
		}
		return current_trip;
	}
	/**
   @author A.Gick
	@param start_node : Represents the starting hotel of the trip
	@param poi_population : Represents the active (non visited) POI's in each trip construct iteration
	@param hotel_population : Represents the active (non visited) Hotels in each trip construct iteration
	@param length_available : Represents the available length limit remained in each iteration
 */
	public static Node greedyNextVisitScoreOriented(Node start_node,ArrayList<POI> poi_population,double length_available,ArrayList<Hotel> hotel_population){
		if(poi_population.size()==0){
			Node final_Hotel=start_node.getNearestHotel(hotel_population);
			return final_Hotel;
		}
		PriorityQueue<NodeWithScore> scores=new PriorityQueue<NodeWithScore>();//initialize priority queue for hold pois sorted by score
		for(POI runner:poi_population){
			NodeWithScore newnode=new NodeWithScore(runner, runner.getScore());//create new node with score
			scores.add(newnode);
		}
		boolean control=true;
		Node to_add=null;//this represents the Node the function returns one way or another
		if(start_node instanceof Hotel){//if start node is hotel consistent loop for best poi cause length available is maximum
			while(control&&scores.size()>0){
				double distance_needed=EuclideianDistance(start_node,scores.peek().node);
				POI first_visit=scores.remove().node;
				if(distance_needed<length_available){
					Hotel nearest=first_visit.getNearestHotel(hotel_population);//find nearest hotel
					double dist_finish=EuclideianDistance(first_visit,nearest);//calculate dist
					if(distance_needed+dist_finish<length_available){//check if to end is possible
						to_add=first_visit;//assign this POI
						poi_population.remove(to_add);
						control=false;
					}
				}
			}
			if(to_add==null){//if length limit of trip is too low then end trip with a hotel to hotel edge
				Node final_Hotel=start_node.getNearestHotel(hotel_population);
				return final_Hotel;
			}
		}else{
			boolean control_loop=true;
			while(scores.size()>(scores.size()/2)&&control_loop){
				Node curr=scores.remove().node;
				double dist_poi=EuclideianDistance(start_node,curr);//calculate dist to next poi
				Hotel nearest_second=((POI)curr).getNearestHotel(hotel_population);//get next poi's nearest hotel
				double dist_total_finish=EuclideianDistance(curr,nearest_second);//calculate dist next poi-nearest hotel
				if(dist_poi+dist_total_finish<length_available){//
					to_add=curr;
					control_loop=false;
					poi_population.remove(to_add);
				}
			}
			if(to_add==null){
				 to_add=((POI)start_node).getNearestHotel(hotel_population);//find nearest hotel
				 if(!(to_add.node_id==0||to_add.node_id==1)){ //start and finish hotel can be used as intermediate
					 hotel_population.remove(to_add);
				 }
			}
		}
		return to_add;
	}//end method
	//Score Orienteed
	
	//Length Orienteed
	/**
	 @InitialSolutionLengthOrienteedBacktrack
	This method constructs a feasible first tour, length orienteed.At start, defensive copies of both populations are created.
	After this start and final depot of the tour are saved to eliminate chance of infeasible tour.Then closet hotel to the final depot
	is selected and removed from hotel population,this is due to the fact that many instances have last trip length limit too low and only the initial closest one satisfies this.
	Then while the number of trips are less than the number allowed by instance, a new trip is constructed.Notice that if the first trip is constructed then inital depot is picked,
	otherwise the last hotel of the last trip is selected.In other words what the method computes is the shortest neighbor to start hotel  that exists in the cut of two circles created, and 
	if from there(forecast part) a circle is drawn with radius the next trip limit and then from the end hotel (0 for us due to backtracking) a radius with total tour limit left , another hotel exists in their cut.
	That way feasibility is ensured, it can easily be prooved that if in any given iteration both in select and forecast part, if a hotel does not exist
	then the instance have not feasible solution.Due to the principle that if the shortest path(straight line here directly to node) is not feasible under length constraint 
	then no other addition to the path may be feasible, as total length is for sure greater than current.
	@return grlength_tour : A tour length orienteed
 */
	public static Tour InitialSolutionLengthOrienteedBacktrack(){
		Tour grlength_tour=new Tour();//initaliaze current tour 
		ArrayList<Hotel> hotel_population=(ArrayList<Hotel>) Hotel.getHotel_population().clone();
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
		grlength_tour.addTriptoTour(constructTripLengthOriented(start_hotel,hotel_population,tour_time_budget,grlength_tour.getNumberOfTrips()));
		}
		grlength_tour.ReverseTourHotelsOnly();
		AddPOISLengthOrienteed(grlength_tour);  
		
		grlength_tour.calcTourScore();//calculate score once after Tour is constructed
		grlength_tour.calcTourLength();
		return grlength_tour;
	}
	/**
	 @InitialSolutionLineOrienteedBacktrack
	 **/
		public static Tour InitialSolutionLineOrienteedBacktrack(){
			Tour grlength_tour=new Tour();//initaliaze current tour 
			ArrayList<Hotel> hotel_population=(ArrayList<Hotel>) Hotel.getHotel_population().clone();
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
			grlength_tour.addTriptoTour(constructTripLengthOriented(start_hotel,hotel_population,tour_time_budget,grlength_tour.getNumberOfTrips()));
			}
			grlength_tour.ReverseTourHotelsOnly();
			AddPOISLineOrienteed(grlength_tour);
			grlength_tour.calcTourScore();//calculate score once after Tour is constructed
			grlength_tour.calcTourLength();
			return grlength_tour;
		}
	/**
	 @constructTripLengthOriented
	This method constructs a feasible trip,by continiusly picking a node from graph, notice that it is not possible to now when the trip will end so boolean variable 
	control, checks the termination of constructive process.Specifially if next node to be added to trip is instance of Hotel then , score and length is calculated and trip is finished.
	The function responsible for the selecting of nodes is guided in each iteration according to the length available of the trip.
	@param start_hotel : Represents the starting hotel of the trip
	@param hotel_population : Represents the active (non visited) Hotels in each trip construct iteration
	@param length_available : Represents the available length limit remained in each iteration
	@return current_trip : The current trip constructed 
**/
	public static Trip constructTripLengthOriented(Hotel start_hotel,ArrayList<Hotel> hotel_population,double tour_limit,int trips_constructed){
		Trip current_trip=new Trip(start_hotel,(Trip.getNumber_of_trips()-1)-trips_constructed);//current trip represents the trip to returned one way or another
		boolean control=true;//variable to control finish of trip if length trip must be less than length trip limit
		while(current_trip.CalcTripLength()<=current_trip.getLength_limit()&&control){
			double length_limit_trip=current_trip.getLength_limit();
			int trip_id=current_trip.getId();
			Node next_node=greedyHNextVisitLengthOriented(start_hotel,length_limit_trip,hotel_population,trip_id);
			if(next_node instanceof Hotel){//if next_node is instance hotel then this node is the final hotel of trip so while loop terminates
				control=false;
				current_trip.setScore(current_trip.CalcTripScore());
			}
			current_trip.addNodetoPermut(next_node);
		}
		return current_trip;
	}
	/**
	 @greedyHNextVisitLengthOriented
	This method is responsible for a decent first hotel selection.In other words it continusly constructs trips controlled from the while statement in previous method.
	The mathematical model of the method can be described geometrically mostly.Starting from explaining the parameters given at each call.
	A start node is given which is picked up from previous method, represents the start hotel of this trip.The node to be returned after execution of code
	is the end hotel of the trip, which will cause the while statement in previous method to end this trip construction and move foward to next trip.
	The id of the given trip that the method is going to be applied and the length limit are also given as parameters.Finally , the hotel population that is passed method to method from the 
	initial call.At start it is checked if the current to be constructed is the final one, if so end hotel(start for us) is returned.
	Due to the fact that we backtrack the whole tour starting to construct the last trip (first trip is considered to be 0 trip whereas the last trip is the d-1 trip where d indicates the number of trips allowed in isntance.
	If not the last trip is constructed then a circle is drawn virtually with center the start node and radius equal to the length limit of the trip.At the same time another circle is drawn
	with center the end depot hotel (start for us).Then after GetNeighboursUnderRadius method  is called we get the cut of these two circles for the start node.
	Continusly we get the closest hotel in start's node hood and we do a forecast by getting the candidates hood with radius decreased by next trip limit.In other words, we quickly test the fact that its feasible 
	to end to end depot within time interval.Notice that we test if the candidates hood contains at least one element.It might contain more if hood with many intermediate hotel is examined.
	On the other way having at least one, ensures that end depot can be reached.The function , acts like trying to find the nearest acceptable, to ensure that this trip will be created with 
	the minimum free flow to add POIS.
	@param start_node : The initial start hotel of the trip constructed in each iteration
	@param length_limit_trip :  The length limit of the trip constructed in each iteration
	@param hotel_population: The population of hotels dragged from the first call
	@param trip_id : The id of the trip constructed in each iteration
	@return to_add: The current node to be added to trip
*/
	public static Node greedyHNextVisitLengthOriented(Node start_node,double length_limit_trip,ArrayList<Hotel> hotel_population,int trip_id){
			Node to_add=null;//the node to return
			if(trip_id!=0){
				boolean h_selection=true;//variable to control hotel selection
				double cur_consumed=Trip.SumUpConstraints(trip_id)+length_limit_trip;//sum of length of trips so far constructed plus the current one to be constructed
				double total_left=Math.abs(Tour.gettour_length_budget()-cur_consumed);//compute total limit available of tour
				int next_trip_id=trip_id-1;//save in other variable to keep original value
				double next_trip_limit=Trip.getLength_limitById(next_trip_id);//compute next trip limit for forecast
				start_node.hood=start_node.GetHNeighboursUnderRadius(length_limit_trip, hotel_population, total_left);//get feasible hood of two circle cuts 
				while(h_selection){
					Hotel nearest_in_hood=start_node.getNearestHotel(start_node.hood);//get the nearest hotel of hood
					start_node.hood.remove(nearest_in_hood);//remove current nearest from hood
					//forecast
					nearest_in_hood.hood=nearest_in_hood.GetHNeighboursUnderRadius(next_trip_limit, hotel_population, Math.abs(total_left-next_trip_limit));//get hood of candidate to ensure feasibility
					if(!(nearest_in_hood.hood.isEmpty())){//if at least one hotel found then feasible
						to_add=nearest_in_hood;
						if(!(to_add.node_id==0||to_add.node_id==1)){
							hotel_population.remove(to_add);	
						}
						h_selection=false;
					}
				}
			}else{
				to_add=Hotel.getStartDepot();//we know we are too close already an we know that distance is feasible under constraint
			}
			return to_add;
	}//end method
	/**@AddPOISLineOrienteed
	 * 
	 * @param current_tour
	 */
	public static void AddPOISLineOrienteed(Tour current_tour){
		ArrayList<POI> poi_pop=(ArrayList<POI>) POI.getPoi_population().clone();//get total poi pop
		for(Trip runner : current_tour.getTrips()){//for each trip on tour
			double trip_limit=runner.getLength_limit();//get trip length constraint
			double length_available=trip_limit;//we do not abstract the distance from hotel to hotel cause intermediates node will be added
			ArrayList<Node> permut=(ArrayList<Node>) runner.getPermutation().clone();//get permutation of current trip
			Hotel start=runner.getStart();
			Hotel end=runner.getEnd();
			boolean control_stop=true;
			double dist_start_end=EuclideianDistance(start,end);
			if(start.node_id==end.node_id||dist_start_end==0){//if same hotel to start and end then no line can be drawn or i one hotel bumps into another
				while(control_stop&&poi_pop.size()>0){
					Node pre_last_visited=runner.getPreLastVisited();
					POI next_to_add=pre_last_visited.getNearestPOI(poi_pop);
					double dist_next_poi=EuclideianDistance(pre_last_visited,next_to_add);
					double dist_to_finish=EuclideianDistance(next_to_add,end);
					if(dist_next_poi+dist_to_finish<=length_available){//check 
						length_available=length_available-dist_next_poi;//decrease only be the dist to next node, in next iteration it may be feasible to visit another one poi and not terminate
						runner.addPoiToPermutation(next_to_add);
						poi_pop.remove(next_to_add);
					}else{
						control_stop=false;
					}	
				}
			}else{
				LineFormula line_hotels=new LineFormula(start,end);//get equation of line connecting these spots
				Node mid_node=new Node(line_hotels.x_mid,line_hotels.y_mid);//create mid of line edgin start-end hotels
				double dist_left=trip_limit-dist_start_end;
				double final_dist=Math.max(dist_start_end,dist_left);//apply max radius due to possible too low distance
				ArrayList<POI> current_hood=mid_node.GetNeighboursMidRadius(poi_pop,final_dist );//get neighborhood under start-end distance radius
				PriorityQueue<NodeWithDist> list_dist_from_line=new PriorityQueue<NodeWithDist>();
				for(POI e:current_hood){
					double distance_from_start=Math.pow(EuclideianDistance(e,start), 2);//distance from start in square
					double distance_from_line=Math.pow(line_hotels.DistanceFromLine(e),2);//distance from line in square
					if(distance_from_line<distance_from_start){
						double actual_distance=Math.sqrt(distance_from_start-distance_from_line);//actual distance from straight line, pythagoreus theorem
						NodeWithDist temp=new NodeWithDist(e, actual_distance,e.getScore());
						list_dist_from_line.add(temp);
					}
				}
				while(list_dist_from_line.size()>0){
					Node pre_last_visited=runner.getPreLastVisited();
					POI next_to_add=(POI)list_dist_from_line.remove().node;
					list_dist_from_line.remove(next_to_add);
					double dist_next_poi=EuclideianDistance(pre_last_visited,next_to_add);
					double dist_to_finish=EuclideianDistance(next_to_add,end);
					double sum=dist_next_poi+dist_to_finish;
					if(sum<length_available){
						length_available=length_available-dist_next_poi;
						poi_pop.remove(next_to_add);
						runner.addPoiToPermutation(next_to_add);
					}
				}
			}
			runner.setScore(runner.CalcTripScore());
			runner.CalcTripLength();
		}
	}
	/**@AddPOISLengthOrienteed
	 * 
	 * @param current_tour
	 */
	public static void AddPOISLengthOrienteed(Tour current_tour){
		ArrayList<POI> poi_pop=(ArrayList<POI>) POI.getPoi_population().clone();//get total poi pop
		for(Trip runner : current_tour.getTrips()){//for each trip on tour
			double trip_limit=runner.getLength_limit();//get trip length constraint
			double length_available=trip_limit;//we do not abstract the distance from hotel to hotel cause intermediates node will be added
			ArrayList<Node> permut=(ArrayList<Node>) runner.getPermutation().clone();//get permutation of current trip
			Hotel start=runner.getStart();
			Hotel end=runner.getEnd();
			boolean control_stop=true;
			double dist_start_end=EuclideianDistance(start,end);
			if(start.node_id==end.node_id||dist_start_end==0){//if same hotel to start and end then no line can be drawn or i one hotel bumps into another
				while(control_stop&&poi_pop.size()>0){
					Node pre_last_visited=runner.getPreLastVisited();
					POI next_to_add=pre_last_visited.getNearestPOI(poi_pop);
					double dist_next_poi=EuclideianDistance(pre_last_visited,next_to_add);
					double dist_to_finish=EuclideianDistance(next_to_add,end);
					if(dist_next_poi+dist_to_finish<=length_available){//check 
						length_available=length_available-dist_next_poi;//decrease only be the dist to next node, in next iteration it may be feasible to visit another one poi and not terminate
						runner.addPoiToPermutation(next_to_add);
						poi_pop.remove(next_to_add);
					}else{
						control_stop=false;
					}	
				}
			}else{
				//uses defensive if case that nearest poi is predecessor of start node and skip examine is needed
				ArrayList<POI> defen_copy=new ArrayList<POI>(poi_pop);
				while(defen_copy.size()>0){//&&length_available>1
					Node pre_last_visited=runner.getPreLastVisited();//get last visited
					POI next_to_add=(POI)pre_last_visited.getNearestPOI(defen_copy);//get nearest of it
					double dist_next_poi=EuclideianDistance(pre_last_visited,next_to_add);//dist to go
					double dist_to_finish=EuclideianDistance(next_to_add,end);//dist to end
					double sum=dist_next_poi+dist_to_finish;//sum up distances
					if(sum<length_available){//if its feasible
						length_available=length_available-dist_next_poi;//update constraints
						defen_copy.remove(next_to_add);
						poi_pop.remove(next_to_add);//remove from original population
						runner.addPoiToPermutation(next_to_add);
					}else{
						defen_copy.remove(next_to_add);//remove from temporary population
					}
				}
			}
			runner.setScore(runner.CalcTripScore());
			runner.CalcTripLength();
		}
	}
	//Length Orienteed
	
//Utilities - Functions
private static Hotel returnIntermediateH(Hotel start_depot,Hotel finish_depot,ArrayList<Hotel> h_pop){
	Hotel intermediate=null;
	boolean contro_h=true;
	ArrayList<Hotel> ererere=new ArrayList(h_pop);
	ererere.remove(Hotel.getFinalDepot());
	ererere.remove(Hotel.getStartDepot());
	while(contro_h){
		System.out.println("searching");
		intermediate=start_depot.getNearestHotel(ererere);
		double dist_inter_final=EuclideianDistance(intermediate,finish_depot);
		double dist_start_inter=EuclideianDistance(start_depot,intermediate);
		double dist_start_finish=EuclideianDistance(start_depot,finish_depot);
		double div=2;
		System.out.println(dist_inter_final);
		System.out.println(dist_start_inter);
		System.out.println(dist_start_finish);
		double result=Math.abs(dist_inter_final-dist_start_inter);
		if(result<5){
			contro_h=false;
		}
		System.out.println(intermediate.node_id);
		ererere.remove(intermediate);
	}
	return intermediate;
}
public static double EuclideianDistance(Node from,Node to){
	double x_diff=Math.pow((from.x_cordinate-to.x_cordinate), 2);
	double y_diff=Math.pow((from.y_cordinate-to.y_cordinate), 2);
	double result=Math.sqrt(x_diff+y_diff);
	return result;
}
//separate up and down population according to evaluation
public static ArrayList<Node> EvaluatePopulationByLine(ArrayList<Node> total_population){
	ArrayList<Node> up_pop=new ArrayList<Node>();
	ArrayList<Node> down_pop=new ArrayList<Node>();
	Hotel start_tour=Hotel.getStartDepot();
	Hotel end_tour=Hotel.getFinalDepot();
	LineFormula edging=new LineFormula(start_tour,end_tour);
	for(Node runner : total_population){
		double y_of_line=edging.ReturnYcordinate(runner);
		if(y_of_line>runner.y_cordinate){
			down_pop.add(runner);
		}else{
			up_pop.add(runner);
		}
	}
	double ev_up=EvaluatePopulation(up_pop);
	double ev_down=EvaluatePopulation(down_pop);
	if(ev_up>ev_down){
		return up_pop;
	}else{
		return down_pop;
	}
}
public static ArrayList<Hotel> SeparateHotelFromTotal(ArrayList<Node> subset_of_total){
	ArrayList<Hotel> hotels_only=new ArrayList<Hotel>();
	for(Node runner:subset_of_total){
		if(runner instanceof Hotel){
			hotels_only.add((Hotel)runner);
		}
	}
	return hotels_only;
}
public static ArrayList<POI> SeparatePOIFromTotal(ArrayList<Node> subset_of_total){
	ArrayList<POI> poi_only=new ArrayList<POI>();
	for(Node runner:subset_of_total){
		if(runner instanceof POI){
			poi_only.add((POI)runner);
		}
	}
	return poi_only;
}
public static double EvaluatePopulation(ArrayList<Node> ev_pop){
		final double h_weight=0.35;
		final double p_weight=0.35;
		final double score_weight=0.3;
		int total_score=0;
		int total_poi=0;
		int total_hotels=0;
		for(Node runner:ev_pop){
			if(runner instanceof Hotel){
				total_hotels++;
			}else{
				total_poi++;
				total_score=total_score+((POI)runner).getScore();
			}
		}
		double total=h_weight*total_hotels+p_weight*total_poi+score_weight*total_score;
		return total;
}


/**
	This method calculates the time average that will be consumed if a next visit
	was randomly chosen.In other words how much time in AVERAGE will the vehicle travel 
	if any choice is made.	Notice that only POI are taken into account.
	@param Node e the node to compute time average for
	@return time average as decimal
 */
public static double TimeAverageHotelPOI(Node e){
	ArrayList<Node> pop=Node.getTotal_population();
	double total_distance=0;
	double total_neighbours=pop.size();
	for(Node runner : pop){
		if(runner!=e){
			if(runner instanceof POI){
				total_distance=total_distance+((POI)runner).getScore();
			}
		}
	}
	//return average
	return total_distance/total_neighbours;
}
/**
This method calculates the score average that will be gained  if a next visit
was randomly chosen.In other words how much score is collected  in AVERAGE.
Notice that only POI are taken into account.
@param Node e the node to compute time average for
@return score average as integer
*/
public static int ScoreAverage(Node e){
	ArrayList<Node> pop=Node.getTotal_population();//get all population complete graph
	pop.remove(e);//remove object that is examined
	int total_score=0;
	int total_neighbours=pop.size();//get number of neighbour nodes
	//for each POI sum up score 
	for(Node runner:pop){
		if(runner!=e){
			if(e instanceof POI){
				total_score=total_score+((POI) runner).getScore();
			}
		}
	}
	//return average
	return total_score/total_neighbours;
}
private static class NodeWithScore implements Comparable<NodeWithScore> {
		
		public final POI node;
		
		public final int score;

		
		
		public NodeWithScore(POI node, int score) {
			this.node = node;
			this.score = score;
		}
		
		
		public int compareTo(NodeWithScore other) {
			if (score < other.score)
				return 1;
			else if (score > other.score)
				return -1;
			else
				return 0;
		}
		
	}
private static class LineFormula {
	
	private final Node  start;
	private final Node end;
	private final double x_mid;
	private final double y_mid;
	
	
	public LineFormula(Node from,Node to){
		this.start=from;
		this.end=to;
		
		double div=2;
		this.x_mid=(from.x_cordinate+to.x_cordinate)/div;
		this.y_mid=(from.y_cordinate+to.y_cordinate)/div;

	}
	
	public double DistanceFromLine(Node spot){
	
			    double normalLength = Math.sqrt((start.x_cordinate-end.x_cordinate)*(start.x_cordinate-end.x_cordinate)+(start.y_cordinate-end.y_cordinate)*(start.y_cordinate-end.y_cordinate));
			    return Math.abs((spot.x_cordinate-end.x_cordinate)*(start.y_cordinate-end.y_cordinate)-(spot.y_cordinate-end.y_cordinate)*(start.x_cordinate-end.x_cordinate))/normalLength;
			  
	}
	
	//update y cordinate to separate population
	public double ReturnYcordinate(Node spot){
		double y_of_line=0;
		double x_cordinate=spot.x_cordinate;
		double coef=(end.y_cordinate-start.y_cordinate)/(end.x_cordinate-start.x_cordinate);
		y_of_line=coef*(x_cordinate-start.x_cordinate)+start.y_cordinate;
		
		return y_of_line;
	}
}

}
