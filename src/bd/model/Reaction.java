package bd.model;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Reaction {

	private List<String> reactants;
	private List<String> products;
	private double rateConstant;
	
	public Reaction(double rateConstant) {
		this.rateConstant = rateConstant;
	}
	
	public void setReactants(String...reactants) {
		this.reactants = Arrays.asList(reactants);
	}
	
	public void setProducts(String...products) {
		this.products = Arrays.asList(products);
	}
	
	public List<String> getReactants() {
		return reactants;
	}

	public List<String> getProducts() {
		return products;
	}

	public double getRateConstant() {
		return rateConstant;
	}

	//public int hashCode() {
	//	return reactants.hashCode() + products.hashCode();//???
	//}
	
	public boolean equals(Object other) {
		if(other instanceof Reaction) {
			Reaction that = (Reaction)other;
			if(!this.products.equals(that.products)) {
				return false;
			}
			if(!this.reactants.equals(that.reactants)) {
				return false;
			}
			if(this.rateConstant != that.rateConstant) {
				return false;
			}
			return true;
		}
		return false;
	}
	
	public String toString() {
		String result = "";
		Iterator<String> it = reactants.iterator();
		result += it.next();
		while(it.hasNext()) {
			result += " + " + it.next();
		}
		result += " -> ";
		it = products.iterator();
		result += it.next();
		while(it.hasNext()) {
			result += " + " + it.next();
		}
		result += " (" + rateConstant + ")";
		return result;
	}
}
