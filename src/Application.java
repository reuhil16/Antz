import java.util.Scanner;

import Model.*;
public class Application {

	public static void main(String[] args) {
		Universe model = new Universe();
		
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
			int[] directions = new int[4];
			for (int i = 0; i < 4; i++){
				switch(line[1].charAt(i)){
				case 'N':
					directions[i] = 0;
					break;
				case 'E':
					directions[i] = 1;
					break;
				case 'S':
					directions[i] = 2;
					break;
				case 'W':
					directions[i] = 3;
					break;
				default:
					System.err.println("Malformed input.");
					System.exit(1);
					break;
				}
			}
			result.addChromosome(line[0].charAt(0), directions, line[2].toCharArray());
			
		}
		
		return result;
	}

}
