package bd.gene_network.logic;

import java.util.ArrayList;
import java.util.Map;

import fern.simulation.Simulator;
import fern.simulation.Simulator.FireType;
import fern.simulation.observer.Observer;

/**
 * This class produces the desired input signal over time. 
 * @author Bastiaan van den Berg
 *
 */
public class BinaryTimingDiagramObserver extends Observer {

	// fixed...
	// low concentration molecule count input environmental signal
	private final int lowInput = 0;
	// high concentration molecule count input environmental signal
	private final int highInput = 100;
	// when signal is set to low, it first drops to this value, before set to zero...
	// otherwise errors occur...
	private final int droppedInput = 10;
	// for the initialization of the system
	private final int lowOutput = 0;
	private final int highOutput = 10;
	
	//*** user input ***/
	// 
	private Map<String, boolean[]> binaryTimingDiagram;
	// ids of input species
	private Map<String, Integer> input;
	// ids of output species
	private Map<String, Integer> output;
	
	
	// the species (small molecule) that is being drained
	private ArrayList<Integer> drainedSpecies = new ArrayList<Integer>();
	
	private int stateTime;
	private int simTime;
	
	private int state;
	
	// stateTransition contains two Strings: state before, state after. The length of these strings
	// are input.size()+output.size(). 0 is low 1 is high
	public BinaryTimingDiagramObserver(
			Simulator sim, 
			Map<String, boolean[]> binaryTimingDiagram, 
			Map<String, Integer> input, 
			Map<String, Integer> output,
			int stateTime,
			int simTime) {
		super(sim);
		this.binaryTimingDiagram = binaryTimingDiagram;
		this.input = input;
		this.output = output;
		this.stateTime = stateTime;
		this.simTime = simTime;
	}

	@Override
	public void activateReaction(int mu, double tau, FireType fireType,
			int times) {
	}

	@Override
	public void finished() {
	}

	@Override
	public void started() {
		
		// init theta to steady state time
		this.setTheta(stateTime);
		
		// init counter
		state = 0;
		
		// init initial molecule counts
		Simulator s = this.getSimulator();
		for(String key : input.keySet()) {
			int species = input.get(key);
			boolean[] plot = binaryTimingDiagram.get(key);
			int amount = plot[state] ? highInput : lowInput;
			s.setAmount(species, amount);
		}
		for(String key : output.keySet()) {
			int species = output.get(key);
			boolean[] plot = binaryTimingDiagram.get(key);
			int amount = plot[state] ? highOutput : lowOutput;
			s.setAmount(species, amount);
		}
	}

	@Override
	public void step() {
		for(Integer i : drainedSpecies) {
			this.getSimulator().setAmount(i, 0);
		}
	}

	@Override
	public void theta(double theta) {
		
		Simulator s = this.getSimulator();
		
		if(theta < simTime) {
		
			// increase state counter
			state++;
			
			// set new molecule counts
			for(String key : input.keySet()) {
				int species = input.get(key);
				boolean[] plot = binaryTimingDiagram.get(key);
				int newAmount = plot[state] ? highInput : lowInput;
				double oldAmount = s.getAmount(species);
				if(oldAmount == 0 && newAmount != 0) {
					// just set the amount to high level
					s.setAmount(species, newAmount);
					drainedSpecies.remove(new Integer(species));
				}
				else if(oldAmount != 0 && newAmount == 0) {
					s.setAmount(species, droppedInput);
					drainedSpecies.add(new Integer(species));
				}
			}
			
			// set new Theta
			setTheta((state + 1) * stateTime);
		}
	}
}
