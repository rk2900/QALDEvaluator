
public class Main {
	public static void main(String[] args) {
		// load gold XML file
		XMLParser goldParse = new XMLParser();
		goldParse.setFilePath(Const.qald_result_gold);
		goldParse.parse(true);
		
//		for (Question question : goldParse.getQuestions()) {
//			System.out.print("QID: "+question.id+"\t");
//			for (String ans : question.answers) {
//				System.out.print(ans+"\t");
//			}
//			System.out.println();
//		}
		
		/**/
		// load generated XML file
		XMLParser genParse = new XMLParser();
		genParse.setFilePath(Const.generated_result);
		genParse.parse(true);
		
		// evaluation
		Evaluator evaluator = new Evaluator();
		if(Const.needsQueryForAnswer)
			evaluator.setGoldAnswer(goldParse);
		evaluator.evaluate(goldParse, genParse);
		/**/
	}
}
