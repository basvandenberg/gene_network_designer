package bd.biopart;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;

import bd.global.Constants;

/**
 * A device is a list of protein generators and a list of environmental signals.
 * @author Bastiaan van den Berg
 */
public class Device extends AbstractDatabaseItem {
	
	/**
	 * List with protein generators.
	 */
	protected List<ProteinGenerator> proteinGenerators;
	/**
	 * List with environmental signals.
	 */
	protected Set<EnvironmentalSignal> environmentalSignals;

	public static final String NL = System.getProperty("line.separator");
	
	/**
	 * Construct a device, reads all data from the database.
	 * @param name The name of the device.
	 */
	public Device(String name) {
		super(name);
	}
	
	/**
	 * Manully construct a device, not read from the database.
	 * @param name The name of the device.
	 * @param proteinGenerators List with protein generators.
	 * @param environmentalSignals List with environmental signals.
	 */
	public Device(String name, List<ProteinGenerator> proteinGenerators, 
			Set<EnvironmentalSignal> environmentalSignals) {
		super(name);
		if(getAllNames().contains(name)) {
			System.out.println("Error: This device already exists: " + name);
			System.exit(-1);
		}
		else {
			this.proteinGenerators = proteinGenerators;
			this.environmentalSignals = environmentalSignals;
		}
	}
	
	/* (non-Javadoc)
	 * @see gnd.biopart.AbstractDatabaseItem#init(org.jdom.Document)
	 */
	protected void init(Document document) {
		super.init(document);
		addSmallMolecules(document);
		addProteinGenerators(document);
		addSubdevices(document);
	}
	
	private void addSmallMolecules(Document document) {
		if(environmentalSignals == null) {
			environmentalSignals = new HashSet<EnvironmentalSignal>();
		}
		List list = document.getRootElement().getChildren("small_molecule");
		for(Object o : list) {
			Element element = (Element)o;
			environmentalSignals.add(new EnvironmentalSignal(element.getAttributeValue("name")));
		}
	}
	
	private void addSubdevices(Document document) {
		List list = document.getRootElement().getChildren("subdevice");
		for(Object o : list) {
			Element element = (Element)o;
			addSubdevice(element);
		}
	}
	
	// recursive!!!
	private void addSubdevice(Element dev) {
		Device device = new Device(dev.getAttributeValue("name"));
		this.proteinGenerators.addAll(device.proteinGenerators);
		if(device.environmentalSignals != null) {
			this.environmentalSignals.addAll(device.environmentalSignals);
		}
	}

	private void addProteinGenerators(Document document) {
		if(proteinGenerators == null) {
			proteinGenerators = new ArrayList<ProteinGenerator>();
		}
		List list = document.getRootElement().getChildren("protein_generator");
		for(Object o : list) {
			Element element = (Element)o;
			addProteinGenerator(element);
		}
	}

	private void addProteinGenerator(Element pg) {
		Promoter promoter = new Promoter(pg.getChildTextTrim("promoter"));
		RBS rbs = new RBS(pg.getChildTextTrim("rbs"));
		ProteinCoding pc = new ProteinCoding(pg.getChildTextTrim("protein_coding"));
		Terminator ter = new Terminator(pg.getChildTextTrim("terminator"));
		proteinGenerators.add(new ProteinGenerator(promoter, rbs, pc, ter));
	}
	
	/**
	 * Create an empty device instance.
	 * @return Empty Device object.
	 */
	public static Device emptyInstance() {
		return new Device("");
	}
	
	/**
	 * This function returns all proteins that are used within this device. This 
	 * means all expressed proteins, but also the dimers and tetramers they can form.
	 * It includes also the proteins that can bind any of the operators in this device.
	 * The set that is provided as argument will be filled. 
	 * @param proteins Set of proteins.
	 */
	public void getAllProteins(Set<Protein> proteins) {
		getExpressedProteins(proteins);
		getTFMonomers(proteins);
		getTFOligomers(proteins);
	}
	
	/**
	 * This function returns all proteins that are expressed by this device. So only
	 * the monomeric proteins are returned. The set that is provided as argument
	 * will be filled.
	 * @param proteins Set of proteins.
	 */
	public void getExpressedProteins(Set<Protein> proteins) {
		for(ProteinGenerator pg : proteinGenerators) {
			proteins.add(pg.getProteinCoding().getProtein());
		}
	}
	
