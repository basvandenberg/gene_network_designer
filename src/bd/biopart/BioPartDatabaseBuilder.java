package bd.biopart;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import bd.global.Constants;

/**
 * This class can be used to automatically build an artificial
 * bioparts database.
 * @author Bastiaan van den Berg
 *
 */
public class BioPartDatabaseBuilder {
	
	//*** Rate constants ***
	public static final double[] k_deg_protein = {0.000183, 0.00167}; // 91-22(10) min (5460-1320(600) sec) half life (k=ln(2)/t_half) grilly07 en hooshangi05
	public static final double[] k_deg_mrna = {0.00144, 0.00385}; // Bernstein02, 80% of all mRNAs had half-lives between 8-3 minutes (480-180 sec). (0.00144-0.00385) 1/sec
	public static final double[] k_bind_protein = {0.0001,0.001}; // nM^-1*sec^-1 SCHLOSSHAUER04 (Realistic protein–protein association rates) range 10^5-10^6  M^-1*sec^-1, assumption Hooshangi 1nM is 1 molecule per cell...
	public static final double k_unbind_protein = 0.0000167; // fixed, Hooshangi...
	public static final double k_bind_environmental_signal = 0.000833; // fixed Hooshangi
	public static final double k_unbind_environmental_signal = 0.00167; // fixed Hooshangi
	public static final double[] k_bind_tf = {0.00278,0.00417}; // Hooshangi +- 1 min...
	public static final double[] k_unbind_tf = {0.00139, 0.00208}; // Hooshangi +- 2 min...
	public static final double[] k_tc = {0.00625, 0.01}; // Hooshangi 0.0333, bionumbers... Not the same as Hooshangi!!! much slower, provides faster simulation, lower molecule counts....
	public static final double[] k_tl = {0.00625, 0.01}; // Hooshangi 0.0333, same as tc, does not correspond to bionumbers calculations... ()

	// rnd generator to generate random parameters or pick random proteins
	private static Random rnd;
	
	// tmp lists of proteins, used to build promoter and protein coding parts
	// so these lists need to be filled before building those parts
	private static ArrayList<Protein> iim = new ArrayList<Protein>();
	private static ArrayList<Protein> i0m = new ArrayList<Protein>();
	private static ArrayList<Protein> i0d = new ArrayList<Protein>();
	private static ArrayList<Protein> i0t = new ArrayList<Protein>();
	private static ArrayList<Protein> subm = new ArrayList<Protein>();
	private static ArrayList<Protein> reporters = new ArrayList<Protein>();
	private static ArrayList<Promoter> pmLibraries = new ArrayList<Promoter>();
	
	private static int numPromotersPerLibrary;
	private static int numRBSs;
	
	
	public static void main(String[] args) {
		
		File file = new File(Constants.DNA_DATABASE_DIR);
		System.out.println("\nThe database wil be written to " + file.getAbsolutePath());
		System.out.println("- IMPORTANT: This action will overwrite the current database!");
		
		System.out.println("\n1. Build large database");
		System.out.println("2. Build demultiplexer experiment database");
		System.out.println("3. Build DLatch experiment database");
		System.out.println();
		System.out.print("Choose the program you want to run or 'q' to quit: ");

		Scanner scanner = new Scanner(System.in);
		boolean correctChoice = false;
		
		while(correctChoice == false) {
			String choice = scanner.next().trim();
			if(choice.equals("1")) {
				BioPartDatabaseBuilder.deleteDatabase();
				BioPartDatabaseBuilder.largeDatabase();
				correctChoice = true;
			}
			else if(choice.equals("2")) {
				BioPartDatabaseBuilder.deleteDatabase();
				BioPartDatabaseBuilder.experiment0();
				correctChoice = true;
			}
			else if(choice.equals("3")) {
				BioPartDatabaseBuilder.deleteDatabase();
				BioPartDatabaseBuilder.experiment1();
				correctChoice = true;
			}
			else if(choice.equals("q")) {
				System.exit(0);
			}
			else {
				System.out.print("Incorrect input, please enter 1, 2, 3, or q: ");
			}
		}
	}	
	
