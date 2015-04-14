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
 * Run a simulation on a given gene network.
 * @author Bastiaan van den Berg
 *
 */
public class SimLogicGeneNetwork {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// create a scanner to read user input
		Scanner s = new Scanner(System.in);
		
		boolean correct = false;
		
		while(!correct) {
			// ask for device (string representation)
			System.out.println("\nProvide the string representation of the device, or press q to exit:");
			// read answer
			String deviceString = s.nextLine();
			if(deviceString.equals("q")) {
				correct = true;
			}
			else {
				try {
					deviceSimulation(deviceString);
					correct = true;
				}
				catch(FileNotFoundException fnfe) {
					System.out.println(fnfe.getMessage());
				}
				catch(Exception e) {
					System.out.println("==> The provided string representation contains an error.");
				}
			}
		}
	}
	
	public static Device deviceSimulation(String deviceString) throws FileNotFoundException {
		
		// build device from the string representation
		Device device = Device.fromStringRepresentation(deviceString);
		String deviceName = device.getName();
		
		// init logic device builder
		GeneNetworkBuilder ldb = new GeneNetworkBuilder(deviceName);
		
		// already instantiated...
		//LogicGeneNetworkSettings lds = LogicGeneNetworkSettings.instance();
		
		// run simulation
		System.out.println("\n*** Logic device simulation ***");
		System.out.print("- Running simulation...");
		double[] result = LogicGeneNetworkSimulation.run(device, ldb.getLogicGeneNetworkSettings());
		System.out.println(" , done.");
		
		DecimalFormat decFormat = new DecimalFormat("0.000");
		
		// print results to standard output
		int rpo = LogicGeneNetworkSimulation.RESULTS_PER_OUTPUT;
		int numOutputs = device.getOutputProteins().size();
		for(int i = 0; i < numOutputs; i++) {
			System.out.println("=> output " + i + ":");
			System.out.println("--> correlation: " + decFormat.format(Double.valueOf(result[i*rpo + LogicGeneNetworkSimulation.CORRELATION])));
			System.out.println("--> mean min low value: " + decFormat.format(Double.valueOf(result[i*rpo + LogicGeneNetworkSimulation.MEAN_MIN_LOW])));
			System.out.println("--> mean max high value: " + decFormat.format(Double.valueOf(result[i*rpo + LogicGeneNetworkSimulation.MEAN_MAX_HIGH])));
		}
		System.out.println("--> score: " + decFormat.format(result[result.length-1]));
		System.out.println("*** Logic device simulation done ***");
		
		return device;
	}
}
