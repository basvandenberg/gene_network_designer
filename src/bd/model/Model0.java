package bd.model;

import fern.network.fernml.FernMLNetwork;
import fern.network.sbml.SBMLNetwork;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.jdom.JDOMException;

import bd.biopart.Device;
import bd.biopart.EnvironmentalSignal;
import bd.biopart.Operator;
import bd.biopart.Protein;
import bd.biopart.ProteinGenerator;
import bd.biopart.TF;
import bd.global.Constants;

public class Model0 extends Model {

	private final String EMPTY_SET = "empty_set";
	private final int NUM_PLASMIDS = 1;
	
	public Model0(Device device) {
		
		super(device);
		
		// Add protein generators
		int counter = 0;
		for(ProteinGenerator pg : device.getProteinGenerators()) {
			addProteinGenerator(pg, device.getSignals(), counter);
			counter++;
		}
		
		// Add all proteins;
		addProteins();
		
		// Add signals
		if(device.getSignals() != null) {
			addSignals();
		}
		
		// Add species: empty set
		speciesList.add(new Species(EMPTY_SET,0));
	}
	
	private void addProteins() {
		
		// Create a set of proteins
		HashSet<Protein> proteinSet = new HashSet<Protein>();
		// Fill the set with all TF oligomer proteins
		device.getAllProteins(proteinSet);
		
		for(Protein protein : proteinSet) {
			
			// Add species: protein
			speciesList.add(new Species(protein.getName(), 0));
			
			Reaction r;
			
			if(!protein.isMonomer()) {
				// Add reaction: protein association
				r = new Reaction(protein.getKBindProtein());
				r.setReactants(protein.getP1().getName(), protein.getP2().getName());
				r.setProducts(protein.getName());
				reactionList.add(r);
				// Add reaction: protein dissociation
				r = new Reaction(protein.getKUnbindProtein());
				r.setReactants(protein.getName());
				r.setProducts(protein.getP1().getName(), protein.getP2().getName());
				reactionList.add(r);
			}
			
			// Add reaction: protein degradation
			r = new Reaction(protein.getKDegProtein());
			r.setReactants(protein.getName());
			r.setProducts(EMPTY_SET);
			reactionList.add(r);
		}
	}
	
	private void addProteinGenerator(ProteinGenerator pg, Set<EnvironmentalSignal> signals, int counter) {
		
		addPromoter(pg, signals, counter);
		addRBS(pg, counter);
		addProteinCoding(pg, counter);
		//addTerminator(pg);		
	}
	
	private void addPromoter(ProteinGenerator pg, Set<EnvironmentalSignal> signals, int counter) {
		
		// number of operators
		int numOperators = pg.getPromoter().getOperators().size();
		// number of possible operator occupations
		int numOccupations = (int)Math.pow(2, numOperators);
		
		Reaction r;
		
		for(int i = 0; i < numOccupations; i++) {
			
			// occupation of the operator (TFs bound to them)
			String occupation = occupation(i, numOperators);
			String name = "pg" + counter + "_gene_" + occupation;
			
			// add species: gene
			speciesList.add(new Species(name, i == 0 ? NUM_PLASMIDS : 0));
			
			// add reaction: transcription
			double ktc;
			if(numOperators == 0) {
				ktc = pg.getPromoter().getKTranscription();
				
			}
			else {
				ktc = pg.getPromoter().getKTranscription(occupation);
			}
			if(ktc > 0) {
				r = new Reaction(ktc);
				r.setReactants(name);
				r.setProducts(name, "pg" + counter + "_mRNA");
				reactionList.add(r);
			}
		}
			
		// TF binding to operators
		for(int i = 0; i < numOccupations; i++) {
			for(int j = 0; j < numOccupations; j++) {
				if(neighbors(i, j, numOperators) && j < i) { // only one direction
					String oldO = occupation(j,numOperators);
					String newO = occupation(i,numOperators);
					Operator operator = pg.getPromoter().getOperators().get(whichOperator(oldO,newO));
					
					// Check if we have an activating signal
					boolean activated = operator.getTF().signalReceiver() && !operator.getTF().bindsSmallMolecule();
					// Add signal to TF name in case of activated TF
					String tfName = operator.getTF().getName();
					if(activated) {
						 tfName += "_" + operator.getTF().getSmallMolecule().getName();
					}
					
					// add reaction: binding TF to DNA
					r = new Reaction(operator.getKBindTF());
					r.setReactants("pg" + counter + "_gene_" + oldO, tfName);
					r.setProducts("pg" + counter + "_gene_" + newO);
					reactionList.add(r);
					
					// add reaction: TF dissociation from DNA
					r = new Reaction(operator.getKUnbindTF());
					r.setReactants("pg" + counter + "_gene_" + newO);
					r.setProducts("pg" + counter + "_gene_" + oldO, tfName);
					reactionList.add(r);
					
					// add reaction: bound TF degradation (similar to dissociation)
					r = new Reaction(operator.getTF().getKDegProtein());
					r.setReactants("pg" + counter + "_gene_" + newO);
					if(activated) {
						// signal does not degrade!
						r.setProducts("pg" + counter + "_gene_" + oldO, operator.getTF().getSmallMolecule().getName() , EMPTY_SET);
					}
					else {
						r.setProducts("pg" + counter + "_gene_" + oldO, EMPTY_SET);
					}
					reactionList.add(r);
					
					// add reaction: TF induction by signal (only when signal is present in device)
					if(operator.getTF().signalReceiver() && 
							signals.contains(operator.getTF().getSmallMolecule()) &&
							operator.getTF().bindsSmallMolecule()) {
						r = new Reaction(operator.getTF().getKBindSignal());
						r.setReactants("pg" + counter + "_gene_" + newO, operator.getTF().getSmallMolecule().getName());
						r.setProducts("pg" + counter + "_gene_" + oldO, operator.getTF().getName()+"_"+operator.getTF().getSmallMolecule().getName());
						reactionList.add(r);
					}
				}
			}
		}
	}
	
