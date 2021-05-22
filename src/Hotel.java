import java.util.ArrayList;



public class Hotel extends Node{
	//class fields
	static boolean test=Initializer.isTestmode();
	static boolean overalltest=Initializer.isOverall_test();
	private static int number_extra_hotels;
	private static ArrayList<Hotel> hotel_population=new ArrayList<Hotel>();
	
	private int score;
	
	public Hotel(double x,double y,int id){
		super(x,y,id);
		this.score=0;
		hotel_population.add(this);
		
	}

	
	
	//setters and getters
	public static int getNumber_extra() {
		return number_extra_hotels;
	}

	public static void setNumber_extra(int number_extra) {
		Hotel.number_extra_hotels = number_extra;
	}

	public static ArrayList<Hotel> getHotel_population() {
		return hotel_population;
	}

	public static void setHotel_population(ArrayList<Hotel> hotel_population) {
		Hotel.hotel_population = hotel_population;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}
	public static Hotel getStartDepot(){
		Hotel initial=null;
		for(Hotel e: hotel_population){
			if(e.node_id==0){
				initial=e;
			}
		}
		return initial;
		
	}

	public static Hotel getFinalDepot(){
		Hotel last_depot=null;
		for(Hotel c:hotel_population){
			if(c.node_id==1){
				last_depot=c; 
			}
		}
		return last_depot;
	}
	
	public static Hotel getHotelById(int id){
		Hotel to_return=null;
		for(Hotel runner : hotel_population){
			if(runner.node_id==id){
				to_return= runner;
				break;
			}
		}
		return to_return;
	}
	
	
	//end setters and getters
	
}
