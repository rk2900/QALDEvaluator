import java.util.LinkedList;

public class Question {
	//public enum answerType {resource, date, number, string, bool, list};
	public int id;
	public boolean onlydbo;
	public boolean aggregation;
	public boolean hybrid;
	public String answerType;
	public LinkedList<String> keywords;
	
	public String question;
	public LinkedList<String> answers;
	
	public String query;
	
	public Question(int id, boolean onlydbo, boolean aggregation, boolean hybrid, String answerType,
			LinkedList<String> keywords, String question, LinkedList<String> answers, String query) {
		this.id = id;
		this.onlydbo = onlydbo;
		this.aggregation = aggregation;
		this.hybrid = hybrid;
		this.answerType = answerType;
		this.keywords = keywords;
		this.question = question;
		this.answers = answers;
		this.query = query;
	}
	
	
	public void print() {
		System.out.println("-------------------------");
		System.out.println("ID: "+id);
		System.out.println("Hybrid: "+hybrid);
		System.out.println("OnlyDBO: "+onlydbo);
		System.out.println("Aggregation: "+aggregation);
		System.out.println("Answer Type: "+answerType);
		System.out.print("Keyword: ");
		for(String key: keywords) {
			System.out.print(key+"\t");
		}
		System.out.println();
		System.out.println("Question: "+question);
		System.out.println("Query: ");
		System.out.println(query);
		System.out.print("Answers: ");
		for(String a: answers) {
			System.out.print(a+"\t");
		}
		System.out.println();
	}
	
}
