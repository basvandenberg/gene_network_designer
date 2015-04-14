/**
 * 
 */
package bd.gene_network.logic;


import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.Scanner;

import bd.biopart.Device;
import bd.gene_network.GeneNetworkBuilder;

/**
 * Run a simulation on a randomly selected device from all possible
 * devices for a given template and bioparts database.
 * @author Bastiaan van den Berg
 *
 */
public class SimRandomLogicGeneNetwork {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// create a scanner to read user input
		Scanner s = new Scanner(System.in);
		
		boolean correct = false;
		
		while(!correct) {
			// ask for device name
			System.out.print("\nProvide the device name, or q to go back to the main menu: ");
			// read answer
			String deviceName = s.next();
			if(deviceName.equals("q")) {
				correct = true;
			}
			else {
				try {
					randomDeviceSimulation(deviceName);
					correct = true;
				}
				catch(FileNotFoundException fnfe) {
					System.out.println(fnfe.getMessage());
				}
			}
		}
	}
	
	public static Device randomDeviceSimulation(String deviceName) throws FileNotFoundException {
		
		// init logic device builder
		GeneNetworkBuilder ldb = new GeneNetworkBuilder(deviceName);
		
		// get a random instantiation of the device
		System.out.println("\n==> Get a random gene network intstance...");
		Device randomDevice = ldb.getRandomGeneNetwork();
		
		if(randomDevice != null) {
			System.out.println(randomDevice.toString() + "\n");
			
			// run simulation
			System.out.println("*** Logic device simulation ***");
			System.out.print("- Running simulation...");
			double[] result = LogicGeneNetworkSimulation.run(randomDevice, ldb.getLogicGeneNetworkSettings());
			System.out.println(" , done.");
			
			DecimalFormat decFormat = new DecimalFormat("0.000");
			
			// print results to standard output
			int rpo = LogicGeneNetworkSimulation.RESULTS_PER_OUTPUT;
			int numOutputs = randomDevice.getOutputProteins().size();
			for(int i = 0; i < numOutputs; i++) {
				System.out.println("=> output " + i + ":");
				System.out.println("--> correlation: " + decFormat.format(Double.valueOf(result[i*rpo + LogicGeneNetworkSimulation.CORRELATION])));
				System.out.println("--> mean min low value: " + decFormat.format(Double.valueOf(result[i*rpo + LogicGeneNetworkSimulation.MEAN_MIN_LOW])));
				System.out.println("--> mean max high value: " + decFormat.format(Double.valueOf(result[i*rpo + LogicGeneNetworkSimulation.MEAN_MAX_HIGH])));
			}
			System.out.println("=> total:");
			System.out.println("--> score: " + decFormat.format(result[result.length-1]));
			System.out.println("*** Logic device simulation done ***\n");
			
			return randomDevice;
		}
		else {
			System.out.println("\n==> Not able to build a random " + deviceName + " device with the parts available in the database.");
			return null;
		}
	}
}