	/**
	 * This function returns all monomeric transcription factors that are part of 
	 * a transcription factor that can bind to a operator from this device. It takes
	 * all the operators, takes all the transcription factors that bind to this operators
	 * and returns the monomers of these TFs. The set that is provided as argument
	 * will be filled.
	 * @param monomers Set of proteins.
	 */
	public void getTFMonomers(Set<Protein> monomers) {
		for(ProteinGenerator pg : proteinGenerators) {
			for(Operator operator : pg.getPromoter().getOperators()) {
				operator.getTF().getMonomers(monomers);
			}
		}
	}
	
	/**
	 * This function returns all oligomer forms (so all but the monomer) of the 
	 * transcription factors that are part of this device. The set that is
	 * provided as argument will be filled.
	 * @param oligomers Set of proteins.
	 */
	public void getTFOligomers(Set<Protein> oligomers) {
		for(ProteinGenerator pg : proteinGenerators) {
			for(Operator operator : pg.getPromoter().getOperators()) {
				operator.getTF().getOligomers(oligomers);
			}
		}
	}
	
	/**
	 * This function returns all environmental signals that bind one of the used
	 * transcription facters in this device. In other words, it returns all 
	 * environmental signals that can be used as input to this device.
	 * @return Set of environmental signals.
	 */
	public Set<EnvironmentalSignal> getPossibleSignals() {
		HashSet<EnvironmentalSignal> signals = new HashSet<EnvironmentalSignal>();
		for(ProteinGenerator pg : proteinGenerators) {
			for(Operator operator : pg.getPromoter().getOperators()) {
				if(operator.getTF().getSmallMolecule() != null) {
					signals.add(operator.getTF().getSmallMolecule());
				}
			}
		}
		return signals;
	}
	
	/**
	 * This function returns all the transcription factors that are used within this
	 * device and that bind the provided environmental signal.
	 * @param environmentalSignal An environmental signal.
	 * @return Set of transcription factors.
	 */
	public Set<TF> getSignalTFs(EnvironmentalSignal environmentalSignal) {
		HashSet<TF> tfs = new HashSet<TF>();
		for(ProteinGenerator pg : proteinGenerators) {
			for(Operator operator : pg.getPromoter().getOperators()) {
				if(environmentalSignal.equals(operator.getTF().getSmallMolecule())) {
					tfs.add(operator.getTF());
				}
			}
		}
		return tfs;
	}
	
	/**
	 * This function returns all transcription factors that bind one of the 
	 * environmental signals used in this device.
	 * @return Set of transcription factors.
	 */
	public Set<TF> getSignalTFs() {
		Set<TF> tfs = new HashSet<TF>();
		for(EnvironmentalSignal signal : environmentalSignals) {
			tfs.addAll(getSignalTFs(signal));
		}
		return tfs;
	}
	
	/**
	 * This function returns a set of environmental signals that are part of this
	 * device but do not bind any of the transcription factors. These environmental 
	 * signals are thus useless, since they cannot have any influence on the device.
	 * @return Set of environmental signals.
	 */
	public Set<EnvironmentalSignal> getIncompatibleSignals() {
		Set<EnvironmentalSignal> possibleSignals = getPossibleSignals();
		HashSet<EnvironmentalSignal> result = new HashSet<EnvironmentalSignal>();
		
		if(environmentalSignals != null) {
			for(EnvironmentalSignal signal : environmentalSignals) {
				if(!possibleSignals.contains(signal)) {
					result.add(signal);
				}
			}
		}
		return result;
	}
	
	/**
	 * This function returns all the transcription factors that bind an 
	 * operator in this device but is not being expressed in this device.
	 * @return Set of transcription factors.
	 */
	public Set<Protein> getIncompatibleProteins() {
		HashSet<Protein> tfMonomers = new HashSet<Protein>();
		getTFMonomers(tfMonomers);
		Set<Protein> expressedProteins = new HashSet<Protein>();
		getExpressedProteins(expressedProteins);
		HashSet<Protein> result = new HashSet<Protein>();
		for(Protein tf : tfMonomers) {
			if(!expressedProteins.contains(tf)) {
				result.add(tf);
			}
		}
		return result;
	}
	
