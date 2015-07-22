
public class Tool {
	public static String removeNewLine(String s) {
		if(s.startsWith("\n")) {
			s = s.substring(1);
		} 
		
		if(s.endsWith("\n")) {
			s = s.substring(0, s.length()-1);
		}
		
		return s;
	}
}
