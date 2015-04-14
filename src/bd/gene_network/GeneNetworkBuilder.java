package bd.gene_network;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import bd.biopart.Activator;
import bd.biopart.Device;
import bd.biopart.EnvironmentalSignal;
import bd.biopart.Inhibitor;
import bd.biopart.BioPartDatabase;
import bd.biopart.Promoter;
import bd.biopart.Protein;
import bd.biopart.ProteinCoding;
import bd.biopart.ProteinGenerator;
import bd.biopart.RBS;
import bd.biopart.Reporter;
import bd.biopart.TF;
import bd.biopart.Terminator;
import bd.gene_network.logic.LogicGeneNetworkSettings;
import bd.gene_network.template.Edge;
import bd.gene_network.template.GeneNetworkTemplate;
import bd.gene_network.template.Vertice;
import bd.global.Constants;


/**
 * This class provides the functionality to build gene networks for a given 
 * gene network template using the available bioparts in the bioparts database.
 * The input file is not extensively checked for errors... should be improved.
 * @author Bastiaan van den Berg
 *
 */
public class GeneNetworkBuilder {

	private String geneNetworkDir;
	
	private LogicGeneNetworkSettings geneNetworkSettings;
	
	private String name;
	private GeneNetworkTemplate geneNetworkTemplate;
	private Map<String, TF> inputMapping;
	private Map<String, Reporter> outputMapping;
	
	private Map<Edge,List<Protein>> possibleProteinsPerEdge;
	private Set<EnvironmentalSignal> environmentalSignals;
	
	private BioPartDatabase pr;
	
	private Random rnd;

	/**
	 * Load gene network data from file.
	 * @param deviceName
	 * @throws FileNotFoundException
	 */
	public GeneNetworkBuilder(String deviceName) throws FileNotFoundException {
		
		geneNetworkDir = Constants.LOGIC_DEVICE_DIR + deviceName + File.separator;
		
		// check if dir exists
		File file = new File(geneNetworkDir);
		if(!file.exists()) {
			String msg = "\n==> Could not load the device settings:\n" +
					"The directory "+file.getAbsolutePath()+" does not exist.";
			throw new FileNotFoundException(msg);
		}
		
		// initialize problem specific settings (gen. netw., input, output...)
		geneNetworkSettings = new LogicGeneNetworkSettings(geneNetworkDir + "settings.xml");
		
		// store needed data from the settings
		this.name = geneNetworkSettings.getName();
		this.geneNetworkTemplate = geneNetworkSettings.getGeneNetwork();
		this.inputMapping = geneNetworkSettings.getInput();
		this.outputMapping = geneNetworkSettings.getOutput();
		
		// get random number generator
		rnd = new Random();
		
		// load bioparts database
		pr = BioPartDatabase.instance();
		
		// check the settings
		check();
		
		// calculate and store all possible TFs per edge
		initPossibleProteinsPerEdge();
		//System.out.println(possibleProteinsPerEdge.toString());
		
		// init the set of small molecules
		environmentalSignals = new HashSet<EnvironmentalSignal>();
		for(TF tf : inputMapping.values()) {
			environmentalSignals.add(tf.getSmallMolecule());
		}
	}
	
	public String getGeneNetworkDir() {
		return geneNetworkDir;
	}
	
	public LogicGeneNetworkSettings getLogicGeneNetworkSettings() {
		return geneNetworkSettings;
	}
	
