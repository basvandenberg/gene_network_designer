package bd.gene_network.template;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import bd.global.Constants;

/**
 * A gene network template. A list of vertices (which are the protein generators) and a list of
 * connecting edges (which are the transcription factors that are used as signals)
 * @author Bastiaan van den Berg
 * 
 */
public class GeneNetworkTemplate {

	private String id;
	private String idPrefix;
	private Map<String, Edge> input = new HashMap<String, Edge>();
	private Map<String, Edge> output = new HashMap<String, Edge>();
	private List<Vertice> vertices = new ArrayList<Vertice>();
	private List<Edge> edges = new ArrayList<Edge>();
	/**
	 * tmp variable while building the network, merged at the end
	 */
	private Map<String, GeneNetworkTemplate> subnetworks = new HashMap<String, GeneNetworkTemplate>();

	/**
	 * 
	 * @param id
	 */
	public GeneNetworkTemplate(String id) {
		this(id, "");
	}

	/**
	 * 
	 * @param id
	 * @param idPrefix
	 */
	public GeneNetworkTemplate(String id, String idPrefix) {
		this.idPrefix = idPrefix;
		this.id = id;
		File file = new File(Constants.GENE_NETWORK_DIR + id + ".xml");
		Document doc = getDocument(file);
		init(doc);
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
	public List<Vertice> getVertices() {
		return vertices;
	}

	/**
	 * 
	 * @return
	 */
	public List<Edge> getEdges() {
		return edges;
	}

	/**
	 * @param inputEdgeId
	 * @return
	 */
	public Edge getInput(String inputEdgeId) {
		return input.get(inputEdgeId);
	}

	/**
	 * @param outputEdgeId
	 * @return
	 */
	public Edge getOutput(String outputEdgeId) {
		return output.get(outputEdgeId);
	}
	
	public int numOutputs() {
		return output.size();
	}
	
	public int numInputs() {
		return input.size();
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public Vertice getVerticeById(String id) {
		for (Vertice vertice : vertices) {
			if (vertice.getId().equals(id)) {
				return vertice;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public Edge getEdgeById(String id) {
		for (Edge edge : edges) {
			if (edge.getId().equals(id)) {
				return edge;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String result = "*** Gene network: " + id + " ***\n";
		result += "\nVertices:\n";
		for (Vertice vertice : vertices) {
			result += vertice.toString() + "\n";
		}
		result += "\nEdges:\n";
		for (Edge edge : edges) {
			result += edge.toString() + "\n";
		}
		result += "\nInput:\n";
		for (String key : input.keySet()) {
			result += key + ": " + input.get(key).toString() + "\n";
		}
		result += "\nOutput:\n";
		for (String key : output.keySet()) {
			result += key + ": " + output.get(key).toString() + "\n";
		}
		result += "\n*** End gene network ***\n";
		return result;
	}

	/**
	 * 
	 * @param file
	 * @return
	 */
	private Document getDocument(File file) {
		Document doc = null;
		try {
			SAXBuilder builder = new SAXBuilder();
			doc = builder.build(file);
		} catch (JDOMException jde) {
			jde.printStackTrace();
		} catch (IOException ie) {
			ie.printStackTrace();
		}
		return doc;
	}

	/**
	 * 
	 * @param doc
	 */
	private void init(Document doc) {
		addSubNetworks(doc);
		addConnectionEdges(doc);
		addVertices(doc);
		addEdges(doc);
		addInputs(doc);
		addOutputs(doc);
		if (!subnetworks.isEmpty()) {
			mergeSubnetworks();
		}
	}

	/**
	 * 
	 * @param doc
	 */
	private void addSubNetworks(Document doc) {
		List list = doc.getRootElement().getChildren("subnetwork");
		for (Object o : list) {
			Element element = (Element) o;
			addSubNetwork(element);
		}
	}

	/**
	 * recursive
	 * 
	 * @param net
	 */
	private void addSubNetwork(Element net) {
		String networkId = idPrefix + net.getAttributeValue("id");
		String netName = net.getAttributeValue("name");
		GeneNetworkTemplate subnet = new GeneNetworkTemplate(netName, networkId);
		subnetworks.put(networkId, subnet);
	}

	/**
	 * 
	 * @param doc
	 */
	private void addConnectionEdges(Document doc) {
		List list = doc.getRootElement().getChildren("connection_edge");
		for (Object o : list) {
			Element element = (Element) o;
			addConnectionEdge(element);
		}
	}

	/**
	 * 
	 * @param element
	 */
	private void addConnectionEdge(Element element) {
		String edgeId = idPrefix + element.getAttributeValue("id");
		// add connection between subnetworks (takes signal from out!!!)
		// connection edges may not have a signal!!!

		List<Edge> out = getOutEdges(element);
		List<Edge> in = getInEdges(element);

		// type of all in edges should be the same... just pick the first
		String type = in.get(0).getType();
		// pick the signal from the first output TODO: this is not enough...
		String signal = out.get(0).getSignal();
		Edge edge = new Edge(edgeId, type, signal);
		for (Edge e : out) {
			for (Vertice v : e.getSrcs()) {
				v.setOutput(edge);
				edge.addSrc(v);
			}
		}
		for (Edge e : in) {
			for (Vertice v : e.getDsts()) {
				// clear the input edges
				v.getInput().remove(e);
				// add connection edges
				v.addInput(edge);
				edge.addDst(v);
			}
		}
		edges.add(edge);
	}

	/**
	 * 
	 * @param element
	 * @return
	 */
	private List<Edge> getOutEdges(Element element) {
		List<Edge> result = new ArrayList<Edge>();
		List list = element.getChildren("src");
		for (Object o : list) {
			Element e = (Element) o;
			result.add(getOutEdge(e));
		}
		return result;
	}

	/**
	 * 
	 * @param element
	 * @return
	 */
	private Edge getOutEdge(Element element) {
		String srcNetwork = idPrefix + element.getAttributeValue("network");
		String srcOutputId = element.getAttributeValue("output_id");
		GeneNetworkTemplate srcGN = subnetworks.get(srcNetwork);
		String outputId = srcGN.idPrefix + srcOutputId;
		return srcGN.output.get(outputId);
	}

	/**
	 * 
	 * @param element
	 * @return
	 */
	private List<Edge> getInEdges(Element element) {
		List<Edge> result = new ArrayList<Edge>();
		List list = element.getChildren("dest");
		for (Object o : list) {
			Element e = (Element) o;
			result.add(getInEdge(e));
		}
		return result;
	}

	/**
	 * 
	 * @param element
	 * @return
	 */
	private Edge getInEdge(Element element) {
		String destNetwork = element.getAttributeValue("network");
		String destInputId = element.getAttributeValue("input_id");
		GeneNetworkTemplate srcGN = subnetworks.get(idPrefix + destNetwork);
		return srcGN.input.get(srcGN.idPrefix + destInputId);
	}

	/**
	 * 
	 * @param doc
	 */
	private void addVertices(Document doc) {
		List list = doc.getRootElement().getChildren("vertice");
		for (Object o : list) {
			Element element = (Element) o;
			addVertice(element);
		}
	}

	/**
	 * 
	 * @param element
	 */
	private void addVertice(Element element) {
		String vid = idPrefix + element.getAttributeValue("id");
		Vertice vertice = new Vertice(vid);
		vertices.add(vertice);
	}

	/**
	 * 
	 * @param doc
	 */
	private void addEdges(Document doc) {
		List list = doc.getRootElement().getChildren("edge");
		for (Object o : list) {
			Element element = (Element) o;
			addEdge(element);
		}
	}

	/**
	 * 
	 * @param element
	 */
	private void addEdge(Element element) {
		String id = idPrefix + element.getAttributeValue("id");
		String type = element.getAttributeValue("type");
		String signal = element.getAttributeValue("signal");
		Edge edge = new Edge(id, type, signal);
		addSrcs(element, edge);
		addDsts(element, edge);
		edges.add(edge);
	}

	/**
	 * 
	 * @param element
	 * @param edge
	 */
	private void addSrcs(Element element, Edge edge) {
		List list = element.getChildren("src");
		for (Object o : list) {
			Element e = (Element) o;
			addSrc(e, edge);
		}
	}

	/**
	 * 
	 * @param element
	 * @param edge
	 */
	private void addSrc(Element element, Edge edge) {
		String vertice_id = idPrefix + element.getAttributeValue("vertice_id");
		edge.addSrc(getVerticeById(vertice_id));
	}

	/**
	 * 
	 * @param element
	 * @param edge
	 */
	private void addDsts(Element element, Edge edge) {
		List list = element.getChildren("dest");
		for (Object o : list) {
			Element e = (Element) o;
			addDst(e, edge);
		}
	}

	/**
	 * 
	 * @param element
	 * @param edge
	 */
	private void addDst(Element element, Edge edge) {
		String vertice_id = idPrefix + element.getAttributeValue("vertice_id");
		edge.addDst(getVerticeById(vertice_id));
	}

	/**
	 * 
	 * @param doc
	 */
	private void addInputs(Document doc) {
		List list = doc.getRootElement().getChildren("input");
		for (Object o : list) {
			Element element = (Element) o;
			addInput(element);
		}
	}

	/**
	 * 
	 * @param element
	 */
	private void addInput(Element element) {
		String id = idPrefix + element.getAttributeValue("id");
		// or, in case of subnetwork edge
		String networkId = element.getAttributeValue("network");
		String inputId = element.getAttributeValue("input_id");
		// or, in case of own edge
		String edgeId = idPrefix + element.getAttributeValue("edge_id");
		if (networkId != null && inputId != null) {
			GeneNetworkTemplate gn = subnetworks.get(idPrefix + networkId);
			Edge inputEdge = gn.input.get(gn.idPrefix + inputId);
			input.put(id, inputEdge);
		} else { // edgeId != null
			input.put(id, getEdgeById(edgeId));
		}
	}

	/**
	 * 
	 * @param doc
	 */
	private void addOutputs(Document doc) {
		List list = doc.getRootElement().getChildren("output");
		for (Object o : list) {
			Element element = (Element) o;
			addOutput(element);
		}
	}

	/**
	 * 
	 * @param element
	 */
	private void addOutput(Element element) {
		String id = idPrefix + element.getAttributeValue("id");
		// or, in case of subnetwork edge
		String networkId = element.getAttributeValue("network");
		String outputId = element.getAttributeValue("output_id");
		// or, in case of own edge
		String edgeId = idPrefix + element.getAttributeValue("edge_id");
		if (networkId != null && outputId != null) {
			GeneNetworkTemplate gn = subnetworks.get(idPrefix + networkId);
			Edge outputEdge = gn.output.get(gn.idPrefix + outputId);
			output.put(id, outputEdge);
		} else { // edgeId != null
			output.put(id, getEdgeById(edgeId));
		}
	}

	/**
	 * 
	 */
	private void mergeSubnetworks() {
		for (GeneNetworkTemplate gn : subnetworks.values()) {
			for (Vertice vertice : gn.getVertices()) {
				vertices.add(vertice);
			}
			// only internal edges
			for (Edge edge : gn.getEdges()) {
				if (!edge.isInputEdge() && !edge.isOutputEdge()) {
					edges.add(edge);
				}
			}
		}
		// connection edges are already added
		// add input and output edges if they are not the connection edges...
		for (String key : input.keySet()) {
			if (!edges.contains(input.get(key))) {
				edges.add(input.get(key));
			}
		}
		for (String key : output.keySet()) {
			if (!edges.contains(output.get(key))) {
				Edge edge = output.get(key);
				edges.add(edge);
				List<Vertice> vertices = edge.getSrcs();
				for(Vertice v : vertices) {
					v.setOutput(edge);
				}
				
			}
		}
	}
}