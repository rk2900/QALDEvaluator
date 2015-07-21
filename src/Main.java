
public class Main {
	public static void main(String[] args) {
		// load gold XML file
		XMLParser goldParse = new XMLParser();
		goldParse.setFilePath(Const.qald_result_gold);
		goldParse.parse(true);
		System.out.println(goldParse.getQuestions().size());
		System.out.println("here");
		
		// load generated XML file
		XMLParser genParse = new XMLParser();
		genParse.setFilePath(Const.generated_result);
		genParse.parse(true);
		System.out.println(genParse.getQuestions().size());
		
		// evaluation
		Evaluator evaluator = new Evaluator();
		if(Const.needsQueryForAnswer)
			evaluator.setGoldAnswer(goldParse);
		evaluator.evaluate(goldParse, genParse);
	}
}
