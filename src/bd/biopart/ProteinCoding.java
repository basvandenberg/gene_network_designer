package bd.biopart;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

/**
 * @author Bastiaan van den Berg
 *
 */
public class ProteinCoding extends Part {
	
	/**
	 * The protein for which this protein coding part encodes.
	 */
	private Protein protein;
	/**
	 * The degradation rate of the mRNA that is transcribed from this protein 
	 * coding part.
	 */
	private double k_deg_mrna;
	
	/**
	 * A new protein coding part is constructed with data fetched from the
	 * database. 
	 * @param name The identifying name of the protein coding part.
	 */
	public ProteinCoding(String name) {
		super(name);
	}
	
	/**
	 * @param name
	 * @param protein
	 * @param k_deg_mRNA
	 */
	public ProteinCoding(String name, Protein protein, double k_deg_mRNA) {
		super(name);
		if(getAllNames().contains(name)) {
			System.out.println("This protein coding already exists: " + name);
		}
		else {
			this.protein = protein;
			this.k_deg_mrna = k_deg_mRNA;
		}
	}
	
	/* (non-Javadoc)
	 * @see gnd.biopart.AbstractDatabaseItem#init(org.jdom.Document)
	 */
	protected void init(Document document) {
		super.init(document);
		setProtein(document);
		setMRNADegRate(document);
	}
	
	/**
	 * @param document
	 */
	private void setProtein(Document document) {
		Element proteinElement = document.getRootElement().getChild("protein");
		String type = proteinElement.getAttributeValue("type");
		String proteinName = proteinElement.getTextTrim();
		if(type.equals("inhibitor")) {
			protein = new Inhibitor(proteinName);
		}
		else if(type.equals("activator")) {
			protein = new Activator(proteinName);
		}
		else if(type.equals("tf_sub")) {
			protein = new TFSub(proteinName);
		}
		else if(type.equals("reporter")) {
			protein = new Reporter(proteinName);
		}
		else {
			System.out.println("Wrong protein type in protein coding part: " + name);
		}
	}
	
	/**
	 * @param document
	 */
	private void setMRNADegRate(Document document) {
		Element root = document.getRootElement();
		k_deg_mrna = Double.valueOf(root.getChildTextTrim("k_deg_mrna"));
	}
	
	/* (non-Javadoc)
	 * @see gnd.biopart.AbstractDatabaseItem#getDocument()
	 */
	protected Document getDocument() {
		Document document = createDocument();
		Element pc = new Element("protein_coding");
		pc.setAttribute("name", getName());
		Element p = new Element("protein");
		Element kdm = new Element("k_deg_mrna");
		p.setText(getProtein().getName());
		if(getProtein() instanceof Inhibitor) {
			p.setAttribute("type", "inhibitor");
		}
		else if(getProtein() instanceof Activator) {
			p.setAttribute("type", "activator");
		}
		else if(getProtein() instanceof TFSub) {
			p.setAttribute("type", "tf_sub");
		}
		else if(getProtein() instanceof Reporter) {
			p.setAttribute("type", "reporter");
		}
		kdm.setText(String.valueOf(k_deg_mrna));
		pc.addContent(p);
		pc.addContent(kdm);
		document.setRootElement(pc);
		return document;
	}
	
	/**
	 * @return
	 */
	public static ProteinCoding emptyInstance() {
		return new ProteinCoding("",null,0.0);
	}
	
	// only monomers or homo-oligomers!!!
	/**
	 * @param protein
	 * @return
	 */
	public static ProteinCoding getProteinCoding(Protein protein) {
		if(!protein.isMonomer()) {
			protein = protein.getMonomer();
		}
		List<String> names = emptyInstance().getAllNames();
		for(String name : names) {
			ProteinCoding pc = new ProteinCoding(name);
			if(pc.getProtein().equals(protein)) {
				return pc;
			}
		}
		return null;
	}
	
	/**
	 * @param protein
	 * @param allPC
	 * @return
	 */
	public static ProteinCoding getProteinCoding(Protein protein, List<ProteinCoding> allPC) {
		if(!protein.isMonomer()) {
			protein = protein.getMonomer();
		}
		for(ProteinCoding pc : allPC) {
			if(pc.getProtein().equals(protein)) {
				return pc;
			}
		}
		return null;
	}
	
	/**
	 * @return
	 */
	public Protein getProtein(){
		return protein;
	}
	
	/**
	 * @return
	 */
	public double getKDegMrna() {
		return k_deg_mrna;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String result = name; // + "\n";
		//result += "      " + protein.toString() + "\n";
		return result;
	}
}
