package bd.gene_network.logic;

import fern.network.Network;
import fern.network.fernml.FernMLNetwork;
import fern.simulation.Simulator;
import fern.simulation.algorithm.GillespieSimple;
import fern.simulation.observer.AmountIntervalObserver;
import fern.tools.gnuplot.GnuPlot;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.JDOMException;

import bd.biopart.Device;
import bd.biopart.EnvironmentalSignal;
import bd.biopart.ProteinGenerator;
import bd.biopart.Reporter;
import bd.biopart.TF;
import bd.global.Constants;
import bd.model.Model0;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

/**
 * Class used to run stochastic simulations on a gene network.
 * @author Bastiaan van den Berg
 *
 */
public class LogicGeneNetworkSimulation {
	
	private static final int numSimulationRuns = 20;
	private static final int measurementsPerState = 100;
	
	public static final int RESULTS_PER_OUTPUT = 3;
	public static final int CORRELATION = 0;
	public static final int MEAN_MIN_LOW = 1;
	public static final int MEAN_MAX_HIGH = 2;
	
	/**
	 * Run a simulation on a gene network device. Very long method, should be split. The score 
	 * calculation is part of this method. This should be separated so that it can easily be 
	 * changed, or multiple score implementations can be used.
	 * @param device
	 * @param settings
	 * @return score for this simulation.
	 */
	public static double[] run(Device device, LogicGeneNetworkSettings settings) {
				
		// get data from the settings
		Map<String, TF> input = settings.getInput();
		Map<String, Reporter> output = settings.getOutput();
		Map<String, boolean[]> binaryTimingDiagram = settings.getBinaryTimingDiagram();
		int stateTime = settings.getStateTime();
		boolean visual = settings.isVisual();
		
		// device -> model -> network... (inefficient!!!)
		Model0 model = new Model0(device);
		//System.out.println(model.toString());
		model.toFernMl();
		File file = new File(Constants.TMP_MODEL_DIR+device.getName()+".fernml");
		Network net = null;
		try {
			net = new FernMLNetwork(file);
		} 
		catch (IOException e) {
			e.printStackTrace();
		} 
		catch (JDOMException e) {
			e.printStackTrace();
		}
		
		// new simulator is needed because not able to remove listeners
		// create simulator for the network
		Simulator sim = new GillespieSimple(net);
		
		// translate TFs to input species int id values
		Map<String, Integer> intInputMapping = new HashMap<String, Integer>();
		for(String id : input.keySet()) {
			TF tf = input.get(id);
			EnvironmentalSignal sm = tf.getSmallMolecule();
			intInputMapping.put(id, sim.getNet().getSpeciesByName(sm.getName())); 
		}
		// translate Reporters to output species int id values
		Map<String, Integer> intOutputMapping = new HashMap<String, Integer>();
		for(String id : output.keySet()) {
			Reporter rep = output.get(id);
			intOutputMapping.put(id, sim.getNet().getSpeciesByName(rep.getName()));	
		}
		
		// update the score based on the simulation results
		// calculate the number of state transitions
		int numInputs = input.keySet().size();
		int numOutputs = output.keySet().size();
		Object[] plots = binaryTimingDiagram.values().toArray();
		int numStates = ((boolean[]) plots[0]).length;
		// calculate the simulation time
		int simTime = numStates * stateTime;
		
		// make lists of keys to ensure the same ordering...
		String[] inputKeys = new String[numInputs];
		int index = numInputs-1;
		for(String key : input.keySet()) {
			inputKeys[index] = key;
			index--;
		}
		String[] outputKeys = new String[numOutputs];
		index = numOutputs-1;
		for(String key : output.keySet()) {
			outputKeys[index] = key;
			index--;
		}
		// make lists of species numbers with the same ordering...
		int[] intInput = new int[numInputs];
		for(int i = 0; i < numInputs; i++) {
			intInput[i] = intInputMapping.get(inputKeys[i]);
		}
		int[] intOutput = new int[numOutputs];
		for(int i = 0; i < numOutputs; i++) {
			intOutput[i] = intOutputMapping.get(outputKeys[i]);
		}
		
		// calculate the stepsize to use
		int stepSize = stateTime/measurementsPerState;
		
		// add input and output observers to simulator
		BinaryTimingDiagramObserver stio = new BinaryTimingDiagramObserver(
				sim, 
				binaryTimingDiagram,
				intInputMapping, 
				intOutputMapping, 
				stateTime,
				simTime);
		sim.addObserver(stio);
			
		// for visualization, slow...
		GnuPlot gp = null;
		AmountIntervalObserver vaio = null;
		if(visual) {
			// get the species to track
			int[] all = new int[numInputs + numOutputs];
			int j = 0;
			for(int i = 0; i < numInputs; i++) {
				all[j] = intInput[i];
				j++;
			}
			for(int i = 0; i < numOutputs; i++) {
				all[j] = intOutput[i];
				j++;
			}
			
			// create plot
			gp = new GnuPlot(); 
			gp.setDefaultStyle("with lines");
			// add amount interval observer
			vaio = new AmountIntervalObserver(sim, stepSize, all);
			//vaio = new AmountIntervalObserver(sim, stepSize, intOutput);
			sim.addObserver(vaio);
		}
		
		// add observer to observe amounts
		AmountIntervalObserver aio = new AmountIntervalObserver(sim, stepSize, intOutput);
		sim.addObserver(aio);
		
		boolean gnuErrorDisplayed = false;
		
		// run simulation state transition numSimulationRuns times
		for(int i = 0; i < numSimulationRuns; i++) {
			sim.start(simTime);
			if(visual) {
				try {
					vaio.toGnuplot(gp);
					gp.plot();
				} 
				catch (IOException e) {
					//e.printStackTrace();
					if(gnuErrorDisplayed == false) {
						System.out.println();
						System.out.println();
						System.out.println("No visualization, gnuplot is not accessible, add it to your path variable.");
						System.out.println();
						gnuErrorDisplayed = true;
					}
				}
				gp.setVisible(true);
				gp.clearData();
			}
		}
		
		// get the date after numSimulation runs
		GnuPlot data = new GnuPlot();
		try {
			aio.toGnuplot(data);
			// save data if visual
			if(visual) {
				// creat tmp dir if it is not already there
				File tmpDir = new File(Constants.TMP_DATA_DIR);
				if(!tmpDir.exists()) {
					tmpDir.mkdir();
				}
				// create new data file
				int counter = 0;
				File dataFile = new File(tmpDir.getPath() + File.separator + "simulation" + counter + ".dat");
				while(!dataFile.createNewFile()) {
					counter++;
					dataFile = new File(tmpDir.getPath() + File.separator + "simulation" + counter + ".dat");
				}
				vaio.toGnuplot(gp);
				gp.saveData(dataFile);
				dataFile.setReadOnly();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// calculate score from obtained data
		Iterator<String> iter = data.getAxes().get(0).iterator();
		
		// ignore first state... initialize the system...
		for(int i = 0; i < measurementsPerState; i++) {
			iter.next();
		}
		
		// create desired output
		int numMeasurements = (numStates-1) * measurementsPerState;
		double[][] desired = new double[numOutputs][numMeasurements];
		for(int i = 0; i < numOutputs; i++) {
			// plot for this output
			boolean[] plot = binaryTimingDiagram.get(outputKeys[i]);
			for(int state = 1; state < numStates; state++) {
				int stateStart = (state - 1) * measurementsPerState;
				int stateEnd = (state * measurementsPerState) - 1;
				int value = plot[state] ? 1 : 0;
				Arrays.fill(desired[i], stateStart, stateEnd, value);
			}
		}
		List<DoubleArrayList> desiredLists = new ArrayList<DoubleArrayList>();
		for(int out = 0; out < numOutputs; out++) {
			desiredLists.add(new DoubleArrayList(desired[out]));
		}
		
		// create arrays with the measured output
		double[][] measured = new double[numOutputs][numMeasurements];
		for(int measurement = 0; measurement < numMeasurements; measurement++) {
			String[] values = iter.next().split("\t");
			for(int out = 0; out < numOutputs; out++) {
				measured[out][measurement] = Double.valueOf(values[out+1]);
			}
		}		
		List<DoubleArrayList> measuredLists = new ArrayList<DoubleArrayList>();
		for(int out = 0; out < numOutputs; out++) {
			measuredLists.add(new DoubleArrayList(measured[out]));
		}
		
		// the +1 is for the calculated score
		double[] result = new double[numOutputs*RESULTS_PER_OUTPUT + 1];
		
		for(int out = 0; out < numOutputs; out++) {
			// calculate correlation per output
			double meanDes = Descriptive.mean(desiredLists.get(out));
			double meanMeas = Descriptive.mean(measuredLists.get(out));
			double varDes = Descriptive.sampleVariance(desiredLists.get(out), meanDes);
			double varMeas = Descriptive.sampleVariance(measuredLists.get(out), meanMeas);
			double stdDes = Descriptive.sampleStandardDeviation(desiredLists.get(out).size(), varDes);
			double stdMeas = Descriptive.sampleStandardDeviation(measuredLists.get(out).size(), varMeas);
			double correlation = Descriptive.correlation(desiredLists.get(out), stdDes, measuredLists.get(out), stdMeas);
			// set correlation to zero when it is negative (to avoid negative times negative is positive later on)
			if(correlation < 0) {
				result[out*RESULTS_PER_OUTPUT + CORRELATION] = 0;
			}
			else {
				result[out*RESULTS_PER_OUTPUT + CORRELATION] = correlation;
			} 
			// calculate ...
			double sumMinLow = 0;
			double sumMaxHigh = 0;
			int countLow = 0;
			int countHigh = 0;
			for(int i = 0; i < numStates-1; i++) {
				int start = i * measurementsPerState;
				int end = start + measurementsPerState-1;
				DoubleArrayList list = (DoubleArrayList) measuredLists.get(out).partFromTo(start, end);
				if(desiredLists.get(out).get(start) == 0) {
					sumMinLow += Descriptive.min(list);
					countLow++;
				}
				else {
					sumMaxHigh += Descriptive.max(list);
					countHigh++;
				}
			}
			result[out*RESULTS_PER_OUTPUT + MEAN_MIN_LOW] = sumMinLow/countLow;
			result[out*RESULTS_PER_OUTPUT + MEAN_MAX_HIGH] = sumMaxHigh/countHigh;
		}
		
		// calculate the score
		double combinedCorrelation = result[CORRELATION];
		double combinedMinValue = result[MEAN_MIN_LOW];
		double combinedMaxValue = result[MEAN_MAX_HIGH];
		for(int i = 1; i < numOutputs; i++) {
			int start = i * RESULTS_PER_OUTPUT;
			combinedCorrelation *= result[start + CORRELATION];
			combinedMinValue += result[start + MEAN_MIN_LOW];
			combinedMaxValue *= result[start + MEAN_MAX_HIGH];
		}
		double totalScore = combinedCorrelation * (combinedMaxValue-combinedMinValue);
		result[RESULTS_PER_OUTPUT*numOutputs] = totalScore;
			
		return result;
	}
	
	/**
	 * Writes the result of a simulation to the provided writer. A tab separated
	 * line is written with for each output the correlation, mean min value, 
	 * mean max value. The last three values are the averaged correlation, mean
	 * min value, and mean max value over all outputs.
	 * @precondition score is a 3 by (#ouputs + 1) matrix
	 * @param score
	 * @param device
	 * @param writer
	 */
	public static void writeResult(double[] score, Device device, Writer writer) {
		DecimalFormat decFormat = new DecimalFormat("0.000");
		String line = "";
		for(int i = 0; i < score.length; i++) {
			line += decFormat.format(Double.valueOf(score[i])) + "\t";
		}
		for(ProteinGenerator pg : device.getProteinGenerators()) {
			line += pg.getPromoter().getName() + "\t";
			line += pg.getRBS().getName() + "\t";
			line += pg.getProteinCoding().getProtein().getName() + "\t";
		}
		line += device.toString() + "\n";
		try {
			writer.write(line);
			writer.flush();
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
			System.out.println("An input/output error occured when writing" +
					"data to the data file.");
		}
	}
}
