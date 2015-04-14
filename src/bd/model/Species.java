package bd.model;

public class Species {

	private String name;
	private int initialAmount;
	
	public Species(String name, int initialAmount) {
		this.name = name;
		this.initialAmount = initialAmount;
	}
	
	//public int hashCode() {
	//	return name.hashCode();
	//}
	
	public String getName() {
		return name;
	}
	
	public int getInitialAmount() {
		return initialAmount;
	}
	
	public boolean equals(Object other) {
		if(other instanceof Species) {
			return name.equals(((Species)other).name);
		}
		return false;
	}

	public String toString() {
		return name + " (" + initialAmount + ")";
	}
}
