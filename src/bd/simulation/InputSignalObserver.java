package bd.simulation;

import fern.simulation.Simulator;
import fern.simulation.Simulator.FireType;
import fern.simulation.observer.Observer;


public class InputSignalObserver extends Observer {
	
	int[] theta = {25000,50000,75000};
	int amountOld = 0;
	int amountNew = 100;
	int counter;
	
	boolean t = false;
	
	public InputSignalObserver(Simulator sim) {
		super(sim);
	}
	
	@Override
	public void activateReaction(int mu, double tau, FireType fireType,
			int times) {
		// TODO Auto-generated method stub

	}

	@Override
	public void finished() {
		// TODO Auto-generated method stub

	}

	@Override
	public void started() {
		// init induction time point
		this.setTheta(15000);
		counter = 0;
		
		
		this.getSimulator().setAmount(this.getSimulator().getNet().getSpeciesByName("sm_iim0"), 400);
		this.getSimulator().setAmount(this.getSimulator().getNet().getSpeciesByName("sm_iim1"), 400);
		
		
		// init gene numbers
		/*int species0 = this.getSimulator().getNet().getSpeciesByName("pg0_gene_0");
		if(species0 >= 0) {
			this.getSimulator().setAmount(species0,geneCount);
		}
		int species1 = this.getSimulator().getNet().getSpeciesByName("pg1_gene_0");
		if(species1 >= 0) {
			this.getSimulator().setAmount(species1,geneCount);
		}
		int species2 = this.getSimulator().getNet().getSpeciesByName("pg2_gene_0");
		if(species2 >= 0) {
			this.getSimulator().setAmount(species2,geneCount);
		}
		int species3 = this.getSimulator().getNet().getSpeciesByName("pg3_gene_0");
		if(species3 >= 0) {
			this.getSimulator().setAmount(species3,geneCount);
		}
		
		// init inducer
		int species4 = getSimulator().getNet().getSpeciesByName("hooshangi05_aTc");
		if(species4 >= 0) {
			getSimulator().setAmount(species4,amountOld);
		}
		
		System.out.println("init:");
		if(species0 >= 0) {
			System.out.println("pg0_gene_0: " + getSimulator().getAmount(species0));
		}
		if(species1 >= 0) {
			System.out.println("pg1_gene_0: " + getSimulator().getAmount(species1));
		}
		if(species2 >= 0) {
			System.out.println("pg2_gene_0: " + getSimulator().getAmount(species2));
		}
		if(species3 >= 0) {
			System.out.println("pg3_gene_0: " + getSimulator().getAmount(species3));
		}
		if(species4 >= 0) {
			System.out.println("aTc: " + getSimulator().getAmount(species4));
		}*/
	}

	@Override
	public void step() {
		//double time = getSimulator().getTime();
		//System.out.print(time + ": ");
		//if(time < theta[0]) {
			//System.out.println("00");
		//}
//		if(time > theta[0] && time < theta[1]) {
//			if(counter == 0) {
//				counter++;
//				int species = this.getSimulator().getNet().getSpeciesByName("IPTG");
//				this.getSimulator().setAmount(species, amountNew);
//				//System.out.println("time: " + getSimulator().getTime() + " --> amount set to: " + getSimulator().getAmount(species));
//			}
//			//System.out.println("01");
//		}
//		else if(time > theta[1] && time < theta[2]) {
//			if(counter == 1) {
//				counter++;
//				int species = this.getSimulator().getNet().getSpeciesByName("aTc");
//				this.getSimulator().setAmount(species, amountNew);
//				//System.out.println("time: " + getSimulator().getTime() + " --> amount aTc set to: " + getSimulator().getAmount(species));
//			}
//			int species = this.getSimulator().getNet().getSpeciesByName("IPTG");
//			this.getSimulator().setAmount(species, amountOld);
//			//System.out.println("time: " + getSimulator().getTime() + " --> amount IPTG set to: " + getSimulator().getAmount(species));
//			//System.out.println("10");
//		}
//		else if(time > theta[2]) {
//			if(counter == 2) {
//				counter++;
//				int species = this.getSimulator().getNet().getSpeciesByName("IPTG");
//				this.getSimulator().setAmount(species, amountNew);
//				//System.out.println("time: " + getSimulator().getTime() + " --> amount IPTG set to: " + getSimulator().getAmount(species));
//			}
//			//System.out.println("11");
//		}
	}

	@Override
	public void theta(double theta) {
		// add inducer
		
		t = true;
		this.getSimulator().setAmount(this.getSimulator().getNet().getSpeciesByName("sm_iim0"), 0);
		this.getSimulator().setAmount(this.getSimulator().getNet().getSpeciesByName("sm_iim1"), 0);
		
		/*int species = this.getSimulator().getNet().getSpeciesByName("");
		if(species >= 0) {
			this.getSimulator().setAmount(species,amountNew);
			System.out.println("time: " + getSimulator().getTime() + " --> amount aTc set to: " + getSimulator().getAmount(species));
		}*/
		/*int species = this.getSimulator().getNet().getSpeciesByName("hooshangi05_aTc");
		if(species >= 0) {
			this.getSimulator().setAmount(species,amountNew);
			System.out.println("time: " + getSimulator().getTime() + " --> amount aTc set to: " + getSimulator().getAmount(species));
		}*/
	}

}
