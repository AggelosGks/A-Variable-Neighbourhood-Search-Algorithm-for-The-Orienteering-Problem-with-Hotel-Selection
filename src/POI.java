import java.util.ArrayList;
import java.util.TreeMap;


public class POI extends Node{
	//class fields
	static boolean test=Initializer.isTestmode();
	static boolean overalltest=Initializer.isOverall_test();
	
	private static ArrayList<POI> poi_population=new ArrayList<POI>();
	
	//instance fields
	private int score;
	
	
	public POI(double x,double y,int id,int score){
		super(x,y,id);
		
		this.score=score;
		poi_population.add(this);
	}
	public POI(){
		super();
	}
	//getters and setters

	public static int getNumber_poi() {
		return poi_population.size();
	}

	

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public static ArrayList<POI> getPoi_population() {
		
		return poi_population;
	}

	public static void setPoi_population(ArrayList<POI> poi_population) {
		POI.poi_population=poi_population;
	}
	
	public Hotel getNearestHotel(ArrayList<Hotel> hot_pop){
		
		TreeMap< Double,Hotel> id_dist=new TreeMap<Double,Hotel>();
		for(Hotel runner:hot_pop){
			double distance=Computation.EuclideianDistance(this, runner);
			id_dist.put(distance, runner);
		}
		return id_dist.get(id_dist.firstKey());
		
	}
	//end getters and setters
	
	
}