	// registry to simulate all possible demultiplexer device instances
	// a small registry so that the number of possible devices is limited
	public static void experiment0() {

		// number of promoters per promoter library
		numPromotersPerLibrary = 2;
		// total number of ribosome binding sites
		numRBSs = 2;
		
		// fixed seed to get the same registry after rebuild
		rnd = new Random(1);
		
		// *** Proteins and environmental signals *** 
		inhibitoryMonomerInhibitors(2);
		monomerInhibitors(5);
		dimerInhibitors(5);
		tetramerInhibitors(5);
		reporters(2);
		
		// *** Promoters ***
		constitutivePromoters();
		singleInhibitedPromoter(iim.get(0));
		doubleInhibitedPromoter(iim.get(0), iim.get(1));
		doubleInhibitedPromoters(5, iim.get(1), i0m);
		doubleInhibitedPromoters(5, iim.get(1), i0d);
		doubleInhibitedPromoters(5, iim.get(1), i0t);
		
		// *** ribosome binding sites ***
		rbss();
		
		// *** protein coding parts ***
		proteinCoding(subm);
		proteinCoding(reporters);
		proteinCoding(iim);
		proteinCoding(i0m);
		
		// *** terminator ***
		terminator();
	}
	
	// different wires
	public static void experiment1() {
		
		// both the slow versions...
		numPromotersPerLibrary = 1;
		numRBSs = 1;
		
		// fixed seed to get the same registry after rebuild
		rnd = new Random(1);
		
		// *** Proteins and environmental signals *** 
		inhibitoryMonomerInhibitors(2);
		monomerInhibitors(6);
		dimerInhibitors(6);
		tetramerInhibitors(6);
		reporters(1);
		
		// *** Promoters ***
		constitutivePromoters();
		// pg2
		singleInhibitedPromoter(iim.get(1));
		// pg7
		singleInhibitedPromoter(i0m.get(4));
		singleInhibitedPromoter(i0d.get(4));
		singleInhibitedPromoter(i0t.get(4));
		// pg8
		singleInhibitedPromoter(i0m.get(5));
		singleInhibitedPromoter(i0d.get(5));
		singleInhibitedPromoter(i0t.get(5));
		// pg4
		doubleInhibitedPromoter(iim.get(0), iim.get(1));
		// pg3
		doubleInhibitedPromoter(iim.get(0), i0m.get(0));
		doubleInhibitedPromoter(iim.get(0), i0d.get(0));
		doubleInhibitedPromoter(iim.get(0), i0t.get(0));
		// pg5
		doubleInhibitedPromoter(i0m.get(1), i0m.get(2));
		doubleInhibitedPromoter(i0m.get(1), i0d.get(2));
		doubleInhibitedPromoter(i0m.get(1), i0t.get(2));
		doubleInhibitedPromoter(i0d.get(1), i0m.get(2));
		doubleInhibitedPromoter(i0d.get(1), i0d.get(2));
		doubleInhibitedPromoter(i0d.get(1), i0t.get(2));
		doubleInhibitedPromoter(i0t.get(1), i0m.get(2));
		doubleInhibitedPromoter(i0t.get(1), i0d.get(2));
		doubleInhibitedPromoter(i0t.get(1), i0t.get(2));
		// pg6
		doubleInhibitedPromoter(i0m.get(3), i0m.get(4));
		doubleInhibitedPromoter(i0m.get(3), i0d.get(4));
		doubleInhibitedPromoter(i0m.get(3), i0t.get(4));
		doubleInhibitedPromoter(i0d.get(3), i0m.get(4));
		doubleInhibitedPromoter(i0d.get(3), i0d.get(4));
		doubleInhibitedPromoter(i0d.get(3), i0t.get(4));
		doubleInhibitedPromoter(i0t.get(3), i0m.get(4));
		doubleInhibitedPromoter(i0t.get(3), i0d.get(4));
		doubleInhibitedPromoter(i0t.get(3), i0t.get(4));
		
		// *** ribosome binding sites ***
		rbss();
		
		// *** protein coding parts ***
		proteinCoding(subm);
		proteinCoding(reporters);
		proteinCoding(iim);
		proteinCoding(i0m);
		
		// *** terminator ***
		terminator();
	}
	