	/**
	 * @return A random device instantiation based on the gene network using
	 * the biological parts in the parts database. Returns null if not able to
	 * build a random device with the available parts in the database.
	 */
	public Device getRandomGeneNetwork() {
		
		// get random wiring
		Map<Edge,Protein> randomWiring = randomWiring();
		
		// build list of protein generators (one per vertice)
		List<ProteinGenerator> pgs = new ArrayList<ProteinGenerator>();
		for(Vertice v : geneNetworkTemplate.getVertices()) {
			
			// get the input transcription factors for this protein generator 
			List<TF> tfs = new ArrayList<TF>();
			for(Edge edge : v.getInput()) {
				TF tf = (TF)randomWiring.get(edge);
				tfs.add(tf);
			}
			
			// pick a random promoter from promoter library
			List<Promoter> library = pr.getPromoterLibrary(tfs);
			Promoter p = null;
			if(library != null) {
				p = library.get(rnd.nextInt(library.size()));
			}
			
			// pick random rbs
			RBS rbs = null;
			if(pr.getAllRBS().size() > 0) {
				rbs = pr.getAllRBS().get(rnd.nextInt(pr.getAllRBS().size()));
			}
			
			// protein coding part is determined by the wiring
			Protein product = randomWiring.get(v.getOutput());
			ProteinCoding pc = ProteinCoding.getProteinCoding(product, pr.getAllPC());
			
			// pick random terminator (there's only one...)
			Terminator t = null;
			if(pr.getAllTerminators().size() > 0) {
				t = pr.getAllTerminators().get(rnd.nextInt(pr.getAllTerminators().size()));
			}
			
			// create the protein generator and add it to the list
			ProteinGenerator pg = new ProteinGenerator(p,rbs,pc,t);
			
			// not able to build protein generator with available parts.
			if(p == null || rbs == null || pc == null || t == null) {
				return null;
			}
			
			pgs.add(pg);
		}
		return new Device(name, pgs, environmentalSignals);
	}
	
	/**
	 * @return All possible networks for the given template with the available parts
	 * in the database.
	 */
	public List<Device> getAllGeneNetworks() {
		
		List<Device> allDevices = new ArrayList<Device>();
		
		// get all possible wirings
		List<Map<Edge,Protein>> wirings = allPossibleWirings();
		System.out.println("\nnumber of possible wirings: " + wirings.size());
		
		// for each of the wirings
		for(Map<Edge,Protein> wiring : wirings) {
			// build list of all possible protein generators per vertice
			List<List<ProteinGenerator>> pgsPerVertice = new ArrayList<List<ProteinGenerator>>();
			for(Vertice v : geneNetworkTemplate.getVertices()) {
				List<ProteinGenerator> pgs = new ArrayList<ProteinGenerator>();
				// get promoter library
				List<TF> tfs = new ArrayList<TF>();
				for(Edge input : v.getInput()) {
					tfs.add((TF)wiring.get(input));
				}
				List<Promoter> pms = pr.getPromoterLibrary(tfs);
				// get protein coding part
				ProteinCoding pc = ProteinCoding.getProteinCoding(wiring.get(v.getOutput()));
				// get terminator
				Terminator t = new Terminator(Terminator.emptyInstance().getAllNames().get(0));
				// build the list of possible protein generators for this wiring
				for(Promoter pm : pms) {
					for(RBS rbs : pr.getAllRBS()) {
						pgs.add(new ProteinGenerator(pm,rbs,pc,t));
					}
				}
				// add list to list of lists
				pgsPerVertice.add(pgs);				
			}			
			List<List<ProteinGenerator>> result = new ArrayList<List<ProteinGenerator>>();
			int numVertices = geneNetworkTemplate.getVertices().size();
			List<ProteinGenerator> current = new ArrayList<ProteinGenerator>(numVertices);
			for(int i = 0; i < numVertices; i++) {
				current.add(null);
			}
			pgCombinationsRec(pgsPerVertice,current,0,result);
			for(List<ProteinGenerator> pgList : result) {
				allDevices.add(new Device(name,pgList,environmentalSignals));
			}
		}

		System.out.println("number of devices: " + allDevices.size());
		return allDevices;
	}
	
