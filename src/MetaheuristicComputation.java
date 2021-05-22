import java.security.KeyStore.Entry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeMap;


public class MetaheuristicComputation {

	static boolean test=Initializer.isTestmode();
	static boolean overalltest=Initializer.isOverall_test();


		//Length Orienteed
	/**
	 @EvaluatedSeparatedLengthOrienteedBacktrack
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
	public static Tour EvaluatedSeparatedLengthOrienteedBacktrack(){
		Tour grlength_tour=new Tour();//initaliaze current tour 
		ArrayList<Node> total_population=(ArrayList<Node>) Node.getTotal_population().clone();
		total_population=EvaluatePopulationByLine(total_population);
		ArrayList<POI> poi_pop=SeparatePOIFromTotal(total_population);
		ArrayList<Hotel> hotel_population=	SeparateHotelFromTotal(total_population);
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
		grlength_tour.addTriptoTour(constructTripEvSeLengthOriented(start_hotel,hotel_population,tour_time_budget,grlength_tour.getNumberOfTrips()));
		}
		grlength_tour.ReverseTourHotelsOnly();
		AddPOISLengthOrienteed(grlength_tour,poi_pop);  
		//AddPOISLineOrienteed(grlength_tour,poi_pop);
		grlength_tour.calcTourScore();//calculate score once after Tour is constructed
		grlength_tour.calcTourLength();
		System.out.println(grlength_tour.toString());
		return grlength_tour;
	}
	/**
	 @constructTripEvSeLengthOriented
	This method constructs a feasible trip,by continiusly picking a node from graph, notice that it is not possible to now when the trip will end so boolean variable 
	control, checks the termination of constructive process.Specifially if next node to be added to trip is instance of Hotel then , score and length is calculated and trip is finished.
	The function responsible for the selecting of nodes is guided in each iteration according to the length available of the trip.
	@param start_hotel : Represents the starting hotel of the trip
	@param hotel_population : Represents the active (non visited) Hotels in each trip construct iteration
	@param length_available : Represents the available length limit remained in each iteration
	@return current_trip : The current trip constructed 
*/
	public static Trip constructTripEvSeLengthOriented(Hotel start_hotel,ArrayList<Hotel> hotel_population,double tour_limit,int trips_constructed){
		Trip current_trip=new Trip(start_hotel,(Trip.getNumber_of_trips()-1)-trips_constructed);//current trip represents the trip to returned one way or another
		boolean control=true;//variable to control finish of trip if length trip must be less than length trip limit
		while(current_trip.CalcTripLength()<=current_trip.getLength_limit()&&control){
			double length_limit_trip=current_trip.getLength_limit();
			int trip_id=current_trip.getId();
			Node next_node=greedyHNextVisitESLengthOriented(start_hotel,length_limit_trip,hotel_population,trip_id);
			
			if(next_node instanceof Hotel){//if next_node is instance hotel then this node is the final hotel of trip so while loop terminates
				control=false;
				current_trip.setScore(current_trip.CalcTripScore());
			}
			current_trip.addNodetoPermut(next_node);
		}
		
		return current_trip;
	}
	/**
	 @greedyHNextVisitESLengthOriented
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
	public static Node greedyHNextVisitESLengthOriented(Node start_node,double length_limit_trip,ArrayList<Hotel> hotel_population,int trip_id){
			Node to_add=null;//the node to return
			if(trip_id!=0){
				System.out.println("Start node "+start_node.node_id);
				boolean h_selection=true;//variable to control hotel selection
				double cur_consumed=Trip.SumUpConstraints(trip_id)+length_limit_trip;//sum of length of trips so far constructed plus the current one to be constructed
				double total_left=Math.abs(Tour.gettour_length_budget()-cur_consumed);//compute total limit available of tour
				int next_trip_id=trip_id-1;//save in other variable to keep original value
				double next_trip_limit=Trip.getLength_limitById(next_trip_id);//compute next trip limit for forecast
				start_node.hood=start_node.GetHNeighboursUnderRadius(length_limit_trip, hotel_population, total_left);//get feasible hood of two circle cuts 
				System.out.println("Start node has hood size: "+start_node.hood.size());
				while(h_selection&&start_node.hood.size()>0){
					Hotel nearest_in_hood=start_node.getNearestHotel(start_node.hood);//get the nearest hotel of hood
				//	System.out.println("Nearest in hood: "+nearest_in_hood.node_id);
					start_node.hood.remove(nearest_in_hood);
					//forecast
					nearest_in_hood.hood=nearest_in_hood.GetHNeighboursUnderRadius(next_trip_limit, hotel_population, Math.abs(total_left-next_trip_limit));//get hood of candidate to ensure feasibility
					//System.out.println("Hood_hood size: "+nearest_in_hood.hood.size());
					if(!(nearest_in_hood.hood.isEmpty())){//if at least one hotel found then feasible
						//System.out.println("		This node has in hood:"+nearest_in_hood.node_id);
						for(Hotel run:nearest_in_hood.hood){
							//System.out.println("   				 "+run.node_id);
						}
						to_add=nearest_in_hood;
						if(!(to_add.node_id==0||to_add.node_id==1)){
							hotel_population.remove(to_add);	
						}
						h_selection=false;
					}
				}
				if(to_add==null){
					to_add=start_node;
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
	public static void AddPOISLineOrienteed(Tour current_tour,ArrayList<POI> poi_population){
		ArrayList<POI> poi_pop=new ArrayList<POI>(poi_population);
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
	public static void AddPOISLengthOrienteed(Tour current_tour,ArrayList<POI> poi_population){
		ArrayList<POI> poi_pop=new ArrayList<POI>(poi_population);
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
				while(defen_copy.size()>0&&length_available>1){
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
//Returns an arraylist of hotel population as clustered hotel with treemap data structure under trip length limits.
public static 	TreeMap<Integer,ClusteredHotel> createClusterHotelsUnderLimits(ArrayList<Node> total_population){
	TreeMap<Integer,Double> id_limits=(TreeMap<Integer, Double>) Trip.getTripids_TimeConstraints().clone();//get length limits for each trip
	ArrayList<Hotel> hotel_pop=SeparateHotelFromTotal(total_population);//saves initial hotel population
	TreeMap<Integer,ClusteredHotel> clust_pop=new TreeMap<Integer,ClusteredHotel>();//saves clustered hotel population for vns
	for(Hotel hotel_iter : hotel_pop){
		ClusteredHotel clust_hotel=new ClusteredHotel(hotel_iter);//initialize treemap for each hotel
		clust_pop.put(hotel_iter.node_id,clust_hotel);
	}
	for(int z=0; z<id_limits.size(); z++){//for every trip length limit
		double length_limit=id_limits.get(z);//get limit
		for(int i=0; i<clust_pop.size(); i++){//for every clust hotel
			ClusteredHotel clust_hotel=clust_pop.get(i);
			PriorityQueue<ClusteredVisitor> visitor_hood=new PriorityQueue<ClusteredVisitor>();//to save sortly ClusteredVisitors according to betw score
			ArrayList<Hotel> feas_visit=clust_hotel.getClusteredVisitorsUnderLimit(length_limit,hotel_pop,z);//get feasible visitor under length limit
			for(Hotel visitor : feas_visit){
				ClusteredVisitor clust_visitor=new ClusteredVisitor(visitor,clust_hotel,length_limit);//create clustered visitor instances
				visitor_hood.add(clust_visitor);//add to priortity queue
			}
			clust_hotel.addEntryRowUnderLimit(length_limit, visitor_hood);//add entire row to clustered hotel for current length limit
		}
	}
	
	return clust_pop;
}
//Return a tree map of hotels according their distance as intermediate , we calculate distance from start and end and we take the absolute difference, minium difference is bridge hotel
public static TreeMap<Double,Hotel> DetectBridgeHotelS(ArrayList<Node> total_population){
	Hotel start_tour=Hotel.getStartDepot();
	Hotel end_tour=Hotel.getFinalDepot();
	TreeMap<Double,Hotel> bridge_hotels=new TreeMap<Double,Hotel>();
	ArrayList<Hotel> h_pop=SeparateHotelFromTotal(total_population);
	h_pop.remove(start_tour);
	h_pop.remove(end_tour);
	for(Hotel h_iter : h_pop){
		double dist_to_start=EuclideianDistance(h_iter,start_tour);
		double dist_to_end=EuclideianDistance(h_iter,end_tour);
		double diff=Math.abs(dist_to_start-dist_to_end);
		bridge_hotels.put(diff,h_iter);
	}
	return bridge_hotels;
	
}

public static 	ArrayList<HoodPOI>   ClusterPOISGraph(ArrayList<Node> total_population){
		ArrayList<POI> poi_pop=SeparatePOIFromTotal(total_population);
		ArrayList<POI> hood_pop=new ArrayList<POI>();
		int matches=0;
		ArrayList<HoodPOI> total_hoods=new ArrayList<HoodPOI>();
		for(POI runner:poi_pop){//for each poi
			if(!hood_pop.contains(runner)){
				POI runner_nearest=runner.getOriginalNearestPOI();//get the nearest 
				POI run_nearest_first=runner_nearest.getOriginalNearestPOI();//of this get its nearest
				if(run_nearest_first.node_id==runner.node_id){//if the second call of nearest is the examined node
					ArrayList<POI> pair_mathc=new ArrayList<POI>();//initialize
					matches++;
					HoodPOI hood=new HoodPOI(runner,runner_nearest);
					total_hoods.add(hood);
					//add pairs-nodes to separate list
					hood_pop.add(runner);
					hood_pop.add(runner_nearest);
				}
			}
		}
		for(POI dismiss : hood_pop){
			poi_pop.remove(dismiss);//delete pairs from initial population
		}
		ArrayList<HoodPOI> forbidden=new ArrayList<HoodPOI>();
		for(HoodPOI hood_iter:total_hoods){
			if(!forbidden.contains(hood_iter)){
				POI first_origin=hood_iter.getOrigin_first();
				POI second_origin=hood_iter.getOrigin_second();
				POI second_f_origin=first_origin.getOriginalNearestPOI(second_origin);
				POI second_s_origin=second_origin.getOriginalNearestPOI(first_origin);
				if(second_f_origin.node_id==second_s_origin.node_id){//same node as second nearest
					if(hood_pop.contains(second_f_origin)){//already in another hood
						
						HoodPOI to_add=HoodPOI.ReturnHoodForPOI(second_f_origin);
						if(to_add==null){
							System.out.println("ERROR");
						}
						hood_iter.addHoodtoHood(to_add);
						forbidden.add(to_add);
					}else{//not in hood
						double dist=EuclideianDistance(second_f_origin,hood_iter.center_hood);
						//add node in hood
						hood_iter.hood.add(new NodeWithDist(second_f_origin,dist,second_f_origin.getScore()));
					}
				}
			}
		}

	
		for(HoodPOI hood : total_hoods){
			if(hood.hood.size()>0){
				System.out.println("-----------------------");
				for(NodeWithDist e:hood.hood){
					System.out.println(e.node.node_id);
				}
				System.out.println("-----------------------");
			}
			
		}
		
		return total_hoods;
}



//Apply changes to a hotel sequence implemented as discent variable neighborhood search
public static void VNSHotelSequence(Tour candidate_tour){
	TreeMap<Integer,Double> trips_constraints=Trip.getTripids_TimeConstraints();
	TreeMap<Integer,ClusteredHotel> clust_hotel_pop=MetaheuristicComputation.createClusterHotelsUnderLimits(new ArrayList<Node>(Node.getTotal_population()));
	
	ArrayList<Hotel> tabu=new ArrayList<Hotel>();//saves hotels that exist in solution, so repetition is controlled 
	ArrayList<Hotel> new_seq=new ArrayList<Hotel>();//saves the new solution
	new_seq.add(Hotel.getStartDepot());//add intial depot
	
	ArrayList<ClusteredHotel> cl_hotel_seq=ModifyHotelToClustered(candidate_tour.getHotelSequenceTour(),clust_hotel_pop);//get sequence as clustered hotels
	for(int ap_index=1; ap_index<=cl_hotel_seq.size()-1; ap_index++){
		ClusteredHotel current=cl_hotel_seq.get(ap_index);//get current in sequence
		ClusteredHotel previous=clust_hotel_pop.get(new_seq.get(ap_index-1).node_id);//returns a current hotel to a clustered hotel instance
		int trip_id=ap_index-1;
		double trip_limit=trips_constraints.get(trip_id);//for position 1 to apply a move we need time limit with id 0
		System.out.println(trip_limit);
		boolean control=true;
		while(control&&previous.limits_hotels.get(trip_limit).size()>0){
			System.out.println(previous.node.node_id);
			ClusteredVisitor opt_visit=previous.limits_hotels.get(trip_limit).remove();//for current length window get the best visitor according to betweenes
			if((!tabu.contains(opt_visit.node))){//if  not inserted in permuatation so far
				System.out.println(opt_visit.node.node_id);
				new_seq.add(opt_visit.node);
				if(!(opt_visit.node.node_id==1||opt_visit.node.node_id==0)){
					tabu.add(opt_visit.node);
				}
				control=false;
			}
		}

	}
	
	System.out.println("                                  ");
	System.out.println("-------------------------------------");
	for(Hotel e : new_seq){
		System.out.println(e.node_id);
	}
	System.out.println("-------------------------------------");
	Tour x=new Tour(new_seq);
	
	Computation.AddPOISLengthOrienteed(x);
	x.calcTourScore();
	x.calcTourLength();
	Initializer.printTour(x);
}
//Returns a clustered hotel instance equavalent to a hotel given as parameter
public static ClusteredHotel fromHotelToClustered(Hotel modify,ArrayList<ClusteredHotel> clust_pop){
	ClusteredHotel cl_modify=null;
	for(ClusteredHotel run : clust_pop){
		if(run.node.node_id==modify.node_id){
			cl_modify=run;
			break;
		}
	}
	return cl_modify;
}
//Calculates total betweenes score for a feasible solution
public int calcTotalBetweenScore(ArrayList<ClusteredHotel> sequence, TreeMap<Integer,Double> trips_cosntraints){
	int estimated_score=0;
	for(int j=0; j<sequence.size()-1; j++){
		int score=calc
	}
	
	
	return estimated_score;
	
}


//Returns the intermediate mid node between two ClusteredHotels, node is not a hotel or poi we use its coordinates from geometrical approach,
public static Node createMidIntermediate(Hotel from,Hotel to){
	double divider=2;
	double x_mid=Math.abs(from.x_cordinate+to.x_cordinate)/divider;
	double y_mid=Math.abs(from.x_cordinate+to.x_cordinate)/divider;
	Node mid=new Node(x_mid,y_mid);
	return mid;
}

//Returns the radius of circle to be drawn in mid intermediate hotel when calc betweness score
//If two hotels are close enough so  the difference from length limit and the distance is greater than the actual distance then we select the difference to be as radius of selection.
//In other words a greater than then distance is provided for search in cases that hotel are extremely near and a few pois exist.
public static double calcRadiusVNSUnderLimit(double trip_limit,double dist_hotels){
	double radius=Math.max(dist_hotels,(trip_limit-dist_hotels));
	return radius;
}



//Returns a current hotel sequence to an equivalent on clustered hotels
public static ArrayList<ClusteredHotel> ModifyHotelToClustered(ArrayList<Hotel> h_sequence,ArrayList<ClusteredHotel> cl_hotels){
	ArrayList<ClusteredHotel> sequence_clustered=new ArrayList<ClusteredHotel>();
	for(Hotel h_runner : h_sequence){
		for(ClusteredHotel cl_runner : cl_hotels){
			if(cl_runner.node.equals(h_runner)){
				sequence_clustered.add(cl_runner);
			}
		}
	}
	return sequence_clustered;
}

//Returns a current clustered hotel sequence to an equivalent on  hotels
public static ArrayList<Hotel> ModifyClusteredToHotel( ArrayList<ClusteredHotel> new_sequence){
	ArrayList<Hotel> h_sequence=new ArrayList<Hotel>();
	for(ClusteredHotel cl_runner : new_sequence){
		h_sequence.add(cl_runner.node);
	}
	return h_sequence;
}

//Swaps the given hotel as parameter to the one located in the index given also as parameter
public static void SwapHotels(ClusteredHotel swap_hotel,int index,ArrayList<ClusteredHotel> sequence){
	if(index>0&&index<sequence.size()-1){//start and finish indexes remain same repsectively
		sequence.set(index, swap_hotel);
	}
}

//Checks if a vns constructed is feasible
public static boolean isFeasibleSeq(TreeMap<Integer, Double> trips_constraints,ArrayList<ClusteredHotel> h_seq){
	boolean overall=false;
	for(int j=1; j<h_seq.size()-1; j++){
		double trip_limit=trips_constraints.get(j-1);
		System.out.println(trip_limit);
		double distance=EuclideianDistance(h_seq.get(j-1).node,h_seq.get(j).node);
		if(distance<=trip_limit){
			overall=true;//
		}else{
			overall=false;//re assign false if an edge is unfeasible
			break;//when find the first unfeasible break an return value
		}
	}
	return overall;
}

//Calculates best (optimal) score between 
public static  int calcBetwenessScore(Hotel from, Hotel target,double trip_limit){
	int betw_score=0;
	//For both sets it is impossible for a cut to exist, one poi is assigned to one and only hotel.
	ArrayList<POI> poi_pop=(ArrayList<POI>) POI.getPoi_population().clone();
	Node mid_inter=createMidIntermediate(from,target);//create mid hotel between two clustered hotels
	double radius_search=calcRadiusVNSUnderLimit(trip_limit,EuclideianDistance(from,target));//get radius to search for POIS
	for(POI poi : poi_pop){
		double dist_poi_mid=EuclideianDistance(poi,mid_inter);
		if(dist_poi_mid<=radius_search){
			betw_score=betw_score+poi.getScore();
		}
	}
	return betw_score;
	
	
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
//Utility Classes
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

private static class ClusteredHotel  {
	
	
	public final Hotel node;
	public  TreeMap<Double,PriorityQueue<ClusteredVisitor>> limits_hotels;

	//creates a cluster hotel and initializes treemap to save clustered visitor under length limit
	public ClusteredHotel(Hotel node) {
		this.node = node;
		this.limits_hotels=new TreeMap<Double,PriorityQueue<ClusteredVisitor>>();//initialize treeMap
	

	}
	public void addEntryRowUnderLimit(double limit,PriorityQueue<ClusteredVisitor> feas_visitors){
		this.limits_hotels.put(limit, new PriorityQueue<ClusteredVisitor>(feas_visitors));
		
	}

	//return a subset of hotels which getting there is feasible under trip limit, the source node is the node calling the method
	public ArrayList<Hotel> getClusteredVisitorsUnderLimit(double trip_limit,ArrayList<Hotel> h_pop,int trip_id){
		ArrayList<Hotel> for_return=new ArrayList<Hotel>();
		if(trip_id==Trip.getNumber_of_trips()-1){//if last trip visitors are examined
			double distance=EuclideianDistance(this.node,Hotel.getFinalDepot());
			if(distance<=trip_limit){

				for_return.add(Hotel.getFinalDepot());
			}
		}else{
			for(Hotel runner : h_pop){
				double distance=EuclideianDistance(this.node,runner);
				if(distance<=trip_limit){
					for_return.add(runner);
				}
			}
		}
		
		return for_return;
	}

}
private static class ClusteredVisitor implements Comparable<ClusteredVisitor> {
	
	//fields 
	public final Hotel node;
	public final int between_score;
	
	//the node is the node of the visitor, target is the hotel which the visitor is clustered to and lengthlimit the current limit
	public ClusteredVisitor(Hotel node,ClusteredHotel target,double length_limit) {
		this.node = node;
		this.between_score=MetaheuristicComputation.calcBetwenessScore(node,target.node,length_limit);
	}

	//comparator for priorityqueue 
	public int compareTo(ClusteredVisitor other) {
		if (between_score < other.between_score)
			return 1;
		else if (between_score  > other.between_score)
			return -1;
		else
			return 0;
	}

	
	
	
	
}


}