	public static void largeDatabase() {
		
		// number of promoters per promoter library
		numPromotersPerLibrary = 20;
		// total number of ribosome binding sites
		numRBSs = 20;
		
		// fixed seed to get the same registry after rebuild
		rnd = new Random(1);
		
		// *** Proteins and environmental signals *** 
		inhibitoryMonomerInhibitors(2);
		monomerInhibitors(4);
		dimerInhibitors(4);
		tetramerInhibitors(4);
		reporters(4);
		
		// *** Promoters ***
		constitutivePromoters();
		singleInhibitedPromoters(iim);
		singleInhibitedPromoters(i0m);
		singleInhibitedPromoters(i0d);
		singleInhibitedPromoters(i0t);
		// 1 m- m-
		doubleInhibitedPromoter(iim.get(0), iim.get(1));
		// m- m
		doubleInhibitedPromoters(4, iim.get(0), i0m);
		doubleInhibitedPromoters(4, iim.get(1), i0m);
		// m- d
		doubleInhibitedPromoters(4, iim.get(0), i0d);
		doubleInhibitedPromoters(4, iim.get(1), i0d);
		// m- t
		doubleInhibitedPromoters(4, iim.get(0), i0t);
		doubleInhibitedPromoters(4, iim.get(1), i0t);
		// 5 m m, 5 m d, 5 m t, 5 d d, 5 d t, 5 t t 
		doubleInhibitedPromoters(5, i0m, i0m);
		doubleInhibitedPromoters(5, i0m, i0d);
		doubleInhibitedPromoters(5, i0m, i0t);
		doubleInhibitedPromoters(5, i0d, i0d);
		doubleInhibitedPromoters(5, i0d, i0t);
		doubleInhibitedPromoters(5, i0t, i0t);
		
		// *** ribosome binding sites ***
		rbss();
		
		// *** protein coding parts ***
		proteinCoding(subm);
		proteinCoding(reporters);
		proteinCoding(iim);
		proteinCoding(i0m);
		
		// *** terminator ***
		terminator();
	}
	
	// demux
	// test case in which there is only one possible configuration/wiring
	public static void testDemultiplexer0() {
		
		// number of promoters per promoter library
		numPromotersPerLibrary = 1;
		// total number of ribosome binding sites
		numRBSs = 1;
		
		// fixed seed to get the same registry after rebuild
		rnd = new Random(1);
		
		// *** Proteins and environmental signals *** 
		inhibitoryMonomerInhibitors(2);
		monomerInhibitors(1);
		reporters(2);
		
		// *** Promoters ***
		constitutivePromoters();
		singleInhibitedPromoter(iim.get(0));
		doubleInhibitedPromoter(iim.get(0), iim.get(1));
		doubleInhibitedPromoter(iim.get(1), i0m);
		
		// *** ribosome binding sites ***
		rbss();
		
		// *** protein coding parts ***
		proteinCoding(reporters);
		proteinCoding(iim);
		proteinCoding(i0m);
		
		// *** terminator ***
		terminator();
	}
	