	public List<Device> getVariations(Device basis) {
		
		// the list with devices which will be returned as result
		List<Device> result = new ArrayList<Device>();
		
		// list of protein generators from the basis device
		List<ProteinGenerator> pgs = basis.getProteinGenerators();
		
		// iterate over all the protein generators to vary them
		for(int i = 0; i < pgs.size(); i++) {
			
			// current protein generator
			ProteinGenerator current = pgs.get(i);
			
			// original parts in protein generator
			Promoter pm = current.getPromoter();
			RBS rbs = current.getRBS();
			ProteinCoding pc = current.getProteinCoding();
			Terminator t = current.getTerminator();
			
			// list with promoters (from same library) and rbs's
			Promoter promoter = current.getPromoter();
			List<Promoter> library = pr.getPromoterLibrary(promoter.getTFs());
			
			// vary the promoter with all possible ones from the library
			for(Promoter pmVariation : library) {
					
				// copy the protein generators from the basis device
				List<ProteinGenerator> newPGs = new ArrayList<ProteinGenerator>();
				newPGs.addAll(pgs);
				
				// create the varied protein generator
				ProteinGenerator variation = new ProteinGenerator(pmVariation,rbs,pc,t);
				
				// replace the varied protein generator
				newPGs.set(i, variation);
				
				// build device with it
				String name = basis.getName();
				Set<EnvironmentalSignal> sms = basis.getSignals();
				Device device = new Device(name, newPGs, sms);
				
				// add it to the resulting list of devices
				result.add(device);
			}
			// vary the rbs's
			for(RBS rbsVariation : pr.getAllRBS()) {
				
				// copy the protein generators from the basis device
				List<ProteinGenerator> newPGs = new ArrayList<ProteinGenerator>();
				newPGs.addAll(pgs);
				
				// create the varied protein generator
				ProteinGenerator variation = new ProteinGenerator(pm,rbsVariation,pc,t);
				
				// replace the varied protein generator
				newPGs.set(i, variation);
				
				// build device with it
				String name = basis.getName();
				Set<EnvironmentalSignal> sms = basis.getSignals();
				Device device = new Device(name, newPGs, sms);
				
				// add it to the resulting list of devices
				result.add(device);
			}
		}
		return result;
	}
	
	// recursive!!!
	private void pgCombinationsRec(List<List<ProteinGenerator>> pgsPerVertice, 
			List<ProteinGenerator> current, int vertice, 
			List<List<ProteinGenerator>> result) {
		// stop condition
		if(vertice == geneNetworkTemplate.getVertices().size()) {
			List<ProteinGenerator> pgCombi = new ArrayList<ProteinGenerator>();
			for(int i = 0; i < current.size(); i++) {
				pgCombi.add(current.get(i));
			}
			result.add(pgCombi);
			return;
		}
		// list with possible proteins for edge with edgeIndex
		List<ProteinGenerator> pgs = pgsPerVertice.get(vertice);
		// iterate over the possible protein generators
		for(int pgIndex = 0; pgIndex < pgs.size(); pgIndex++) {
			current.set(vertice, pgs.get(pgIndex));
			pgCombinationsRec(pgsPerVertice, current, vertice+1, result);
		}
		// backtracking step, reset the mapping for the current edge.
		current.set(vertice, null);
	}
	
	/**
	 * A depth first search for all possible wirings of the gene network
	 * @return
	 */
	private List<Map<Edge,Protein>> allPossibleWirings() {
		
		// convert to list of lists
		int numWires = geneNetworkTemplate.getEdges().size();
		List<List<Protein>> input = new ArrayList<List<Protein>>();
		for(int i = 0; i < numWires; i++) {
			input.add(possibleProteinsPerEdge.get(geneNetworkTemplate.getEdges().get(i)));
		}
		
		// init current mapping with all edge mapping to null
		List<Protein> current = new ArrayList<Protein>(input.size());
		for(int i = 0; i < input.size(); i++) {
			current.add(null);
		}
		
		// init result list of mappings
		List<List<Protein>> mappings = new ArrayList<List<Protein>>();
		
		// fill the result list
		allPossibleWiringsRec(input, current, 0, mappings);
		
		// convert the list to mapping
		List<Map<Edge,Protein>> possibleWirings = new ArrayList<Map<Edge,Protein>>();
		for(List<Protein> mapping : mappings) {
			Map<Edge,Protein> map = new HashMap<Edge,Protein>();
			for(int i = 0; i < numWires; i++) {
				map.put(geneNetworkTemplate.getEdges().get(i),mapping.get(i));
			}
			possibleWirings.add(map);
		}
		return possibleWirings;
	}
	
