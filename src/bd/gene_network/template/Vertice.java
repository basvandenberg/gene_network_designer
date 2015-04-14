package bd.gene_network.template;

import java.util.ArrayList;
import java.util.List;

/**
 * A vertice within a gene network template.
 * @author Bastiaan van den Berg
 * 
 */
public class Vertice {

	/**
	 * The id of this vertice.
	 */
	private String id;

	/**
	 * A list with all the edges that enter (point to) this vertice.
	 */
	private List<Edge> input;

	/**
	 * The edge that leave this vertice.
	 */
	private Edge output;

	/**
	 * Constructs a new vertice without any input or output edges.
	 * @param id
	 */
	public Vertice(String id) {
		this.id = id;
		this.input = new ArrayList<Edge>();
	}

	/**
	 * 
	 * @param edge
	 */
	public void addInput(Edge edge) {
		this.input.add(edge);
		if(!(edge.getDsts() == null) && !edge.getDsts().contains(this)) {
			edge.addDst(this);
		}
	}

	/**
	 * 
	 * @return
	 */
	public String getId() {
		return id;
	}

	/**
	 * 
	 * @return
	 */
	public List<Edge> getInput() {
		return input;
	}

	/**
	 * 
	 * @return
	 */
	public Edge getOutput() {
		return output;
	}

	/**
	 * 
	 * @return
	 */
	public String regulationPattern() {
		String result = "";
		for (Edge edge : input) {
			if (edge.getType().equals("+")) {
				result = "+" + result;
			} else {
				assert edge.getType().equals("-");
				result += "-";
			}
		}
		return result;
	}

	/**
	 * 
	 * @param output
	 */
	public void setOutput(Edge output) {
		this.output = output;
		if(!(output.getSrcs() == null) && !output.getSrcs().contains(this)) {
			output.addSrc(this);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {
		if (other != null && other instanceof Vertice) {
			return id.equals(((Vertice) other).id);
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String result = id  + " ([";
		for(Edge e : input){
			result += e.getId() + ",";
		}
		if(!input.isEmpty()) {
			result = result.substring(0, result.length()-1);
		}
		result+="],";
		if(output == null) {
			result += "*";
		}
		else {
			result += output.getId();
		}
		result += ")";
		return result;
	}
}