	// demux
	// second test case in which there are 10 possible configuration/wiring
	// there is only one wire that is not fixed, this wire can be occupied 
	// by one out of ten possible TFs i0m...i9m
	public static void testDemultiplexer1() {
		
		// number of promoters per promoter library
		numPromotersPerLibrary = 1;
		// total number of ribosome binding sites
		numRBSs = 1;
		
		// fixed seed to get the same registry after rebuild
		rnd = new Random(1);
		
		// *** Proteins and environmental signals *** 
		inhibitoryMonomerInhibitors(2);
		monomerInhibitors(10);
		reporters(2);
		
		// *** Promoters ***
		constitutivePromoters();
		singleInhibitedPromoter(iim.get(0));
		doubleInhibitedPromoter(iim.get(0), iim.get(1));
		doubleInhibitedPromoters(10,iim.get(1), i0m);
		
		// *** ribosome binding sites ***
		rbss();
		
		// *** protein coding parts ***
		proteinCoding(reporters);
		proteinCoding(iim);
		proteinCoding(i0m);
		
		// *** terminator ***
		terminator();
	}
	
	// c-element
	// test case in which 192 (8*4!) configurations are possible
	public static void testCElement0() {
		
		// number of promoters per promoter library
		numPromotersPerLibrary = 1;
		// total number of ribosome binding sites
		numRBSs = 1;
		
		// fixed seed to get the same registry after rebuild
		rnd = new Random(1);
		
		// *** Proteins and environmental signals *** 
		inhibitoryMonomerInhibitors(2);
		monomerInhibitors(10);
		reporters(1);
		
		// *** Promoters ***
		constitutivePromoters();
		singleInhibitedPromoter(i0m.get(6));
		singleInhibitedPromoter(i0m.get(7));
		singleInhibitedPromoter(i0m.get(8));
		singleInhibitedPromoter(i0m.get(9));
		doubleInhibitedPromoter(iim.get(0), iim.get(1));
		doubleInhibitedPromoter(iim.get(0), i0m.get(0));
		doubleInhibitedPromoter(iim.get(1), i0m.get(1));
		doubleInhibitedPromoter(i0m.get(2), i0m.get(3));
		doubleInhibitedPromoter(i0m.get(4), i0m.get(5));
		
		// *** ribosome binding sites ***
		rbss();
		
		// *** protein coding parts ***
		proteinCoding(reporters);
		proteinCoding(iim);
		proteinCoding(i0m);
		
		// *** terminator ***
		terminator();
	}
	
	// c-element
	// second test case in which 13440 (8*(8!/(8-4)!)) configurations are 
	// possible, through the addition of only 4 extra TFs with single promoters.
	public static void testCElement1() {

		// number of promoters per promoter library
		numPromotersPerLibrary = 1;
		// total number of ribosome binding sites
		numRBSs = 1;
		
		// fixed seed to get the same registry after rebuild
		rnd = new Random(1);
		
		// *** Proteins and environmental signals *** 
		inhibitoryMonomerInhibitors(2);
		monomerInhibitors(14);
		reporters(1);
		
		// *** Promoters ***
		constitutivePromoters();
		singleInhibitedPromoter(i0m.get(6));
		singleInhibitedPromoter(i0m.get(7));
		singleInhibitedPromoter(i0m.get(8));
		singleInhibitedPromoter(i0m.get(9));
		singleInhibitedPromoter(i0m.get(10));
		singleInhibitedPromoter(i0m.get(11));
		singleInhibitedPromoter(i0m.get(12));
		singleInhibitedPromoter(i0m.get(13));
		doubleInhibitedPromoter(iim.get(0), iim.get(1));
		doubleInhibitedPromoter(iim.get(0), i0m.get(0));
		doubleInhibitedPromoter(iim.get(1), i0m.get(1));
		doubleInhibitedPromoter(i0m.get(2), i0m.get(3));
		doubleInhibitedPromoter(i0m.get(4), i0m.get(5));
		
		// *** ribosome binding sites ***
		rbss();
		
		// *** protein coding parts ***
		proteinCoding(reporters);
		proteinCoding(iim);
		proteinCoding(i0m);
		
		// *** terminator ***
		terminator();
	}
	