	// recursive!!!
	private void allPossibleWiringsRec(List<List<Protein>> input, List<Protein> current, int edgeIndex, List<List<Protein>> mappings) {
		
		// stop condition, bottom of the tree reached, a mapping found
		if(edgeIndex == input.size()) {
			//System.out.println(current.toString());
			List<Protein> mapping = new ArrayList<Protein>();
			for(int i = 0; i < current.size(); i++) {
				mapping.add(current.get(i));
			}
			mappings.add(mapping);
			return;
		}

		// list with possible proteins for edge with edgeIndex
		List<Protein> proteins = input.get(edgeIndex);
		
		// iterate over the possible proteins
		for(int proteinIndex = 0; proteinIndex < proteins.size(); proteinIndex++) {
			// get current protein
			Protein protein = input.get(edgeIndex).get(proteinIndex);
			// if not already used for the mapping (prevent cross talk)
			if(!current.contains(protein)) {
				// map the protein to the edge
				current.set(edgeIndex, protein);
				// if the mapping is valid (there are promoters available that
				// can implement this mapping)
				if(checkMappingList(current)) {
					// go to the next edge
					allPossibleWiringsRec(input, current, edgeIndex+1, mappings);
				}
			}
		}
		// backtracking step, reset the mapping for the current edge.
		current.set(edgeIndex, null);
	}
	
	private Map<Edge,Protein> randomWiring() {
		// shuffle the possible proteins per edge
		for(List<Protein> proteins : possibleProteinsPerEdge.values()) {
			Collections.shuffle(proteins);
		}
		return firstPossibleWiring();
	}
	
	private Map<Edge,Protein> firstPossibleWiring() {
		
		// convert to list of lists
		int numWires = geneNetworkTemplate.getEdges().size();
		List<List<Protein>> input = new ArrayList<List<Protein>>();
		for(int i = 0; i < numWires; i++) {
			input.add(possibleProteinsPerEdge.get(geneNetworkTemplate.getEdges().get(i)));
		}
		
		// init current mapping with all edge mapping to null
		List<Protein> current = new ArrayList<Protein>(input.size());
		for(int i = 0; i < input.size(); i++) {
			current.add(null);
		}
		
		// fill current with first mapping found
		firstPossibleWiringRec(input, current, 0);
		
		// convert the list to mapping
		Map<Edge,Protein> map = new HashMap<Edge,Protein>();
		for(int i = 0; i < numWires; i++) {
			map.put(geneNetworkTemplate.getEdges().get(i),current.get(i));
		}
		
		return map;
	}
	
	private boolean firstPossibleWiringRec(List<List<Protein>> input, List<Protein> current, int edgeIndex) {
		
		// stop condition, bottom of the tree reached, a mapping found
		if(edgeIndex == input.size()) {
			return true;
		}

		// list with possible proteins for edge with edgeIndex
		List<Protein> proteins = input.get(edgeIndex);
		
		// iterate over the possible proteins
		for(int proteinIndex = 0; proteinIndex < proteins.size(); proteinIndex++) {
			// get current protein
			Protein protein = input.get(edgeIndex).get(proteinIndex);
			// if not already used for the mapping (prevent cross talk)
			if(!current.contains(protein)) {
				// map the protein to the edge
				current.set(edgeIndex, protein);
				// if the mapping is valid (there are promoters available that
				// can implement this mapping)
				if(checkMappingList(current)) {
					// go to the next edge
					boolean found = firstPossibleWiringRec(input, current, edgeIndex+1);
					if(found) {
						return true;
					}
				}
			}
		}
		// backtracking step, reset the mapping for the current edge.
		current.set(edgeIndex, null);
		return false;
	}
	