	/**
	 * This function returns all the proteins that act as output
	 * of the device, i.e. the reporters.
	 * @return Set of reporter proteins.
	 */
	public Set<Protein> getOutputProteins() {
		HashSet<Protein> tfMonomers = new HashSet<Protein>();
		getTFMonomers(tfMonomers);
		HashSet<Protein> reporters = new HashSet<Protein>();
		getExpressedProteins(reporters);
		// expressed - tfMonomers
		for(Protein protein : tfMonomers) {
			reporters.remove(protein);
		}
		return reporters;
	}

	/**
	 * 
	 * @return
	 */
	public Set<Protein> getInputProteins() {
		HashSet<Protein> tfMonomers = new HashSet<Protein>();
		getTFMonomers(tfMonomers);
		HashSet<Protein> reporters = new HashSet<Protein>();
		getExpressedProteins(reporters);
		// tfMonomers - expressed
		for(Protein protein : reporters) {
			tfMonomers.remove(protein);
		}
		return tfMonomers;
	}
	
	/**
	 * This function returns all transcription factors that are used as 
	 * internal signals. 
	 * @return Set of transcription factors.
	 */
	public Set<Protein> getInternalProteins() {
		HashSet<Protein> tfMonomers = new HashSet<Protein>();
		getTFMonomers(tfMonomers);
		HashSet<Protein> reporters = new HashSet<Protein>();
		getExpressedProteins(reporters);
		// intersection tfMonomers and expressed
		HashSet<Protein> result = new HashSet<Protein>();
		for(Protein protein : tfMonomers) {
			if(reporters.contains(protein)) {
				result.add(protein);
			}
		}
		return result;
	}
	
	/**
	 * @return The number of protein generators.
	 */
	public int getNumProteinGenerators() {
		return proteinGenerators.size();
	}
	
	/**
	 * @return
	 */
	public String report() {
		
		StringBuffer sb = new StringBuffer();
		
		Set<Protein> inputs = getInputProteins();
		Set<Protein> reporters = getOutputProteins();
		Set<Protein> internal = getInternalProteins();
		
		sb.append("\n*** Report ***" + NL + NL);
		
		sb.append("#OVERVIEW" + NL);
		sb.append("Number of protein generators: " + getNumProteinGenerators() + NL);
		sb.append("Number of external signals: " + environmentalSignals.size() + NL);
		sb.append("Number of input proteins: " + inputs.size() + NL);
		sb.append("Number of internal proteins (internal signals): " + internal.size() + NL);
		sb.append("Number of output proteins (reporters): " + reporters.size() + NL);
		sb.append("\n");
		
		sb.append("#INTERFACE" + NL);
		sb.append("Input: ");
		if(inputs.isEmpty() && environmentalSignals.isEmpty()) {
			sb.append("-" + NL);
		}
		else {
			for(EnvironmentalSignal signal : environmentalSignals) {
				sb.append(signal.name.toString() + ", ");
			}
			for(Protein protein : inputs) {
				sb.append(protein.name.toString() + ", ");
			}
			sb.replace(sb.lastIndexOf(","), sb.length(), NL);
		}
		sb.append("Output: ");
		if(reporters.isEmpty()) {
			sb.append("-" + NL);
		}
		else {
			for(Protein protein : reporters) {
				sb.append(protein.name.toString() + ", ");
			}
			sb.replace(sb.lastIndexOf(","), sb.length(), NL);
		}
		sb.append("Internal: ");
		if(internal.isEmpty()) {
			sb.append("-" + NL);
		}
		else {
			for(Protein protein : internal) {
				sb.append(protein.name.toString() + ", ");
			}
			sb.replace(sb.lastIndexOf(","), sb.length(), NL);
		}
		
		sb.append(NL + "#DEVICE CHECK" + NL);
		// proteins <> TF
		Set<Protein> proteins = getIncompatibleProteins();
		sb.append("Incompatible proteins: ");
		if(!proteins.isEmpty()) {
			for(Protein protein : proteins) {
				sb.append(protein.name + ", ");
			}
			sb.replace(sb.lastIndexOf(","), sb.length(), NL);
		}
		else {
			sb.append("-" + NL);
		}
		// signals <> possible possible signals
		Set<EnvironmentalSignal> i = getIncompatibleSignals();
		sb.append("Incompatible signals: ");
		if(!i.isEmpty()) {
			for(EnvironmentalSignal inducer : i) {
				sb.append(inducer.name + ", ");
			}
			sb.replace(sb.lastIndexOf(","), sb.length(), NL);
		}
		else {
			sb.append("-" + NL);
		}
		
		sb.append("\n*** End report ***" + NL);
		
		return sb.toString();
	}
	
