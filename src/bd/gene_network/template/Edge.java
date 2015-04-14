package bd.gene_network.template;

import java.util.ArrayList;
import java.util.List;

/**
 * An edge within a gene network template.
 * @author Bastiaan van den Berg
 *
 */
public class Edge {

	/**
	 * The id of the edge.
	 */
	private String id;

	/**
	 * The source vertices of this edge.
	 */
	private List<Vertice> srcs;

	/**
	 * The destination vertices of this edge.
	 */
	private List<Vertice> dsts;

	/**
	 * The type of edge. This can be either "+", "-", or "0". This determines what kind of
	 * transcription factor should be used for this edge. Either an activator ("+"), an 
	 * inhibitor ("-") or a non-regulatory protein like a reporter ("0").
	 */
	private String type; // "-" or "0", "+"

	/**
	 * The signal that can inhibit or activate this edge. The value can be either "+" (activation),
	 * "-" (inhibition), or null (no signal that acts on this edge). This determines if, and what 
	 * kind of small molecule (or other external signal) should act on the transcription factor that
	 * is assigned to this edge. 
	 */
	private String signal; // "-" or "+" or null

	/**
	 * Constructs a new Edge that is still unconnected.
	 * @param id
	 * @param type
	 * @param signal
	 */
	public Edge(String id, String type, String signal) {
		this.id = id;
		this.type = type;
		this.signal = signal;
		this.srcs = new ArrayList<Vertice>();
		this.dsts = new ArrayList<Vertice>();
	}

	/**
	 * Adds a destination vertice to which this edge points.
	 * @param dst
	 */
	public void addDst(Vertice dst) {
		dsts.add(dst);
		if(!(dst.getInput() == null) && !dst.getInput().contains(this)) {
			dst.addInput(this);
		}
	}

	/**
	 * Adds a source vertice from which this edge leaves.
	 * @param src
	 */
	public void addSrc(Vertice src) {
		srcs.add(src);
		if(!(src.getOutput() == null) && !src.getOutput().equals(this)) {
			src.setOutput(this);
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if(other != null && other instanceof Edge) {
			return id.equals(((Edge)other).getId());
		}
		return false;
	}
	
	/**
	 * returns a list with all destination edges.
	 * @return
	 */
	public List<Vertice> getDsts() {
		return dsts;
	}

	/**
	 * Returns the signal that regulates this edge. null if there is no signal 
	 * that regulates this edge.
	 * @return The signal that regulates this edge.
	 */
	public String getSignal() {
		return signal;
	}

	/**
	 * Returns the list of source vertices for this edge.
	 * @return The list of source vertices for this edge.
	 */
	public List<Vertice> getSrcs() {
		return srcs;
	}

	/**
	 * Returns the id of this edge.
	 * @return The id of this edge.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the type of this edge ("-", "+", "0")
	 * @return "-", "+" or "0"
	 */
	public String getType() {
		return type;
	}

	/**
	 * Returns if this edge is an input edge. An edge is an input edge when it
	 * has no source vertices and one or more destination vertices. 
	 * @return True, if this is an input edge. False otherwise.
	 */
	public boolean isInputEdge() {
		return srcs.isEmpty() && !dsts.isEmpty();
	}

	/**
	 * Returns if this edge is an output edge. An edge is an output edge whun it
	 * has no destination vertices and one or more source vertices.
	 * @return True, if this is an output edge. False otherwise.
	 */
	public boolean isOutputEdge() {
		return dsts.isEmpty() && !srcs.isEmpty();
	}
	
	/**
	 * Returns if this edge is ...
	 * @return
	 */
	public boolean isUnconnectedEdge() {
		return srcs.isEmpty() && dsts.isEmpty();
	}

	/**
	 * Returns if this edge can be regulated by a signal (which is the case
	 * when the signal is not null).
	 * @return True if a signal can regulate this edge, false otherwise.
	 */
	public boolean isRegulatedEdge() {
		return signal != null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String result = id;// + " (";
		//result += srcs.isEmpty() ? "*" : srcs.toString();
		//result += ",";
		//result += dsts.isEmpty() ? "*" : dsts.toString();
		//result += ",";
		//result += type == null ? "*" : type;
		//result += ",";
		//result += signal == null ? "*" : signal;
		//result += ")";
		return result;
	}
}