	// d-latch
	// test case in which only one configuration is possible
	public static void testDLatch0() {

		// number of promoters per promoter library
		numPromotersPerLibrary = 1;
		// total number of ribosome binding sites
		numRBSs = 1;
		
		// fixed seed to get the same registry after rebuild
		rnd = new Random(1);
		
		// *** Proteins and environmental signals *** 
		inhibitoryMonomerInhibitors(2);
		monomerInhibitors(6);
		reporters(1);
		
		// *** Promoters ***
		constitutivePromoters();
		singleInhibitedPromoter(iim.get(1));
		singleInhibitedPromoter(i0m.get(4));
		singleInhibitedPromoter(i0m.get(5));
		doubleInhibitedPromoter(iim.get(0), iim.get(1));
		doubleInhibitedPromoter(iim.get(0), i0m.get(0));
		doubleInhibitedPromoter(i0m.get(1), i0m.get(2));
		doubleInhibitedPromoter(i0m.get(3), i0m.get(4));
		
		// *** ribosome binding sites ***
		rbss();
		
		// *** protein coding parts ***
		proteinCoding(reporters);
		proteinCoding(iim);
		proteinCoding(i0m);
		
		// *** terminator ***
		terminator();
	}
	
	/**
	 * creates environmental signals that inhibit a monomer TF  
	 * @param numSignals
	 */
	private static void inhibitoryMonomerInhibitors(int amount) {
		for(int i = 0; i < amount; i++) {
			// create inhibiting environmental signal
			String esName = "es_iim" + i;
			boolean inhibitor = true;
			EnvironmentalSignal es = new EnvironmentalSignal(esName, inhibitor);
			es.writeDocument();
			// create TF (inhibitor) to which the environmental signal can bind
			String inhName = "iim" + i;
			Inhibitor inh = new Inhibitor(inhName, randomParam(k_deg_protein,rnd), null, null, 0.0, 0.0, es, k_bind_environmental_signal, k_unbind_environmental_signal);
			iim.add(inh);
			inh.writeDocument();
		}
	}
	
	private static void monomerInhibitors(int amount) {
		for(int i = 0; i < amount; i++) {
			String name = "i0m" + i;
			Inhibitor inh = new Inhibitor(name, randomParam(k_deg_protein,rnd), null, null, 0.0, 0.0, null, 0.0, 0.0);
			i0m.add(inh);
			inh.writeDocument();
		}
	}
	
	private static void dimerInhibitors(int amount) {
		for(int i = 0; i < amount; i++) {
			// build monomer subprotein
			String subName = "i0d" + i + "_subm";
			TFSub tfs = new TFSub(subName, randomParam(k_deg_protein,rnd), null, null, 0.0, 0.0);
			subm.add(tfs);
			tfs.writeDocument();
			// build the dimer inhibitor
			String name = "i0d" + i;
			Inhibitor inh = new Inhibitor(name, randomParam(k_deg_protein,rnd), tfs, tfs, randomParam(k_bind_protein,rnd), k_unbind_protein, null, 0.0, 0.0);
			i0d.add(inh);
			inh.writeDocument();
		}
	}
	
	private static void tetramerInhibitors(int amount) {
		for(int i = 0; i < amount; i++) {
			// monomer sub
			String subNameM = "i0t" + i + "_subm";
			TFSub tfsm = new TFSub(subNameM, randomParam(k_deg_protein,rnd), null, null, 0.0, 0.0);
			subm.add(tfsm);
			tfsm.writeDocument();
			// dimer sub
			String subNameD = "i0t" + i + "_subd";
			TFSub tfsd = new TFSub(subNameD, randomParam(k_deg_protein,rnd), tfsm, tfsm, randomParam(k_bind_protein,rnd), k_unbind_protein);
			tfsd.writeDocument();
			// tetramer inhibitor
			String name = "i0t" + i;
			Inhibitor inh = new Inhibitor(name, randomParam(k_deg_protein,rnd), tfsd, tfsd, randomParam(k_bind_protein,rnd), k_unbind_protein, null, 0.0, 0.0);
			i0t.add(inh);
			inh.writeDocument();
		}
	}
	
