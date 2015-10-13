package collectData;

import java.io.BufferedWriter;
import java.io.FileWriter;

public class WritePaper {
	BufferedWriter writer=null;
	
	public WritePaper(){
		
		try{
		//	BufferedWriter writer = new BufferedWriter(new FileWriter("G://test.txt"));	
			 writer = new BufferedWriter(new FileWriter("G://빅데이터2.txt",true));
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
	}
	public void openWriter(String paper){
		try{
			writer.write("\n"+paper+"\r");	
			writer.flush();
		}catch(Exception ex){
			
		}
		
		
	}

	
	public void closeWriter(){
		try{
			writer.close();
		}catch(Exception ex){
			
		}
	}
	
}

