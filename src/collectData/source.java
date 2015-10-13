package collectData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class source {
	
	Document doc = null;
	String add1 = "http://api.dbpia.co.kr/v1/search/search.xml?key=8cdcafebb30f172422d40f954d1cd40d&target=se_adv&searchall=";
	int number = 0;
	int pageNumber = 1;
	int categoryNum = 1;
	int authorURL = 0;
	
	public source() throws IOException, ParserConfigurationException,
			SAXException {

		getData();
		System.out.println("총 갯수 : " + number + " 카테고리 : " + (categoryNum - 1)
				+ " 페이지수 : " + (pageNumber - 1));
		System.out.println("url수 : " + authorURL);
	}

	// 데이터 수집하기!!!
	public void getData() throws IOException, ParserConfigurationException,
			SAXException {

		Scanner sc = new Scanner(System.in);
		String keyword = sc.nextLine();

		this.add1 = add1 + keyword;

		System.out.println(add1);
		URL url = new URL(add1);
		InputStream in = url.openStream();
		BufferedReader rd = new BufferedReader(new InputStreamReader(in));
		String line;
		StringBuffer response = new StringBuffer();
		while ((line = rd.readLine()) != null) {
			String temp = line;
			response.append(temp);
			 System.out.println(temp);
			response.append('\r');
		}
		rd.close();
		getDocument(in, url);
		pageNumber(doc);
		for (pageNumber = 1; pageNumber < pageNumber(doc) + 1; pageNumber++) {
			for (categoryNum = 1; categoryNum < 10; categoryNum++) {
				String url_page = add1 + "&pagenumber=" + pageNumber
						+ "&category=" + categoryNum;
				url = new URL(url_page);
				getDocument(in, url);
				String category = ""+categoryNum;
				getItemData(doc, keyword,category);
				// System.out.println("페이지 수 : " + pageNumber);

			}

		}

	}

	// item별로 데이터 가지고오기
	public void getDocument(InputStream str, URL url)
			throws ParserConfigurationException, IOException, SAXException {

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();

		InputStream stream = str;
		stream = url.openStream();

		this.doc = db.parse(stream);

		this.doc.getDocumentElement().normalize();

	}

	// item으로 나눈뒤 title불러오기
	public void getItemData(Document doc, String keyword,String category) throws IOException {

		NodeList itemNodeList = doc.getElementsByTagName("item");
		WritePaper wp = new WritePaper();
		System.out.println("item노드 수 : " + itemNodeList.getLength());// 갯수

		/*
		 * if(itemNodeList.getLength()==0){
		 * 
		 * System.out.println("정지");
		 * System.out.println("총 갯수 : "+number+" 카테고리 : "
		 * +categoryNum+" 페이지수 : "+pageNumber); System.exit(0); }
		 */
		
		// 타이틀!!
		for (int i = 0; i < itemNodeList.getLength(); i++) {
			paperInfo paper = new paperInfo(keyword);// 논문 아이템 객체
			
			Node itemNode = itemNodeList.item(i);
			
			if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
				Element itemElement = (Element) itemNode;

				// NodeList authors =
				// itemElement.getElementsByTagName("author");
				// System.out.println("authors 갯수 : "+authors.getLength());
				// ======
				getTitle(itemElement, paper);
				getAuthors(itemElement, paper);
				getPublisher(itemElement, paper);
				getIssue(itemElement, paper);
				// getfree_yn(itemElement,paper);
				// getPreview(itemElement,paper);
				getlinkURL(itemElement, paper);
				paper.categoryNum = category;
				// /기서 디비 paparInfo를 넣어주면됨!!!
				String papers = paper.toString();
				System.out.println("텍스트에 들어가게될 논문 정보 : " + papers);// +paper.toString()
				try{
					wp.openWriter(papers);	
				}catch(NullPointerException npe){
					
				}
				
			}

			// DB에 삽입하기!! Connect DB!!!
			// 상위에 connectMYSQL(paper);
			System.out.println("논문 저자 숫자1 : " + paper.author.size());

		}// for문이 끝나는곳!!
			wp.closeWriter();
	}

	// 요약정보
	public int pageNumber(Document doc) {
		NodeList itemNodeList = doc.getElementsByTagName("paramdata");
		Node itemNode = itemNodeList.item(0);
		Element itemElement = (Element) itemNode;
		NodeList pagecount = itemElement.getElementsByTagName("pagecount");
		Element pageElement = (Element) pagecount.item(0);
		NodeList childElementNodeList = pageElement.getChildNodes();
		System.out.println("pageTotalNumber : " + childElementNodeList.item(0));
		String ss = childElementNodeList.item(0).toString();

		int pageTotalNumber = Integer.parseInt(extractValue(ss));
		return pageTotalNumber;// 페이지 숫자!!
	}

	// 제목
	public void getTitle(Element itemElement, paperInfo paper) {

		String title = null;
		NodeList titlelist = itemElement.getElementsByTagName("title");
		Element titleElement = (Element) titlelist.item(0);
		NodeList childElementNodeList = titleElement.getChildNodes();
		title = extractValue(childElementNodeList.item(0).toString());
		String a = null;
		if (title.indexOf("<span") == -1) {
			// System.out.println("span 없음");
			paper.title = title;

		} else {
			// System.out.println("span 있음");
			// System.out.println("title1 : "+title);
			paper.title = extractTitle(title);
			// System.out.println("title1 : "+paper.title);
		}

		// System.out.println("title2 : " + paper.title);

	}

	// Authors안의 내용!!
	public void getAuthors(Element itemElement, paperInfo paper) {
		paper.author = new ArrayList<Author>();
		NodeList title = itemElement.getElementsByTagName("authors");
		// System.out.println("authors size : " + title.getLength());
		if (title.getLength() != 0) {
			Element authorsElement = (Element) title.item(0);
			// System.out.println("authorsElement : " +
			// authorsElement.hasChildNodes());
			// System.out.println("authorsElement : "+authorsElement.hasChildNodes());
			if (authorsElement.hasChildNodes() == true) {

				NodeList authorInfo = authorsElement
						.getElementsByTagName("author");
				// System.out.println("authors size : " +
				// authorInfo.getLength());//
				// 저자
				// 숫자
				// 불러오기
				for (int i = 0; i < authorInfo.getLength(); i++) {
					String order = null;
					String name = null;
					String url = null;
					Author author = new Author();
					Element authorElement = (Element) authorInfo.item(i);
					// System.out.println("authorElement : "+authorElement.hasChildNodes());
					NodeList authorInfo2 = authorsElement
							.getElementsByTagName("order");
					Element authorElement2 = (Element) authorInfo2.item(i);
					NodeList authorInfo3 = authorsElement
							.getElementsByTagName("name");
					Element authorElement3 = (Element) authorInfo3.item(i);

					order = extractValue(authorElement2.getChildNodes().item(0)
							.toString());
					// System.out.println("order : " + order);// 순서
					name = extractValue(authorElement3.getChildNodes().item(0)
							.toString());
					try {
						NodeList authorInfo1 = authorsElement
								.getElementsByTagName("url");
						Element authorElement1 = (Element) authorInfo1.item(i);
						url = extractValue(authorElement1.getChildNodes()
								.item(0).toString());
						// System.out.println("autherUrl : " + url);// 순서
					} catch (NullPointerException np) {

						// np.printStackTrace();

					}

					if (name.indexOf("<span") == -1) {
						// System.out.println("span 없음");
						author.name = name;
						// System.out.println("name2 : " + author.name);// 이름
					} else {
						// System.out.println("span 있음");
						// System.out.println("name1"+name);
						author.name = extractTitle(name);
						// System.out.println("name1 : "+author.name);
					}

					author.order = order;

					if (url != null) {

						author.url = url;
						authorURL++;

					} else {
						author.url = null;
					}

					paper.author.add(author);

				}
				// System.out.println("논문 저자 숫자 : "+paper.author.size());
			}

		}

	}

	// 발행기관 찾기!!
	public void getPublisher(Element itemElement, paperInfo paper) {

		String url = null;// 주소이름
		String name = null;// 이름
		NodeList publisher = itemElement.getElementsByTagName("publisher");
		// System.out.println("authors size : " + title.getLength());
		Element publisherElement = (Element) publisher.item(0);
		NodeList puble_url_list = publisherElement.getElementsByTagName("url");
		Element puble_url_ele = (Element) puble_url_list.item(0);
		NodeList puble_Name_list = publisherElement
				.getElementsByTagName("name");
		Element puble_Name_ele = (Element) puble_Name_list.item(0);
		url = extractValue(puble_url_ele.getChildNodes().item(0).toString());
		name = extractValue(puble_Name_ele.getChildNodes().item(0).toString());
		// System.out.println("publisher_url : " + url);// 이름
		// System.out.println("publisher_name : " + name);// 이름
		paper.publisher_url = url;
		paper.publisher_name = name;

	}

	public void getIssue(Element itemElement, paperInfo paper) {
		String name = null;
		String number = null;
		String date = null;

		NodeList issueList = itemElement.getElementsByTagName("issue");
		// System.out.println("authors size : " + title.getLength());
		Element issueElement = (Element) issueList.item(0);
		NodeList issue_name_list = issueElement.getElementsByTagName("name");
		Element issue_name_ele = (Element) issue_name_list.item(0);
		NodeList issue_Num_list = issueElement.getElementsByTagName("num");
		Element issue_Num_ele = (Element) issue_Num_list.item(0);
		NodeList issue_date_list = issueElement.getElementsByTagName("yymm");
		Element issue_date_ele = (Element) issue_date_list.item(0);
		try {
			name = extractValue(issue_name_ele.getChildNodes().item(0)
					.toString());
		} catch (NullPointerException npe) {
			name = null;
		}

		// System.out.println("Issue_name : " + name);
		try {
			number = extractValue(issue_Num_ele.getChildNodes().item(0)
					.toString());
		} catch (NullPointerException npe) {
			number = null;
		}

		// System.out.println("Issue_number : " + number);
		date = extractValue(issue_date_ele.getChildNodes().item(0).toString());
		// System.out.println("Issue_date : " + date);
		paper.Issue_name = name;
		paper.Issue_number = number;
		paper.Issue_date = date;

	}

	// 무료 공개 여부
	/*
	 * public void getfree_yn(Element itemElement, paperInfo paper) { String
	 * free = null; NodeList free_yn_List =
	 * itemElement.getElementsByTagName("free_yn"); Element free_yn_ele =
	 * (Element) free_yn_List.item(0); free =
	 * extractValue(free_yn_ele.getChildNodes().item(0).toString());
	 * System.out.println("무료공개 여부 : " + free); paper.free_yn=free;
	 * 
	 * }
	 */
	/*
	 * // 프리퓨 제공 여부 public void getPreview(Element itemElement, paperInfo paper)
	 * { String preview_url = null; String preview_yn = null;
	 * 
	 * NodeList preview_List = itemElement.getElementsByTagName("preview_yn");
	 * System.out.println("preview_ele childNodes: " +
	 * preview_List.getLength());
	 * 
	 * //
	 * System.out.println("preview_ele childNodes: "+preview_ele.hasChildNodes
	 * ()); if (preview_List.getLength() == 1) { Element preview_ele = (Element)
	 * preview_List.item(0); preview_yn =
	 * extractValue(preview_ele.getChildNodes().item(0) .toString());
	 * System.out.println("preview_YN : " + preview_yn); if
	 * (preview_yn.equals("Y")) { preview_List =
	 * itemElement.getElementsByTagName("preview"); preview_ele = (Element)
	 * preview_List.item(0); preview_url =
	 * extractValue(preview_ele.getChildNodes().item(0) .toString());
	 * System.out.println("preview_URL : " + preview_url);
	 * 
	 * } else {
	 * 
	 * preview_url = null; System.out.println("preview_URL : " + preview_url); }
	 * } paper.preview_url = preview_url; paper.preview_yn=preview_yn;
	 * 
	 * }
	 */
	public void getlinkURL(Element itemElement, paperInfo paper) {
		String linkurl = null;
		NodeList link_List = itemElement.getElementsByTagName("link_url");
		Element link_ele = (Element) link_List.item(0);
		linkurl = extractValue(link_ele.getChildNodes().item(0).toString());
		// System.out.println("링크 URL : " + linkurl);
		paper.linkURL = linkurl;
	}

	// xml->데이터 수정!!
	public String extractValue(String st) {
		String str = st;
		str = str.substring(8, str.length() - 1).trim();
		return str;
	}

	public String extractTitle(String title) {
		String a;
		int start1, end1;
		int start2, end2;
		start1 = title.indexOf("<");
		end1 = title.indexOf(">");
		start2 = 0;
		end2 = 0;
		for (int i = title.length(); i > 8; i--) {

			if (title.substring(i - 7, i).equals("</span>")) {
				// System.out.println((i - 7) + "," + i);
				// System.out.println(title.substring(i - 7, i));
				start2 = i - 7;
				end2 = i;
			}

		}
		a = title.substring(0, start1);
		a = a + title.substring(end1 + 1, start2);
		a = a + title.substring(end2, title.length());

		return a;
	}

}
