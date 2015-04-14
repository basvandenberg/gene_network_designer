package bd.gene_network.logic;


import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import bd.biopart.Device;
import bd.gene_network.GeneNetworkBuilder;
import bd.global.Constants;

/**
 * Run a simulation on all possible gene networks for a given template with
 * the available bioparts database.
 * @author Bastiaan van den Berg
 *
 */
public class SimAllLogicGeneNetworks {

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// create a scanner to read user input
		Scanner s = new Scanner(System.in);
		
		GeneNetworkBuilder ldb = null;
		boolean correct = false;
		
		while(!correct) {
			// ask for device (string representation)
			System.out.print("\nProvide the name of the logic device name: ");
			// read answer
			String deviceName = s.next();
			
			// logic device builder
			try {
				ldb = new GeneNetworkBuilder(deviceName);
				correct = true;
			}
			catch(FileNotFoundException fnfe) {
				System.out.println(fnfe.getMessage());
			}
		}
		
		System.out.println("\nWARNING: This will only work for small gene " +
				"networks\n         and a small parts registry, otherwise an" +
				" out\n         of memory error will occur.");
		
		// get all possible device instantiations for the gene network using
		// bioparts database (great heap overflow risk...)
		System.out.println("\n===> Get a list with all possible devices...");
		List<Device> allDevices = ldb.getAllGeneNetworks();
		int numDevices = allDevices.size();
		
		// create data file and writer to that file
		System.out.println("\n===> Create output file to write data...");
		String fileName = ldb.getLogicGeneNetworkSettings().getName() + "_allDevices";
		BufferedWriter writer = Constants.simulationDataFile(fileName);
				
		System.out.println("\n*** Logic device simulation ***");
		System.out.println("- Running simulations,  which takes a long time.");
		
		// device counter
		int counter = 0;
		
		// start time, used to inform the user about the time that it will take
		long startTime = new Date().getTime()/1000;
		
		// run simulation for each device
		for(Device device : allDevices) {
			
			// show the user the progress, print a dot per device
			if(counter % 100 == 0) {
				System.out.print("Devices " + counter + "..." + (counter+99) + ": ");
			}
			System.out.print(".");
			
			// run simulation
			double[] score = LogicGeneNetworkSimulation.run(device, ldb.getLogicGeneNetworkSettings());
			
			// write result to file
			LogicGeneNetworkSimulation.writeResult(score, device, writer);
			
			// timing, to inform the user
			if(counter % 100 == 99) {
				System.out.println(", done.");
				int runTime = (int) (new Date().getTime()/1000 - startTime);
				int estTime = (int) ((1.0*runTime / counter)*(numDevices-counter));
				System.out.println("Simulation time (sec): " + runTime);
				System.out.println("Expected time left (sec): " + estTime);
			}
			// increase device counter
			counter++;
		}		
		System.out.println("\n*** Logic device simulation done ***");
	}
}
