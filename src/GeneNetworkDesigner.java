import java.io.File;
import java.util.Scanner;

import bd.biopart.BioPartDatabase;
import bd.biopart.BioPartDatabaseBuilder;
import bd.gene_network.logic.SimAllLogicGeneNetworks;
import bd.gene_network.logic.SimLogicGeneNetwork;
import bd.gene_network.logic.SimRandomLogicGeneNetwork;
import bd.global.Constants;

/**
 * Command line program. 
 * @author Bastiaan van den Berg
 *
 */
public class GeneNetworkDesigner {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// load parts registry
		BioPartDatabase.instance();
		
		File dataFile = new File(Constants.TMP_DATA_DIR);
		System.out.println("- NOTE: Data files will be written to " + dataFile.getAbsolutePath());
		
		while(true) {
			
			System.out.println("\n1. Simulate a random logic gene network");
			System.out.println("2. Simulate a logic gene network");
			System.out.println("3. Simulate all possible logic gene networks");
			System.out.println("4. (Re)Build bioparts database\n");
			System.out.print("Choose the program you want to run or 'q' to quit: ");

			Scanner scanner = new Scanner(System.in);
			boolean correctChoice = false;
			
			while(correctChoice == false) {
				String choice = scanner.next().trim();
				if(choice.equals("1")) {
					SimRandomLogicGeneNetwork.main(new String[0]);
					correctChoice = true;
				}
				else if(choice.equals("2")) {
					SimLogicGeneNetwork.main(new String[0]);
					correctChoice = true;
				}
				else if(choice.equals("3")) {
					SimAllLogicGeneNetworks.main(new String[0]);
					correctChoice = true;
				}
				else if(choice.equals("4")) {
					BioPartDatabaseBuilder.main(new String[0]);
					// reload the rebuild parts registry
					BioPartDatabase.instance().destroy();
					BioPartDatabase.instance();
					correctChoice = true;
				}
				else if(choice.equals("q")) {
					System.exit(0);
				}
				else {
					System.out.print("Incorrect input, please enter 1, 2, 3, 4, or q: ");
				}
			}
		}
	}
}
