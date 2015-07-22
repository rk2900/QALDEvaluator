import java.util.ArrayList;
import java.util.HashSet;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import knowledgebase.ClientManagement;

public class Evaluator {
	
	public void setGoldAnswer(XMLParser goldParse) {
		ArrayList<Question> questions = goldParse.getQuestions();
		for (Question question : questions) {
			String query = question.query;
			question.answers.clear();
			System.err.println("QID = "+question.id);
			if(query == null || query.length() ==0 || query.contains("OUT OF SCOPE")) {
				System.err.println("No query!\tID = "+question.id);
				continue;
			}
			if(query.contains("ASK")) {
				Boolean result = ClientManagement.ask(query, true);
				question.answers.add(result.toString());
			} else {
				ResultSet results = ClientManagement.query(query, true);
				String varName = results.getResultVars().get(0);
				while(results.hasNext()) {
					QuerySolution qs = results.next();
					RDFNode node = qs.get(varName);
					String answer;
					if(node.isLiteral())
						answer = node.asLiteral().getString();
					else {
						answer = node.asResource().getURI();
					}
					question.answers.add(answer);
//					System.out.println("Question ID = "+question.id+"\t"+answer);
				}
			}
		}
	}
	
	
	public void evaluate(XMLParser goldParse, XMLParser genParse) {
		int goldQuestionNumber = 0;
		for (Question q : goldParse.getQuestions()) {
			goldQuestionNumber = goldQuestionNumber<q.id?q.id:goldQuestionNumber;
		}
		System.err.println("TOTAL NUMBER = "+goldQuestionNumber);
		int[] status = new int[goldQuestionNumber+1];
		double[] precision = new double[goldQuestionNumber+1];
		double[] recall = new double[goldQuestionNumber+1];
		double[] fscore = new double[goldQuestionNumber+1];
		for(int i=0; i<goldQuestionNumber+1; i++) {
			precision[i] = recall[i] = fscore[i] = 1.0;
		}
		
		for (Question goldQ : goldParse.getQuestions()) {
			int qid = goldQ.id;
			System.err.println("QID = "+goldQ.id);
			Question genQ = genParse.getQuestionWithId(goldQ.id);
			
			if(goldQ.query.contains("yago")) {
				precision[goldQ.id] = recall[goldQ.id] = 0.0;
				status[qid] = -4; // yago
				continue;
			}
			
			if(genQ == null) {
				if(goldQ.answers.isEmpty() || goldQ.answers.getFirst().equals("OUT OF SCOPE")) {
					precision[goldQ.id] = recall[goldQ.id] = 1.0;
					status[qid] = -1; // gold:0 we:0
				} else {
					precision[goldQ.id] = recall[goldQ.id] = 0.0;
					status[qid] = -2; // gold:1 we:0
				}
			}
			else {
				if(goldQ.answers.isEmpty() || goldQ.answers.getFirst().equals("OUT OF SCOPE")) {
					precision[qid] = recall[qid] = 0.0; // gold:0 we:1
					status[qid] = -3;
				}
				else{ // gold:1 we:1
					HashSet<String> goldAnsSet = new HashSet<>();
					HashSet<String> intersect = new HashSet<>();
					for (String ans : goldQ.answers) {
						goldAnsSet.add(ans);
						System.err.print(ans+"\t");
					}
					System.err.println("\n------------------");
					for (String genAns : genQ.answers) {
						System.err.print(genAns+"\t");
						if(goldAnsSet.contains(genAns)) {
							intersect.add(genAns);
						}
					}
					System.err.println("\n===================================");
					precision[qid] = intersect.size()*1.0/genQ.answers.size();
					recall[qid] = intersect.size()*1.0/goldQ.answers.size();
					status[qid] = 1;
				}
			}
		}
		
		double p = 0.0;
		double r = 0.0;
		double f1 = 0.0;
		
		int fakeCount = 0;
		
		for(int i=1; i<goldQuestionNumber+1; ++i) {
			if(status[i] == 0) { // not a real question in the test file
				fakeCount++;
				continue;
			}
			p += precision[i];
			r += recall[i];
			if(precision[i]<0.0001 && recall[i]<0.0001) {
				fscore[i] = 0.0;
			} else {
				fscore[i] = 2*precision[i]*recall[i]/(precision[i]+recall[i]);
			}
			f1 += fscore[i];
			System.out.println(i+"\t"+precision[i]+"\t"+recall[i]+"\t"+status[i]);
		}
		p /= (goldQuestionNumber-fakeCount);
		r /= (goldQuestionNumber-fakeCount);
		f1 /= (goldQuestionNumber-fakeCount);
		
		System.err.println("FAKE COUNT = "+fakeCount);
		System.out.println("GLOBAL_2: precision="+p+", "+"recall="+r+", "+"f1="+f1);
		
		/***************************/
		p = 0.0;
		r = 0.0;
		f1 = 0.0;
		int count=0;
		for(int i=1; i<goldQuestionNumber+1; ++i) {
			if( !(status[i] == 1 || status[i] == -3)) {
				continue;
			}
			count++;
			p += precision[i];
			r += recall[i];
			if(precision[i]<0.0001 && recall[i]<0.0001) {
				fscore[i] = 0.0;
			} else {
				fscore[i] = 2*precision[i]*recall[i]/(precision[i]+recall[i]);
			}
			f1 += fscore[i];
		}
		System.out.println("COUNT = "+count);
		System.out.println("GLOBAL: precision="+p/(goldQuestionNumber-fakeCount)+", "+"recall="+r/(goldQuestionNumber-fakeCount)+", "+"f1="+f1/(goldQuestionNumber-fakeCount));
		System.out.println("PARTIAL: precision="+p/count+", "+"recall="+r/count+", "+"f1="+f1/count);
		
	}
}
