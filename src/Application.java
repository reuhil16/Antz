import java.util.Scanner;

import Model.AntType;
import Model.Universe;

public class Application {

	public static void main(String[] args) {
		Universe modle = new Universe();
		
		Scanner sc = new Scanner(System.in);
		String[] line;
		
		while(sc.hasNextLine()) {
			line = sc.nextLine().split("\\s+");
			
		}
		

	}
	
	public static AntType parseAnt(Scanner sc){
		String[] line;
		AntType result = new AntType();
		
		while(sc.hasNextLine()) {
			line = sc.nextLine().split("\\s+");
			if (line[0].equalsIgnoreCase("END"))
				break;
			if (line.length != 3 || line[0].length() != 1 || 
					line[1].length() != 4 || line[2].length() != 4) {
				System.err.println("Malformed input.");
				System.exit(1);
			}
			
		}
		
		return null;
	}

}