	/**
	 * Stores which transcription factors can be used for each
	 * edge in the gene network template.
	 */
	private void initPossibleProteinsPerEdge() {
		possibleProteinsPerEdge = new HashMap<Edge,List<Protein>>();
		// all input edges (all regulated by a signal)
		for(String id : inputMapping.keySet()) {
			List<Protein> proteins = new ArrayList<Protein>();
			Edge edge = geneNetworkTemplate.getInput(id);
			Protein protein = inputMapping.get(id);
			proteins.add(protein);
			possibleProteinsPerEdge.put(edge, proteins);
		}
		// all unregulated output edges
		for(String id : outputMapping.keySet()) {
			Edge edge = geneNetworkTemplate.getOutput(id);
			if(edge.getType() == null || edge.getType().equals("0")) {
				List<Protein> proteins = new ArrayList<Protein>();
				Protein protein = outputMapping.get(id);
				proteins.add(protein);
				possibleProteinsPerEdge.put(edge, proteins);
			}
		}
		for(Edge edge : geneNetworkTemplate.getEdges()) {
			List<Protein> proteins = new ArrayList<Protein>();
			// input edges
			if(edge.isRegulatedEdge()) {
				// do nothing allready done
			}
			// these should be reporters in outputMapping
			else if(edge.getType() == null || edge.getType().equals("0")) {
				// do nothing, already done
			}
			// all inhibitor edges
			else if(edge.getType().equals("-")) {
				List<String> names = Inhibitor.emptyInstance().getAllNames();
				for(String name : names) {
					Inhibitor inhibitor = new Inhibitor(name);
					// leave out tfs that need to be activated
					if(!inhibitor.activated()) {
						proteins.add(inhibitor);
					}
				}
				possibleProteinsPerEdge.put(edge, proteins);
			}
			// all activator edges
			else if(edge.getType().equals("+")) {
				List<String> names = Activator.emptyInstance().getAllNames();
				for(String name : names) {
					Activator activator = new Activator(name);
					// leave out tfs that need to be activated
					if(!activator.activated()) {
						proteins.add(new Activator(name));
					}
				}
				possibleProteinsPerEdge.put(edge, proteins);
			}
			else {
				// should not get to here, add assert
			}
		}
	}
	
	private boolean checkMappingList(List<Protein> mappingList) {
		// for each vertice
		for(Vertice vertice : geneNetworkTemplate.getVertices()) {
			// get all TFs that must bind to the promoter
			List<TF> tfs = new ArrayList<TF>();
			for(Edge edge : vertice.getInput()) {
				TF tf = (TF)mappingList.get(geneNetworkTemplate.getEdges().indexOf(edge));
				//TF tf = (TF)mapping.get(edge);
				if(tf != null) {
					tfs.add(tf);
				}
			}
			// get the regulation pattern
			String pattern = vertice.regulationPattern();
			// return false if there is no suitable promoter library available
			if(Promoter.allPromoters(pr.getOnePromoterPerLibrary(), pattern, tfs).isEmpty()) {
				return false;
			}
		}
		return true;
	}
	
	// not used
	public boolean mutateRBS(Device device) {
		// pick a random protein generator
		ProteinGenerator pg = device.getProteinGenerators().get(rnd.nextInt(device.getNumProteinGenerators()));
		// store old rbs
		RBS oldRBS = pg.getRBS();
		// replace rbs with a random one from all rbss (could be the same...)
		RBS newRBS = pr.getAllRBS().get(rnd.nextInt(pr.getAllRBS().size()));
		pg.setRBS(newRBS);
		return !oldRBS.equals(newRBS);
	}
	
	// not used
	public boolean mutatePromoterStrength(Device device) {
		// pich a random protein generator
		ProteinGenerator pg = device.getProteinGenerators().get(rnd.nextInt(device.getNumProteinGenerators()));
		// store old promoter
		Promoter oldPromoter = pg.getPromoter();
		// replace promoter with a random one from the same library (could be the same...)
		List<Promoter> library = pr.getPromoterLibrary(pg.getPromoter().getTFs());
		Promoter newPromoter = library.get(rnd.nextInt(library.size()));
		pg.setPromoter(newPromoter);
		return !oldPromoter.equals(newPromoter);
	}
	
