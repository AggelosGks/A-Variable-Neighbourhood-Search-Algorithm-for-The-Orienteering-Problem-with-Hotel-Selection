import java.awt.Label;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;
import java.awt.Label;
import javax.swing.border.Border;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.formula.functions.Column;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;





public class Initializer {
	static String execution_mode="";
	static String view_mode="";
	static boolean overall_test=false;
	static boolean testmode=false;
	static boolean viewmode=false;
	static File folder=null;
	final static String length="length";
	final static String separation="separation";
	final static String line="line";
	static File[] listOfFiles=null;
	public static void main(String args[]){

		ReadArguments(args);
		//start execution mode for multiple instances
		String mode="";
		TreeMap<String,Tour> tours_names=new TreeMap<String,Tour>();
			System.out.println("What mode ?");
			Scanner input=new Scanner(System.in);
			String read=input.nextLine();
			if(read!=null){
				if(read.equals(length)){
					mode=length;
				}else if(read.equals(separation)){
					mode=separation;
				}else{
					mode=line;
				}
				
			}else{
				System.out.println("Error Try again");
			}
			System.out.println("What Set ?");
			read=input.nextLine();
			String set=read;
		for (int i = 0; i < listOfFiles.length; i++) {
			if(listOfFiles[i]!=null){
				if(listOfFiles[i].isFile()){
					String file_name=listOfFiles[i].getName();
					System.out.println(file_name+" : INSTANCE");
					ReadInstanceData(file_name);
					//algorithm mode
					if(overall_test){
						ArrayList<Node> def_copy=(ArrayList<Node>) Node.getTotal_population().clone();
						Tour  initial_tour=null;
						initial_tour=Computation.InitialSolutionLengthOrienteedBacktrack();
						printTour(initial_tour);
						tours_names.put(file_name, initial_tour);
						
					}	
					//single test mode
					else{
						ArrayList<Node> def_copy=(ArrayList<Node>) Node.getTotal_population().clone();
						Tour  initial_tour=null;
						initial_tour=Computation.InitialSolutionLengthOrienteedBacktrack();
						MetaheuristicComputation.VNSHotelSequence(initial_tour);
						printTour(initial_tour);
						EnablePanelTour(initial_tour,"",def_copy);

			
					}
					//re-set structures for next instance
					Node.setTotal_population(new ArrayList<Node>());
					Hotel.setHotel_population(new ArrayList<Hotel>());
					POI.setPoi_population(new ArrayList<POI>());
					Trip.setShare(-1);
					Trip.setNumber_of_trips(0);
					
				}else{
					System.out.println("Here");
				}
			}
		}

		//UpdateFile(tours_names,mode,set);
		
	}//end main
	public static void ReadInstanceData(String argument)
	{
		ArrayList<Double> holder=new ArrayList<Double>();
		BufferedReader br = null;
		String line = "";//(1)
		String saver="";
		try 
		{
			br = new BufferedReader(new FileReader(argument));//KSEKINAEI ANAGNWSI ARXEIOU
			while ((line = br.readLine()) != null) {// string line reads
				if (line.trim().length() > 0){
					
					String[] ary = line.split("");
					for(String c:ary){
						if(isNumerical(c)){
							saver=saver+c;
						}else{
							if(!(saver.equals(""))){
								holder.add(Double.parseDouble(saver));
							}
							saver="";
						}
					}
				}
			}
		 Instance current=new Instance(holder);
		 current.CreateInstance();
		}
	
		catch (FileNotFoundException e) 
		{
			            e.printStackTrace();
				} catch (IOException e) {
			            e.printStackTrace();
			        }
					finally 
					{
			            if (br != null) {
			                try {
			                    br.close();
			                } catch (IOException e) {
			                    e.printStackTrace();
			                }    
			            }
			        }
	}
	public static boolean isNumerical(String onechar){
		if(onechar.equals(".")){
			return true;
		}
		try{
			Integer.parseInt(onechar);
			return true;
		}catch(Exception e){
			return false;
		}
	}
	public static void ReadArguments(String[] args){
		final String overall="overall";
		final String test="test";
		final String view="viewmode";
		if(args[0]!=null&&args[1]!=null){
			folder = new File(args[0]);
			 listOfFiles = folder.listFiles();
			for (int i = 0; i < listOfFiles.length; i++) {
			      if (listOfFiles[i].isFile()) {
			        if(listOfFiles[i].getName().contains("ophs")){
			        	continue;
			        }else{
			        	listOfFiles[i]=null;
			        }
			      } else if (listOfFiles[i].isDirectory()) {
			    		listOfFiles[i]=null;
			      }
			    }
			execution_mode=args[1];
			view_mode=args[2];
			if(execution_mode.equals(overall)){
				overall_test=true;
			}else{
				testmode=true;
			}
			if(view_mode.equals(view)){
				viewmode=true;
			}
		}else{
			System.err.println("Error on given start arguments");
			System.exit(1);
		}
	}
	public static boolean isOverall_test() {
		return overall_test;
	}
	public static boolean isTestmode() {
		return testmode;
	}
	public static void EnablePanelTour(Tour tour_for_mapping,String fname,ArrayList<Node> pop)//method to open frame for printing
	{
		new SimpleDrawing(tour_for_mapping,fname,pop);

	 }
	public static void EnablePanelGraph(ArrayList<Node> pop){
		new SimpleDrawing(pop);
	}
	public static void printTour(Tour e){
		if(e.isFeasible()){
			System.out.println(e.toString());
		}
		if(e.isTourMaximum()){
			System.out.println("This tour achieves maximum score");
		}
	}
	public static void UpdateFile(TreeMap<String,Tour> tours_names,String mode,String set){
		try {
			int 	ch_column=0;//column to apply changes
			if(mode.equals(separation)){
				ch_column=2;
			}else if(mode.equals(length)){
				ch_column=1;
			}else{
				ch_column=3;
			}
			String filename="C:\\Users\\kostos\\Desktop\\NewExcelFile.xls" ;
			String filename1="C:\\Users\\kostos\\Desktop\\NewExcelFile.xls" ;
			 //Read the spreadsheet that needs to be updated
			 FileInputStream input_document = new FileInputStream(new File(filename));
			//Access the workbook
             HSSFWorkbook my_xls_workbook = new HSSFWorkbook(input_document); 
             //Access the worksheet, so that we can update / modify it.
             HSSFSheet my_worksheet = my_xls_workbook.getSheet(set); 
             int total_rows=my_worksheet.getPhysicalNumberOfRows();
             int row_counter=1;//skip headlines
          // declare a Cell object
             while(tours_names.size()>0){
            	Entry<String, Tour> entry= tours_names.pollFirstEntry();
            	String instance_name=entry.getKey();
            	Tour current=entry.getValue();
            	int curr_score=current.getTour_score();
            	  Cell cell = null; 
            	  cell = my_worksheet.getRow(row_counter).getCell(0);
            	  
            	  cell.setCellValue(instance_name);
            	  cell = my_worksheet.getRow(row_counter).getCell(ch_column);
            	  cell.setCellValue(curr_score);
            	  row_counter++;
             }
           
             // Access the cell first to update the value
            
             // Get current value and then add 5 to it 
           
             input_document.close();
             //Open FileOutputStream to write updates
             FileOutputStream output_file =new FileOutputStream(new File(filename));
             //write changes
             my_xls_workbook.write(output_file);
             //close the stream
             output_file.close();
        }catch(Exception e){
		e.printStackTrace();
	}
		
			
		
	
    
    
	
}
}
