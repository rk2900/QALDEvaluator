import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class GAQuery {

	/**
	 * Given one question, get all the answers returned by gAnswer system.
	 * @param question the question text
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public LinkedList<String> query(String question) throws InterruptedException {
		LinkedList<String> answers = new LinkedList<>();
		StringBuilder sb = new StringBuilder();
		sb.append("http://59.108.48.18:8080/gAnswer/ganswer2.jsp?question=");
		try {
			sb.append(URLEncoder.encode(question, "utf-8"));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		String url = sb.toString();
		
		Document doc = null;
		boolean passFlag = true;
		do {
			try {
				System.err.println("GET");
				doc=Jsoup.connect(url).get();
			} catch (IOException e) {
				Thread.sleep(10000);
				passFlag = false;
			}
		} while(!passFlag);
		Elements sumDiv = doc.getElementsByAttributeValue("class", "summary-text");
		String summary = sumDiv.text();
		
		int totalNumber = Integer.parseInt(summary.substring(0, summary.indexOf(" ")));
		System.err.println("Total number: "+totalNumber);
		if(totalNumber == 0 || totalNumber > 900) {
			return answers;
		}
		
		int pageNumber = totalNumber/30+1;
		System.err.println("Page number: "+pageNumber);

		int pageCount = 0;
		do {
			Elements listDiv = doc.getElementsByAttributeValue("id","entity_name");
			if(listDiv.isEmpty()) {
				System.err.println("Literal");
				Elements td = doc.getElementsByAttributeValue("id", "hit");
				System.out.println(td.text());
				answers.add(td.text());
			} else {
				for (Element element : listDiv) {
//					System.out.println(element.attr("href")+"\t"+element.text());
					String uri = "http://dbpedia.org/resource/"+element.text();
					answers.add(uri);
				}
			}
			System.err.println("Page "+(pageCount+1)+" done.");
			Thread.sleep(3000);
			passFlag = true;
			do {
				try {
					System.err.println("GET");
					doc=Jsoup.connect(url+"&page="+(++pageCount)).get();
				} catch (IOException e) {
					Thread.sleep(10000);
					passFlag = false;
				}
			} while(!passFlag);
		} while (pageCount<pageNumber);
		
		System.err.println("Query finished.\n");
		
		return answers;
	}
	
	public ArrayList<Question> getGenParse(ArrayList<Question> goldParse) throws IOException, InterruptedException {
		ArrayList<Question> genParse = new ArrayList<>();
		int qid = 0;
		for (Question question : goldParse) {
			System.err.println("Querying "+(++qid)+": "+question.question);
			LinkedList<String> answers = query(question.question);
			Question genQ = new Question(question.id, false, false, false, "", new LinkedList<>(), question.question, answers, "");
			genParse.add(genQ);
		}
		return genParse;
	}
	
	public static void main(String[] args) throws IOException, InterruptedException {
		// load gold XML file
		XMLParser goldParse = new XMLParser();
		goldParse.setFilePath(Const.qald_result_gold);
		goldParse.parse(true);
		
		// get generated answers from gAnswer
		GAQuery gaQuery = new GAQuery();
		ArrayList<Question> genParse = gaQuery.getGenParse(goldParse.getQuestions());
		
		// evaluation
		Evaluator evaluator = new Evaluator();
		if(Const.needsQueryForAnswer)
			evaluator.setGoldAnswer(goldParse);
		evaluator.evaluate(goldParse.getQuestions(), genParse);
	}

}