	// not used
	public boolean mutateTF(Device device) {
		
		// get the list of protein generators
		List<ProteinGenerator> pgs = device.getProteinGenerators();
		
		// get all used TFs
		Set<Protein> usedTFs = new HashSet<Protein>();
		device.getTFMonomers(usedTFs);
		
		// pick a random TF out of the used ones (the one to be mutated)
		TF oldTF = (TF)usedTFs.toArray()[(rnd.nextInt(usedTFs.size()))];
		
		// pick a new TF which should replace it
		TF newTF = null;
		if(oldTF instanceof Inhibitor) {
			int rndIndex = rnd.nextInt(pr.getAllActiveInhibitors().size());
			newTF = pr.getAllActiveInhibitors().get(rndIndex);
		}
		else { // oldTF instanceof Activator
			int rndIndex = rnd.nextInt(pr.getAllActiveActivators().size());
			newTF = pr.getAllActiveActivators().get(rndIndex);
		}
		
		// check if we didn't pick the same or an already used TF
		if(oldTF.equals(newTF) || usedTFs.contains(newTF)) {
			return false;
		}
		
		// get possible promoter libraries per promoter that binds the TF that 
		// we want to mutate
		Map<ProteinGenerator,List<Promoter>> posPmPerPG = 
			new HashMap<ProteinGenerator,List<Promoter>>();
		for(ProteinGenerator pg : pgs) {
			Promoter pm = pg.getPromoter();
			String pattern = pm.regulationPattern();
			List<TF> tfs = pm.getTFs();
			if(tfs.contains(oldTF)) {
				List<TF> newTFs = new ArrayList<TF>(tfs);
				newTFs.remove(oldTF);
				newTFs.add(newTF);
				posPmPerPG.put(pg, Promoter.allPromoters(pr.getOnePromoterPerLibrary(), pattern, newTFs));
			}
		}
		
		// check if promoters are available for the desired mutation
		for(List<Promoter> pms : posPmPerPG.values()) {
			if(pms.isEmpty()) {
				return false;
			}
		}
		
		// at this point we should be sure that the mutation can be performed...
		
		// replace promoters
		for(ProteinGenerator pg : posPmPerPG.keySet()) {
			// pick random library (out of the possible ones)
			List<Promoter> pms = posPmPerPG.get(pg);
			Promoter pm = pms.get(rnd.nextInt(pms.size()));
			List<Promoter> library = pr.getPromoterLibrary(pm.getTFs());
			// pick random promoter out of the library
			Promoter newPromoter = library.get(rnd.nextInt(library.size()));
			// set the new promoter
			pg.setPromoter(newPromoter);
		}
		
		// have all protein coding parts that produce the old TF to produce 
		// the new TF
		ProteinCoding newPC = ProteinCoding.getProteinCoding(newTF, pr.getAllPC());
		for(ProteinGenerator pg : pgs) {
			ProteinCoding pc = pg.getProteinCoding();
			if(pc.getProtein().equals(oldTF)) {
				pg.setProteinCoding(newPC);
			}
		}
		return true;
	}

	private void check(){
		// check if a device with this name already exists
		if(Device.emptyInstance().getAllNames().contains(name)) {
			System.out.println("A device with the name " + name + " already exists");
			System.exit(-1);
		}
		
		// check if the number of outputs correspond
		if(geneNetworkTemplate.numOutputs() != outputMapping.size()) {
			System.out.println("Different number of outputs.");
			System.exit(-1);
		}
		
		// check if the number of inputs correspond
		if(geneNetworkTemplate.numInputs() != inputMapping.size()) {
			System.out.println("Different number of inputs.");
			System.exit(-1);
		}
		
		// TODO: check if the outputs are defined correctly
		
		// check if the inputs are defined correctly
		for(String key : inputMapping.keySet()) {
			TF tf = inputMapping.get(key);
			if(!tf.bindsSmallMolecule()) {
				System.out.println("Wrong input specified.");
				System.exit(-1);
			}
		}
	}
}