	private static void reporters(int amount) {
		// all the same degradation rate
		double degRate = 0.0012; // from hooshangi05
		// build the reporters
		for(int i = 0; i < amount; i++) {
			String name = "reporter" + i;
			Reporter rep = new Reporter(name, degRate, null, null, 0.0, 0.0);
			reporters.add(rep);
			rep.writeDocument();
		}		
	}
	
	private static void constitutivePromoters() {
		// 1 constitutive library (over full transcription rate range)
		double step = (k_tc[1] - k_tc[0])/(numPromotersPerLibrary-1);
		double current = k_tc[0];
		for(int i = 0; i < numPromotersPerLibrary; i++) {
			Promoter pm = new Promoter("pm_"+i, new ArrayList<Operator>(), current);
			pm.writeDocument();
			current += step;
		}
	}
	
	private static void singleInhibitedPromoters(List<Protein> inhibitors) {
		for(Protein inhibitor : inhibitors) {
			singleInhibitedPromoter(inhibitor);
		}
	}
	
	private static void singleInhibitedPromoter(Protein inhibitor) {
		double[] range = randomParamRange(k_tc,rnd);
		double step = (range[1] - range[0])/(numPromotersPerLibrary-1);
		double current = range[0];
		for(int i = 0; i < numPromotersPerLibrary; i++) {
			Operator o = new Operator(randomParam(k_bind_tf,rnd), randomParam(k_unbind_tf, rnd), (Inhibitor) inhibitor);
			ArrayList<Operator> ops = new ArrayList<Operator>();
			ops.add(o);
			String name = "pm_" + inhibitor.getName() + "_" + i;
			Promoter pm = new Promoter(name, ops, current);
			pm.writeDocument();
			current += step;
		}
	}
	
	private static void doubleInhibitedPromoter(Protein i0, Protein i1) {
		List<Protein> list = new ArrayList<Protein>();
		list.add(i1);
		doubleInhibitedPromoter(i0, list);
	}
	
	private static void doubleInhibitedPromoters(int amount, Protein i0, List<Protein> i1) {
		List<Protein> list = new ArrayList<Protein>();
		list.add(i0);
		doubleInhibitedPromoters(amount, list, i1);
	}
	
	private static void doubleInhibitedPromoter(Protein i0, List<Protein> i1) {
		List<Protein> list = new ArrayList<Protein>();
		list.add(i0);
		doubleInhibitedPromoter(list, i1);
	}
	
	private static void doubleInhibitedPromoters(int amount, List<Protein> i0, List<Protein> i1) {
		for(int i = 0; i < amount; i++) {
			doubleInhibitedPromoter(i0,i1);
		}
	}
	
	private static void doubleInhibitedPromoter(List<Protein> i0, List<Protein> i1) {
		List<Inhibitor> inhibitors = uniqueLibrary(i0,i1);
		promoter(inhibitors.get(0), inhibitors.get(1));
	}
	
	//
	private static void promoter(Inhibitor i0, Inhibitor i1) {
		double[] range = randomParamRange(k_tc,rnd);
		double step = (range[1] - range[0])/(numPromotersPerLibrary-1);
		double current = range[0];	
		for(int i = 0; i < numPromotersPerLibrary; i++) {
			ArrayList<Operator> ops = new ArrayList<Operator>();
			Operator o0 = new Operator(randomParam(k_bind_tf,rnd), randomParam(k_unbind_tf,rnd),i0);
			Operator o1 = new Operator(randomParam(k_bind_tf,rnd), randomParam(k_unbind_tf,rnd),i1);
			ops.add(o0);
			ops.add(o1);
			String name = "pm_"+ i0.getName() + "_" + i1.getName() + "_" + i;
			promoter(name, ops, current);
			current += step;
		}
	}
	
