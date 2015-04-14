package bd.biopart;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that loads the whole database into memory so that it can be used
 * in a fast way. All xml files containing the data are read and stored into 
 * database item objects.
 * Uses the singleton pattern, so that the database will only be loaded once.
 * @author Bastiaan van den Berg
 *
 */
/**
 * @author bastiaan
 *
 */
/**
 * @author bastiaan
 *
 */
/**
 * @author bastiaan
 *
 */
public class BioPartDatabase {

	// singleton design pattern
	private static BioPartDatabase instance = null;
	
	// subdivided into libraries
	private List<List<Promoter>> allPromoters;
	private List<Promoter> onePromoterPerLibrary;
	private List<ProteinCoding> allPC;
	private List<RBS> allRBS;
	private List<Terminator> allTerminators;
	private List<Inhibitor> allActiveInhibitors;
	private List<Activator> allActiveActivators;
	
	/**
	 * Load all database items, which means that all xml files must be read.
	 */
	private BioPartDatabase() {
		// load the registry...
		System.out.print("\n- Loading bioparts database, this may take a while...");
		loadPromoters();
		loadRBS();
		loadPC();
		loadTerminators();
		loadActiveInhibitors();
		loadActiveActivators();
		System.out.println(", done.");
	}
	
	/**
	 * This function loads the bioparts database when this is not done yet,
	 * otherwise it simply returns the allready loaded database.
	 * @return The bioparts database
	 */
	public static BioPartDatabase instance() {
		if (instance == null) {
			instance = new BioPartDatabase();
		}
		return instance;
	}
	
	/**
	 * This function returns a list with promoters that bind all the transcription
	 * factors given in tfs. Returns null if there is no promoter that bind all
	 * TFs.
	 * @param tfs A list of transcription factors.
	 * @return A list of promoters.
	 */
	public List<Promoter> getPromoterLibrary(List<TF> tfs) {
		for(List<Promoter> library : allPromoters) {
			if(library.get(0).getTFs().containsAll(tfs) &&
					tfs.containsAll(library.get(0).getTFs())) {
				return library;
			}
		}
		return null;
	}
	
	/**
	 * This method returns all promoters in the database.
	 * @return A list with promoters.
	 */
	public List<List<Promoter>> getAllPromoters() {
		return allPromoters;
	}

	/**
	 * This function returns one promoter per promoter library.
	 * @return List with promoters.
	 */
	public List<Promoter> getOnePromoterPerLibrary() {
		return onePromoterPerLibrary;
	}

	/**
	 * This function returns all protein coding parts.
	 * @return List with protein coding parts.
	 */
	public List<ProteinCoding> getAllPC() {
		return allPC;
	}

	/**
	 * This function returns all ribosome binding sites.
	 * @return List with ribosome bining sites.
	 */
	public List<RBS> getAllRBS() {
		return allRBS;
	}

	/**
	 * This function returns all terminators.
	 * @return List with terminators.
	 */
	public List<Terminator> getAllTerminators() {
		return allTerminators;
	}

	/**
	 * This function returns all active inhibitors. An active
	 * inhibitor is an inibitor that does not has to be activated
	 * by an enviromental signal.
	 * @return List with inhibitors.
	 */
	public List<Inhibitor> getAllActiveInhibitors() {
		return allActiveInhibitors;
	}

	/**
	 * This function returns all active activators. An active 
	 * activator is an activator that does not has to be activated
	 * by an environmental signal.
	 * @return List with activators.
	 */
	public List<Activator> getAllActiveActivators() {
		return allActiveActivators;
	}

	private void loadPromoters() {
		List<String> names = Promoter.emptyInstance().getAllNames();
		allPromoters = new ArrayList<List<Promoter>>();
		for(String n : names) {
			Promoter pm = new Promoter(n);
			boolean libAvailable = false;
			// add promoter to library if it already exists
			for(List<Promoter> library : allPromoters) {
				if(library.get(0).sameLibrary(pm)) {
					libAvailable = true;
					library.add(pm);
					break;
				}
			}
			// create new library (list of promoters) if it does not exist yet.
			if(!libAvailable) {
				ArrayList<Promoter> library = new ArrayList<Promoter>();
				library.add(pm);
				allPromoters.add(library);
			}
		}
		
		// fill onePromoterPerLibrary (simply one promoter per library...)
		onePromoterPerLibrary = new ArrayList<Promoter>();
		for(List<Promoter> library : allPromoters) {
			// just pick the first out of the library
			onePromoterPerLibrary.add(library.get(0));
		}
	}
	
	private void loadRBS() {
		List<String> names = RBS.emptyInstance().getAllNames();
		allRBS = new ArrayList<RBS>(names.size());
		for(String name : names) {
			allRBS.add(new RBS(name));
		}
	}
	
	private void loadPC() {
		List<String> names = ProteinCoding.emptyInstance().getAllNames();
		allPC = new ArrayList<ProteinCoding>(names.size());
		for(String name : names) {
			allPC.add(new ProteinCoding(name));
		}
	}
	
	private void loadTerminators() {
		List<String> names = Terminator.emptyInstance().getAllNames();
		allTerminators = new ArrayList<Terminator>(names.size());
		for(String name : names) {
			allTerminators.add(new Terminator(name));
		}
	}
	
	private void loadActiveInhibitors() {
		List<String> names = Inhibitor.emptyInstance().getAllNames();
		allActiveInhibitors = new ArrayList<Inhibitor>();
		for(String name : names) {
			Inhibitor inhibitor = new Inhibitor(name);
			if(!inhibitor.activated()) {
				allActiveInhibitors.add(inhibitor);
			}
		}
	}
	
	private void loadActiveActivators() {
		List<String> names = Activator.emptyInstance().getAllNames();
		allActiveActivators = new ArrayList<Activator>();
		for(String name : names) {
			Activator activator = new Activator(name);
			if(!activator.activated()) {
				allActiveActivators.add(activator);
			}
		}
	}
	
	public void destroy() {
    	instance = null;
    }
}
