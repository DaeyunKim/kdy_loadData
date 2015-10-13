package collectData;

import java.util.ArrayList;

public class paperInfo {

	String category=""; //카테고리
	String categoryNum ="";
	String title=""; //제목
	ArrayList<Author> author = new ArrayList<Author>();//저자정보
	
	String publisher_url=""; // 학회 정보 url
	String publisher_name=""; // 학회 이름
	String Issue_date=""; //논문 발표 날짜
	String linkURL=""; //논문 URL
	String Issue_name=""; 
	String Issue_number="";
	

	public paperInfo(String category){
		this.category=category;
	}
	
	String callAuthor(){
		String str = null;
		
		for(Author o : author){
			
			str+=o.order+"@"+o.name+"@"+o.url+"@";
			
		}			
		
		return str; 
	}
	
	
	//connectDB(){}
	
	public String toString( ){
		
		String paper_Info = null;
		
		paper_Info = category+"@"+categoryNum+"@"+title +"@"+publisher_url+"@"+publisher_name+"@"+Issue_date+"@"+linkURL+"@"+Issue_name+"@"
		+Issue_number+"@"+author.size()+"@"+callAuthor();
		
		return paper_Info;
	}
	
	
	
	
}