	private void addRBS(ProteinGenerator pg, int counter) {
		
		// add species: mRNA
		speciesList.add(new Species("pg" + counter + "_mRNA", 0));
		
		// add reaction: translation (mRNA -> protein)
		Reaction r = new Reaction(pg.getRBS().getKTranslation());
		r.setReactants("pg" + counter + "_mRNA");
		r.setProducts("pg" + counter + "_mRNA", pg.getProteinCoding().getProtein().getName());
		reactionList.add(r);
	}
	
	private void addProteinCoding(ProteinGenerator pg, int counter) {
		
		// add reaction: mRNA degradation
		Reaction r = new Reaction(pg.getProteinCoding().getKDegMrna());
		r.setReactants("pg" + counter + "_mRNA");
		r.setProducts(EMPTY_SET);
		reactionList.add(r);
	}
	
	private void addSignals() {
	
		for(EnvironmentalSignal signal : device.getSignals()) {
				
			// add species: signal
			speciesList.add(new Species(signal.getName(), 0));
			
			Set<TF> signalTFs = device.getSignalTFs(signal);
			for(TF tf : signalTFs) {
				
				// add species: TF signal complex
				speciesList.add(new Species(tf.getName()+"_"+signal.getName(), 0));
				
				// add reaction: signal (free)TF binding
				Reaction r = new Reaction(tf.getKBindSignal());
				r.setReactants(tf.getName(), signal.getName());
				r.setProducts(tf.getName() + "_" + signal.getName());
				reactionList.add(r);

				// add reaction: dissociate inducer from TF 
				if(tf.getKUnbindSignal() > 0) {
					r = new Reaction(tf.getKUnbindSignal());
					r.setReactants(tf.getName() + "_" + signal.getName());
					r.setProducts(tf.getName(), signal.getName());
					reactionList.add(r);
				}
				
				// add reaction: degradation free TF-signal complex
				r = new Reaction(tf.getKDegProtein());
				r.setReactants(tf.getName() + "_" + signal.getName());
				r.setProducts(signal.getName(), EMPTY_SET);
				reactionList.add(r);
			}
		}
	}
	
	/**
	 * 
	 * @param i
	 * @param numOperators
	 * @return
	 */
	private String occupation(int i, int numOperators) {
		String result = Integer.toBinaryString(i);
		int extraLength = numOperators - result.length();
		for(int j = 0; j < extraLength; j++) {
			result = "0" + result;
		}
		return result;
	}
	
	/**
	 * 
	 * @param i
	 * @param j
	 * @param numOperators
	 * @return
	 */
	private boolean neighbors(int i, int j, int numOperators) {
		String bin1 = occupation(i, numOperators);
		String bin2 = occupation(j, numOperators);
		int distance = 0;
		for(int k = 0; k < numOperators; k++) {
			if(bin1.charAt(k) != bin2.charAt(k)) {
				distance++;
			}
		}
		return distance == 1;
	}
	
	/**
	 * 
	 * @param oc1
	 * @param oc2
	 * @return
	 */
	private int whichOperator(String oc1, String oc2) {
		for(int i = 0; i < oc1.length(); i++) {
			if(oc1.charAt(i) != oc2.charAt(i)) {
				return i;
			}
		}
		return -1;
	}
	
	public void toFernMl() {
		new FernMLWriter().writeModel(this);
	}
	
	public void toSBML() throws IOException, JDOMException {	
		this.toFernMl();
		FernMLNetwork fernNetwork = new FernMLNetwork(new File(Constants.TMP_MODEL_DIR + device.getName() + ".fernml"));
		SBMLNetwork sbmlNetwork = new SBMLNetwork(fernNetwork);
		sbmlNetwork.saveToFile(new File(Constants.TMP_MODEL_DIR + device.getName() + ".sbml"));
	}
}
