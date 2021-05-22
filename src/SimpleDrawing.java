import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class SimpleDrawing extends JFrame {
/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
//indicates the width of each line representing x and y axis
final static int width_line=2;
final static int stable_cordinate=0;
//Start_x and Start_y variables indicate the start of x and y axis to map nodes
final static int start_x=20;
final static int start_y=35;
private final Tour tour_visualize;
private final ArrayList<HoodPOI> pairs;
private String file_name;
private final ArrayList<Node> population;
//saves the actual distance in pixes between to numbers (e.g the distance between (0,1) and (0,2) is 30 pixels on screen)
final static int range_multi_y=30;
final static int range_multi_x=35;
//radius for POI and Hotels
final static int hotel_radius=30;
final static int poi_radius=20;

//constructor
public SimpleDrawing(Tour tour_for_mapping,String namefile,ArrayList<Node> pop) {
	
	this.pairs=null;
	this.population=pop;
	this.file_name=namefile;
	this.tour_visualize=tour_for_mapping;
    setSize(new Dimension(800, 800));
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setVisible(true);
}
public SimpleDrawing(Tour tour_for_mapping,ArrayList<Node> pop) {
	this.pairs=null;
	this.population=pop;
	this.file_name="";
	this.tour_visualize=tour_for_mapping;
    setSize(new Dimension(500, 500));
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setUndecorated(true);
    setVisible(true);
}
public SimpleDrawing(ArrayList<Node> pop) {
	this.pairs=null;
	this.file_name="";
	this.population=pop;
	this.tour_visualize=null;
    setSize(new Dimension(500, 500));
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setVisible(true);
}
public SimpleDrawing(ArrayList<Node> pop,ArrayList<HoodPOI> pairs) {
	this.pairs=pairs;
	this.file_name="";
	this.population=pop;
	this.tour_visualize=null;
    setSize(new Dimension(500, 500));
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setVisible(true);
}

public void paint(Graphics g) {
	 Graphics2D gg = (Graphics2D) g;
	 gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	 TreeMap<Integer,Double> constraints=(TreeMap<Integer, Double>) Trip.getTripids_TimeConstraints().clone();
    g.setColor(Color.BLACK);
    g.fillRect(stable_cordinate, start_y, 2000, width_line);
    g.fillRect(start_x, stable_cordinate, width_line, 1500);
    g.setColor(Color.blue);
   TreeMap<Integer,Double> as=new TreeMap<Integer,Double>();
   for(Node e:this.population){
	   as.put(Integer.valueOf(e.node_id),Computation.TimeAverageHotelPOI(e));
   }
    for(Node e:this.population){
       if(e instanceof Hotel){
    	
    	   //design circle with double coordinates 
    	   Ellipse2D.Double shape = new Ellipse2D.Double( start_x+range_multi_x*e.x_cordinate, start_y+range_multi_y*e.y_cordinate, hotel_radius, hotel_radius);
    	   gg.setColor(Color.BLUE);
    	   //map hotel
    	   gg.fill(shape);
    	   //map circle with given center as parameter to map possible radius of travel
    	   //drawCircle( g,start_x+range_multi*(int)e.x_cordinate, start_y+range_multi*(int)e.y_cordinate,19*as.get(e.node_id).intValue());
    	   //g.setColor(Color.BLACK);
       }
       else{
    	 
    	  Ellipse2D.Double shape = new Ellipse2D.Double( start_x+range_multi_x*e.x_cordinate, start_y+range_multi_y*e.y_cordinate, poi_radius, poi_radius);
    	
   		   gg.setColor(Color.green);
   		   if(this.pairs!=null){
   			   for(HoodPOI hood:this.pairs){
   				NodeWithDist temp=new NodeWithDist(e);
   				if(hood.getHoodSize()==3){
   					for(NodeWithDist run: hood.getHood()){
   					  if(run.node.node_id==e.node_id){
   						 gg.setColor(Color.RED);
   					  }
   				  }
   				}else{
   					for(NodeWithDist run: hood.getHood()){
     					  if(run.node.node_id==e.node_id){
     						  gg.setColor(Color.YELLOW);
     					  }
     				  }
   				}
   				
   			   }
   			 }
   		   
    	   //map POI
    	   gg.fill(shape);

       }
    }
    g.setColor(Color.BLACK);
    for(Node e:this.population){
    	if(e instanceof Hotel){
    		  g.setFont(new Font("TimesRoman", Font.PLAIN, 30)); 
       	   g.drawString(Integer.toString(e.node_id),start_x+hotel_radius+range_multi_x*(int)e.x_cordinate, start_y+hotel_radius+range_multi_y*(int)e.y_cordinate);//+" x: "+Double.toString(e.x_cordinate)+" y: "+Double.toString(e.y_cordinate), start_x+30*(int)e.x_cordinate, start_y+30*(int)e.y_cordinate);
    	}else{
    		 g.setFont(new Font("TimesRoman", Font.PLAIN, 20)); 
         	  g.drawString(Integer.toString(e.node_id),start_x+poi_radius+range_multi_x*(int)e.x_cordinate, start_y+poi_radius+range_multi_y*(int)e.y_cordinate);
    	}
    }
        
   if(this.tour_visualize!=null){
	   ArrayList<Trip> trips_of_tour=this.tour_visualize.getTrips();
		for(Trip trip_runner: trips_of_tour){
			if(trip_runner.getId()% 2==0){//even trip
				gg.setColor(Color.BLACK);
			}else{//odd trip
				gg.setColor(Color.RED);
			}
			ArrayList<Node> runner_permutation= trip_runner.getPermutation(); 
			for(int j=0; j<runner_permutation.size()-1; j++){
				Node start_point=runner_permutation.get(j);
				Node finish_point=runner_permutation.get(j+1);//and next one
				Shape edge = new Line2D.Double(start_x+poi_radius+range_multi_x*start_point.x_cordinate,start_y+poi_radius+range_multi_y*start_point.y_cordinate,start_x+poi_radius+range_multi_x*finish_point.x_cordinate, start_y+poi_radius+range_multi_y*finish_point.y_cordinate);
				gg.draw(edge);
			}
		}
   }
   Hotel start=null;
   Hotel finish=null;
   double first_limit=constraints.firstEntry().getValue();
   double total_length=Tour.gettour_length_budget();
   
   for(Node runner: this.population){
	   if(runner instanceof Hotel){
		   if(runner.node_id==0){
			   start=(Hotel)runner;
		   }else if(runner.node_id==1){
			   finish=(Hotel)runner;
		   }
	   }
   }
   if(!(this.file_name.equals(""))){
	   Container c = this.getContentPane();
	   BufferedImage im = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
	   c.paint(im.getGraphics());
	   try {
		ImageIO.write(im, "PNG", new File(this.file_name+".png"));
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
   }

   
   
   

  
	
}
public  static void drawCircle(Graphics cg, int xCenter, int yCenter, int r) {
	
	cg.setColor(Color.ORANGE);
	cg.drawOval(xCenter-r, yCenter-r, 2*r, 2*r);
	return;
}

}