	/**
	 * @return
	 */
	public static String[] getAllDeviceNames() {
		File file = new File(Constants.DEVICE_DIR);
		String[] files = file.list();
		for(int i = 0; i < files.length; i++) {
			int index = files[i].lastIndexOf('.');
			files[i] = files[i].substring(0,index);
		}
		Arrays.sort(files);
		return files;
	}
	
	/* (non-Javadoc)
	 * @see gnd.biopart.AbstractDatabaseItem#getDocument()
	 */
	protected Document getDocument() {
		Document document = createDocument();
		Element root = new Element("device");
		root.setAttribute("name", name);
		document.setRootElement(root);
		
		for(ProteinGenerator pg : proteinGenerators) {
			Element pge = new Element("protein_generator");
			Element pm = new Element("promoter");
			pm.setText(pg.getPromoter().getName());
			Element rbs = new Element("rbs");
			rbs.setText(pg.getRBS().getName());
			Element pc = new Element("protein_coding");
			pc.setText(pg.getProteinCoding().getName());
			Element t = new Element("terminator");
			t.setText(pg.getTerminator().getName());
			pge.addContent(pm);
			pge.addContent(rbs);
			pge.addContent(pc);
			pge.addContent(t);
			root.addContent(pge);
		}
		
		for(EnvironmentalSignal sm : environmentalSignals) {
			Element sme = new Element("small_molecule");
			sme.setAttribute("name", sm.getName());
			root.addContent(sme);
		}
		
		return document;
	}
	
	/**
	 * Build device from string representation. The string representation is the
	 * same as the one returned by the toString method. 
	 * |deviceName[(pmName,rbsName,pcName,tName);(,,,);(,,,);...],{es0Name,es1Name,...}|
	 * dirty simple method...
	 * @param stringRepresentation
	 * @return
	 */
	public static Device fromStringRepresentation(String s) {
		
		// get start and end index of protein generators
		int startIndex = s.indexOf('[');
		int endIndex = s.indexOf(']');
		
		// get the device name
		String deviceName = s.substring(1, startIndex).trim();
		
		// build list with protein generators
		List<ProteinGenerator> pgs = new ArrayList<ProteinGenerator>();
		String pgsString = s.substring(startIndex+1, endIndex);
		String[] pgStrings = pgsString.split(";");
		for(String pgString : pgStrings) {
			ProteinGenerator pg = ProteinGenerator.fromStringRepresentation(pgString.trim());
			pgs.add(pg);
		}
		
		// build set with environmental signals
		Set<EnvironmentalSignal> ess = new HashSet<EnvironmentalSignal>();
		startIndex = s.indexOf('{');
		endIndex = s.indexOf('}');
		String essString = s.substring(startIndex+1, endIndex);
		String[] esStrings = essString.split(",");
		for(String esString : esStrings) {
			ess.add(new EnvironmentalSignal(esString.trim()));
		}
		
		return new Device(deviceName, pgs, ess);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String result = "|" + name + "[";
		for(ProteinGenerator pg : proteinGenerators) {
			result += pg.toString() + ";";
		}
		result = result.substring(0, result.length()-1);
		result += "],{";
		for(EnvironmentalSignal es : environmentalSignals) {
			result += es.getName() + ",";
		}
		result = result.substring(0, result.length()-1);
		result += "}|";
		return result;
	}
	
	/**
	 * @return List with all protein generators.
	 */
	public List<ProteinGenerator> getProteinGenerators() {
		return proteinGenerators;
	}
	
	/**
	 * @return Set with all environmental signals
	 */
	public Set<EnvironmentalSignal> getSignals() {
		return environmentalSignals;
	}
	
	/**
	 * Add a protein generator.
	 * @param pg Protein generator.
	 */
	public void addProteinGenerator(ProteinGenerator pg) {
		proteinGenerators.add(pg);
	}
	
	/**
	 * Add an environmental signal.
	 * @param es Environmental signal.
	 */
	public void addSmallMolecule(EnvironmentalSignal es) {
		environmentalSignals.add(es);
	}
}
