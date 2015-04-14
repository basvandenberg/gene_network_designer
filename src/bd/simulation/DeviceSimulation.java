package bd.simulation;

import java.io.File;
import java.io.IOException;

import org.jdom.JDOMException;

import bd.global.Constants;

import fern.network.FeatureNotSupportedException;
import fern.network.Network;
import fern.network.fernml.FernMLNetwork;
import fern.simulation.Simulator;
import fern.simulation.algorithm.GillespieSimple;
import fern.simulation.observer.AmountIntervalObserver;
import fern.tools.NetworkTools;
import fern.tools.gnuplot.GnuPlot;

public class DeviceSimulation {

	/**
	 * @param args
	 * @throws JDOMException 
	 * @throws IOException 
	 * @throws FeatureNotSupportedException 
	 */
	public static void main(String[] args) throws IOException, JDOMException, FeatureNotSupportedException {

		/*** replace with configuration file data ***/
		String deviceName = "test";
		int stepSize = 10;
		int numSimulations = 50;
		int simTime = 50000;
		String[] obsProtein = {"reporter0","sm_iim0","sm_iim1"};
		String simDataFile = "test.dat";
		/*** end configuration ***/

		File file = new File(Constants.TMP_MODEL_DIR + deviceName + ".fernml");
		Network net = new FernMLNetwork(file);
		NetworkTools.dumpNetwork(net);

		Simulator sim = new GillespieSimple(net);

		// add observers
		AmountIntervalObserver amountObs = new AmountIntervalObserver(sim,stepSize,obsProtein);
		InputSignalObserver iso = new InputSignalObserver(sim);
		//ReactionObserver reactionObs = new ReactionObserver(sim);
		sim.addObserver(amountObs);
		sim.addObserver(iso);
		//sim.addObserver(reactionObs);

		// create gnuplot
		GnuPlot gp = new GnuPlot(); 
		gp.setDefaultStyle("with lines");

		for(int i = 0; i < numSimulations; i++) {
			System.out.println("sim " + i);
			sim.start(simTime);
			amountObs.toGnuplot(gp);
			gp.saveData(new File(SIM_DIR + simDataFile));
			gp.plot();
			gp.setVisible(true);
			//gp.saveImage(new File(String.format("images/TestSimulation%02d.png",j)));
			gp.clearData();
		}
	}

	public static final String SIM_DIR = ".." + File.separator + ".." + File.separator + "simulationData" + File.separator; 

}
