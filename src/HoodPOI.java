//THIS CLASS IS NOT USED !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.TreeMap;


public class HoodPOI implements Comparable<HoodPOI> {
	
	private static TreeMap<HoodPOI,POI> node_hoodid;
	final static double divisioner=2;
	static int share=0;
	public PriorityQueue<NodeWithDist> hood;
	private int score_hood;
	private POI origin_first;
	private POI origin_second;
	public Node center_hood;
	private int id=0;
	
	
	//Constructor for hood of size 2
	public HoodPOI(POI first,POI second){
		this.origin_first=first;
		this.origin_second=second;
		this.score_hood=0;
		double c_x=(first.x_cordinate+second.x_cordinate)/divisioner;
		double c_y=(first.y_cordinate+second.y_cordinate)/divisioner;
		this.center_hood=new Node(c_x,c_y);
		double dist_f_c=Computation.EuclideianDistance(first,center_hood);
		double dist_s_c=Computation.EuclideianDistance(second,center_hood);
		this.hood=new PriorityQueue<NodeWithDist>();
		this.hood.add(new NodeWithDist(first,dist_f_c,first.getScore()));
		this.hood.add(new NodeWithDist(second,dist_f_c,second.getScore()));
		share++;
		this.id=share;
		node_hoodid=new TreeMap<HoodPOI,POI>();
		node_hoodid.put(this,origin_first);
		node_hoodid.put(this,origin_second);
		
	}
	public int getHoodSize(){
		return this.hood.size();
		
	}
	public int calcHoodScore(){
		int total_score=0;
		for(NodeWithDist hood_partic : this.hood){
			POI x=(POI)hood_partic.node;
			total_score=total_score+x.getScore();
		}
		
		return total_score;
	}
	public void AssignCenterHood(){
		if(hood.size()==2){
			double div=2;
			
		}
	}
	//A hood is optimal for another hood when both origins have as second nearest node one of the other hood's origins
	public  boolean HoodAgainstHood(HoodPOI other){
		boolean isOptimal=false;
		//get origins
		POI first_origin=this.origin_first;
		POI sec_origin=this.origin_second;
		//get second nearest for each origin, excluding origin already in hood
		POI sec_of_first=first_origin.getOriginalNearestPOI(sec_origin);
		POI sec_of_sec=sec_origin.getOriginalNearestPOI(first_origin);
		if(sec_of_first.node_id==sec_of_sec.node_id){//same node check
			//merge two hoods
			for(NodeWithDist runner:other.hood){
				this.hood.add(runner);
			}
			isOptimal=true;
		}
		return isOptimal;
	}
	public static HoodPOI ReturnHoodForPOI(POI exists){
		HoodPOI return_hood=null;
		for(Entry<HoodPOI, POI> entry : node_hoodid.entrySet()) {
			POI value = entry.getValue();
			System.out.println(value.node_id);
			
			if(value.node_id==exists.node_id){
				return_hood=entry.getKey();
				
			} 
		}
		return return_hood;
	}
	public void addHoodtoHood(HoodPOI to_add){
		
		for(NodeWithDist run_add : to_add.hood){
			this.hood.add(run_add);
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//SETTERS AND GETTERS
	public PriorityQueue<NodeWithDist> getHood() {
		return hood;
	}
	public void setHood(PriorityQueue<NodeWithDist> hood) {
		this.hood = hood;
	}
	public int getScore_hood() {
		return score_hood;
	}
	public void setScore_hood(int score_hood) {
		this.score_hood = score_hood;
	}
	public POI getOrigin_first() {
		return origin_first;
	}
	public void setOrigin_first(POI origin_first) {
		this.origin_first = origin_first;
	}
	public POI getOrigin_second() {
		return origin_second;
	}
	public void setOrigin_second(POI origin_second) {
		this.origin_second = origin_second;
	}
	@Override
	public int compareTo(HoodPOI other) {
		//check distances first
		if (id < other.id)
			return -1;
		else if (id > other.id)
			return 1;
		else
			return 0;
	}

	
	//SETTERS AND GETTERS
}