	private static List<Inhibitor> uniqueLibrary(List<Protein> list0, List<Protein> list1) {
		boolean uniqueLib = false;
		Inhibitor i0 = null;
		Inhibitor i1 = null;
		// risking an endless loop here...
		while(!uniqueLib) {
			ArrayList<Operator> ops = new ArrayList<Operator>();
			i0 = (Inhibitor) list0.get(rnd.nextInt(list0.size()));
			i1 = (Inhibitor) list1.get(rnd.nextInt(list1.size()));
			// make sure we have two different inhibitors
			// risking an endless loop here...
			while(i1.equals(i0)) {
				i1 = (Inhibitor) list1.get(rnd.nextInt(list1.size()));
			}
			Operator o0 = new Operator(0.0, 0.0, i0);
			Operator o1 = new Operator(0.0, 0.0, i1);
			ops.add(o0);
			ops.add(o1);
			Promoter lib = new Promoter("",ops,0.0);
			uniqueLib = true;
			for(Promoter p : pmLibraries) {
				if(p.sameLibrary(lib)) {
					uniqueLib = false;
					break;
				}
			}
			if(uniqueLib) {
				pmLibraries.add(lib);
			}
		}
		List<Inhibitor> result = new ArrayList<Inhibitor>(2);
		result.add(i0);
		result.add(i1);
		return result;
	}
	
	private static void promoter(String name, List<Operator> ops, double k) {
		Promoter pm = new Promoter(name, ops, k);
		pm.writeDocument();
	}
	
	private static void rbss() {
		double step = (k_tl[1]-k_tl[0]) / (numRBSs-1);
		double current = k_tl[0];
		for(int i = 0; i < numRBSs; i++) {
			String name = "rbs" + i;
			rbs(name, current);
			current += step;
		}
	}
	
	private static void rbs(String name, double k) {
		RBS rbs = new RBS(name, k);
		rbs.writeDocument();
	}
	
	private static void proteinCoding(List<Protein> proteins) {
		for(Protein protein : proteins) {
			String name = "pc_" + protein.getName();
			ProteinCoding pc = new ProteinCoding(name, protein, randomParam(k_deg_mrna,rnd));
			//ProteinCoding pc = new ProteinCoding(name, protein, k_deg_mrna);
			pc.writeDocument();
		}
	}
	
	private static void terminator() {
		Terminator t = new Terminator("t");
		t.writeDocument();
	}
	
	private static double randomParam(double[] range, Random rnd) {
		return range[0] + rnd.nextDouble()*(range[1]-range[0]);
	}
	
	private static double[] randomParamRange(double[] range, Random rnd) {
		double quarterRange = (range[1] - range[0])/4;
		double min = range[0] + rnd.nextDouble()*quarterRange;
		double max = range[1] - rnd.nextDouble()*quarterRange;
		double[] result = {min,max};
		return result;
	}
	
	/**
	 * Delete the database.
	 */
	public static void deleteDatabase() {
		Promoter.emptyInstance().deleteAllItems();
		ProteinCoding.emptyInstance().deleteAllItems();
		RBS.emptyInstance().deleteAllItems();
		Terminator.emptyInstance().deleteAllItems();
		Reporter.emptyInstance().deleteAllItems();
		Inhibitor.emptyInstance().deleteAllItems();
		Activator.emptyInstance().deleteAllItems();
		TFSub.emptyInstance().deleteAllItems();
		EnvironmentalSignal.emptyInstance().deleteAllItems();
		iim = new ArrayList<Protein>();
		i0m = new ArrayList<Protein>();
		i0d = new ArrayList<Protein>();
		i0t = new ArrayList<Protein>();
		subm = new ArrayList<Protein>();
		reporters = new ArrayList<Protein>();
		pmLibraries = new ArrayList<Promoter>();
	}
}
