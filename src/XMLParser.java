

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.rdf.model.RDFNode;

import basic.FileOps;

/**
 * Parse QALD5 train data into Question structure instances
 * @author rk
 *
 */

public class XMLParser {
	
	private String filePath;
	private Document doc;
	private ArrayList<Question> questions;
	private HashMap<Integer, Question> mappedQuestions;
	private static HashSet<String> types;
	
	public XMLParser() {
		System.out.println("No document given.");
		filePath = new String();
		questions = new ArrayList<Question>();
		mappedQuestions = new HashMap<>();
	}
	
	public XMLParser(String file) {
		filePath = new String(file);
		load();
		questions = new ArrayList<Question>();
		mappedQuestions = new HashMap<>();
	}
	
	public XMLParser(Document document) {
		if(document == null) {
			System.out.println("Document not loaded.");
		} else {
			doc = document;
			questions = new ArrayList<Question>();
			mappedQuestions = new HashMap<>();
		}
	}
	
	public void setFilePath(String filePath) {
		this.filePath = new String(filePath);
		load();
		System.out.println("Set document file: "+filePath);
	}

	public void load() {
		if(FileOps.exist(filePath)) {
			File xmlFile = new File(filePath);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			try {
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				doc = dBuilder.parse(xmlFile);
			} catch (SAXException | IOException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
			System.out.println("File '"+filePath+"' loaded.");
		} else {
			System.out.println("File not exist.");
		}
	}
	
	public void parse(boolean withAns) {
		if(doc == null) {
			System.out.println("Document not loaded.");
			return;
		}
		System.out.println("Parsing ...");
		doc.getDocumentElement().normalize();
		NodeList nList = doc.getElementsByTagName("question");
		
		for(int qid=0; qid<nList.getLength(); qid++) {
			Node node = nList.item(qid);
			//attributes
			int id = 0;
			boolean onlydbo = false;
			boolean aggregation = false;
			boolean hybrid = false;
			String answerType = "";
			//contents
			LinkedList<String> keywords = new LinkedList<String>();
			String question = "";
			LinkedList<String> answers = new LinkedList<String>();
			String query = "";
			
			if(node.getNodeType() == Node.ELEMENT_NODE) {
				Element element = (Element)node;
				id = Integer.parseInt(element.getAttribute("id"));
				onlydbo = element.getAttribute("onlydbo").equals("true")?true:false;
				aggregation = element.getAttribute("aggregation").equals("true")?true:false;
				hybrid = element.getAttribute("hybrid").equals("true")?true:false;
				answerType = new String(element.getAttribute("answertype"));
				
				NodeList strNodeList = element.getElementsByTagName("string");
				for(int i=0; i<strNodeList.getLength(); i++) {
					Node strNode = strNodeList.item(i);
					if(strNode.getAttributes().item(0).getNodeValue().equals("en")) {
						question = new String(strNode.getTextContent());
						break;
					}
				}
				
				NodeList keywordNodeList = element.getElementsByTagName("keywords");
				for(int i=0; i<keywordNodeList.getLength(); i++) {
					Node keywordNode = keywordNodeList.item(i);
					if(keywordNode.getAttributes().item(0).getNodeValue().equals("en")) {
						String[] keys = keywordNode.getTextContent().split(",");
						for(String key: keys) {
							keywords.add(new String(key));
						}
						break;
					}
				}
				
				if(withAns) {
					Node queryNode = element.getElementsByTagName("query").item(0);
					if(queryNode != null) {
						query = new String(queryNode.getTextContent());
					}
					
					Element answersElement = (Element)element.getElementsByTagName("answers").item(0);
					if(answersElement != null) {
						NodeList answerList = answersElement.getElementsByTagName("answer");
						for(int i=0; i<answerList.getLength(); i++) {
							Node answer = answerList.item(i);
							answers.add(new String(answer.getTextContent()));
						}
					}
				} else {}
			}
			Question q = new Question(id, onlydbo, aggregation, hybrid, 
					answerType, keywords, question, answers, query);
			
			questions.add(q);
			mappedQuestions.put(id, q);
		}
		System.out.println("Parsing finished.");
	}
	
	public ArrayList<Question> getQuestions() {
		return questions;
	}
	
	public ArrayList<Question> getQuestionsPart(int begin, int end) {
		ArrayList<Question> qList = new ArrayList<Question>();
		int count = 0;
		for (Question question : questions) {
			if(count++ >= begin && count < end) {
				qList.add(question);
			}
		}
		return qList;
	}
	
	public ArrayList<Question> getQuestionsPart(int begin, int end, String type) {
		ArrayList<Question> qList = new ArrayList<>();
		if(types.contains(type)) {
			System.out.println("Found such QA type.");
			int count = 0;
			for (Question question : questions)
				if(count++ >= begin && count < end && question.answerType.equals(type))
					qList.add(question);
		} else
			System.out.println("Warning: No such QA pairs.");
		return qList;
	}
	
	/**
	 * To get specified question with q-id in training data.
	 * @param qid the id number in training data attributes
	 * @return the specified question
	 */
	public Question getQuestionWithId(int qid) {
		return mappedQuestions.get(qid);
	}
	
	/**
	 * To get specified question with pseudo q-id in XML file.
	 * @param id pseudo q-id
	 * @return the specified question
	 */
	public Question getQuestionWithPseudoId(int id) {
		return questions.get(id-1);
	}
	
	public Element getQuestionElement(Document doc, Integer id, LinkedList<RDFNode> answerList) {
			// question element
			Element question = doc.createElement("question");
			// set question attribute
			question.setAttribute("id", id.toString());
			
			System.out.println("XML Question ID: "+id);
			// set string element
			Element stringElement = doc.createElement("string");
			stringElement.setAttribute("lang", "en");
			question.appendChild(stringElement);
			
			// answers element
			Element answers = doc.createElement("answers");
			question.appendChild(answers);

			// answer element
			for (RDFNode node : answerList) {
				String ans;
				if(node.isLiteral()) {
					ans = node.asLiteral().getLexicalForm();
				} else {
					ans = node.toString();
				}
				if(ans != null) {
					Element answer = doc.createElement("answer");// Literal/Resource
					answer.appendChild(doc.createTextNode(ans));
					answers.appendChild(answer);
				}
			}
			return question;
	}
	
	
}
