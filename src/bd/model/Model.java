package bd.model;


import java.util.ArrayList;
import java.util.List;

import bd.biopart.Device;

public abstract class Model {

	protected Device device;
	protected List<Species> speciesList = new ArrayList<Species>();
	protected List<Reaction> reactionList = new ArrayList<Reaction>();
	
	public Model(Device device) {
		this.device = device;
	}
	
	public Device getDevice() {
		return device;
	}
	
	public List<Species> getSpecies() {
		return speciesList;
	}
	
	public List<Reaction> getReactions() {
		return reactionList;
	}
	
	public String toString() {
		String result = "\nModel: (" + speciesList.size() + " species, " + reactionList.size() + " reactions" + ")\n";
		result += "@species\n";
		for(Species s : speciesList) {
			result += "  " + s.toString() + "\n";
		}
		result += "@reactions\n";
		for(Reaction r : reactionList) {
			result += "  " + r.toString() + "\n";
		}
		return result;
	}
}
