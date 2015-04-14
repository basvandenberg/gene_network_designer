/**
 * 
 */
package bd.gene_network.logic;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import bd.biopart.Activator;
import bd.biopart.Inhibitor;
import bd.biopart.Reporter;
import bd.biopart.TF;
import bd.gene_network.template.GeneNetworkTemplate;

/**
 * This class holds the global parameters for the Device optimization problem. 
 * It implements the singleton design pattern because only one instance should
 * be instantiated.
 * 
 * @author Bastiaan van den Berg
 */
public final class LogicGeneNetworkSettings {
    
    private String name;
    private GeneNetworkTemplate geneNetworkTemplate;
    private Map<String,TF> input = new HashMap<String,TF>();
    private Map<String,Reporter> output = new HashMap<String,Reporter>();
    private Map<String,boolean[]> binaryTimingDiagram = new HashMap<String,boolean[]>();
    private int stateTime;
    private boolean visual;
	
    /**
     * Constructor for objects of class ProblemSettings
     * @param Filename Problem settings file name
     */
    public LogicGeneNetworkSettings(String filePath) throws FileNotFoundException { 
    	File file = new File(filePath);
		// read the item from the file if the file exists
		if(file.exists()) {
			Document doc = getDocument(file);
			//System.out.print("- Loading logic device settings from settings file...");
			init(doc);
			//System.out.println(", done.");
		}
		else {
			String msg = "=> Could not load the device settings:\n" +
					"- The file "+file.getAbsolutePath()+" does not exist.";
			throw new FileNotFoundException(msg);
		}
    }
    
    /**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the geneNetworkTemplate
	 */
	public GeneNetworkTemplate getGeneNetwork() {
		return geneNetworkTemplate;
	}

	/**
	 * @return the input
	 */
	public Map<String, TF> getInput() {
		return input;
	}

	/**
	 * @return the output
	 */
	public Map<String, Reporter> getOutput() {
		return output;
	}

	/**
	 * @return the stateTransitionPaths
	 */
	public Map<String,boolean[]> getBinaryTimingDiagram() {
		return binaryTimingDiagram;
	}

	/**
	 * @return The simulation time (sec) of one state. 
	 */
	public int getStateTime() {
		return stateTime;
	}

	/**
	 * @return the visual
	 */
	public boolean isVisual() {
		return visual;
	}
	
	/**
	 * @param visual
	 */
	public void setVisual(boolean visual) {
		this.visual = visual;
	}
    
    private Document getDocument(File file) {
		Document doc = null;
		try {
			SAXBuilder builder = new SAXBuilder();
			doc = builder.build(file);
		}
		catch(JDOMException jde) {
			jde.printStackTrace();
		}
		catch(IOException ie) {
			ie.printStackTrace();
		}
		return doc;
	}

    private void init(Document doc) {
    	this.name = doc.getRootElement().getAttributeValue("name");
    	loadGeneNetwork(doc.getRootElement());
    	loadInputs(doc.getRootElement());
    	loadOutputs(doc.getRootElement());
    	loadNumbers(doc.getRootElement());
    	loadBinaryTimingDiagram(doc.getRootElement());
    }
    
    private void loadGeneNetwork(Element root) {
    	String gnName = root.getChildTextTrim("gene_network");
    	geneNetworkTemplate = new GeneNetworkTemplate(gnName);
    }
    
    private void loadInputs(Element root) {
    	List list = root.getChildren("input");
		for(Object o : list) {
			Element element = (Element)o;
			String inputID = element.getAttributeValue("id");
			String tfName = element.getValue();
			TF tf = null;
			if(geneNetworkTemplate.getInput(inputID).getSignal().equals("-")) {
				tf = new Inhibitor(tfName);
			}
			else {
				tf = new Activator(tfName);
			}
			input.put(inputID,tf);
		}
    }
    
    private void loadOutputs(Element root) {
    	List list = root.getChildren("output");
		for(Object o : list) {
			Element element = (Element)o;
			String inputID = element.getAttributeValue("id");
			String reporterName = element.getValue();
			output.put(inputID,new Reporter(reporterName));
		}
    }
    
    private void loadNumbers(Element root) {
    	stateTime = Integer.valueOf(root.getChildTextTrim("state_time"));
    	visual = root.getChildTextTrim("visual").equals("true");
    }
    
    private void loadBinaryTimingDiagram(Element root) {
    	Element element = root.getChild("binary_timing_diagram");
    	List list = element.getChildren("plot");
    	for(Object o : list) {
    		Element e = (Element)o;
    		String id = e.getAttributeValue("id");
    		String plot = e.getValue().trim();
    		boolean[] binSeq = new boolean[plot.length()];
    		for(int i = 0; i < plot.length(); i++) {
    			binSeq[i] = plot.charAt(i) == '1';
    		}
    		binaryTimingDiagram.put(id, binSeq);
    	}
    }
}
