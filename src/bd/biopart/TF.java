package bd.biopart;

import org.jdom.Document;
import org.jdom.Element;

/**
 * @author Bastiaan van den Berg
 *
 */
public abstract class TF extends Protein {

	private EnvironmentalSignal environmentalSignal;
	private double k_bind_environmentalSignal;
	private double k_unbind_environmentalSignal;
	
	/**
	 * @param name
	 */
	public TF(String name) {
		super(name);
	}
	
	/**
	 * @param name
	 * @param k_deg_protein
	 * @param p1
	 * @param p2
	 * @param k_bind_protein
	 * @param k_unbind_protein
	 * @param environmentalSignal
	 * @param k_bind_smallMolecule
	 * @param k_unbind_smallMolecule
	 */
	public TF(String name, double k_deg_protein, TFSub p1, TFSub p2,
			double k_bind_protein, double k_unbind_protein, 
			EnvironmentalSignal environmentalSignal, double k_bind_smallMolecule, 
			double k_unbind_smallMolecule) {
		super(name);
		if(getAllNames().contains(name)) {
			System.out.println("This transcription factor already exists: " + name);
		}
		else {
			this.k_deg_protein = k_deg_protein;
			this.p1 = p1;
			this.p2 = p2;
			this.k_bind_protein = k_bind_protein;
			this.k_unbind_protein = k_unbind_protein;
			this.environmentalSignal = environmentalSignal;
			this.k_bind_environmentalSignal = k_bind_smallMolecule;
			this.k_unbind_environmentalSignal = k_unbind_smallMolecule;
		}
	}

	/* (non-Javadoc)
	 * @see gnd.biopart.Protein#init(org.jdom.Document)
	 */
	protected void init(Document document) {
		super.init(document);
		addSmallMolecule(document);	
	}
	
	/* (non-Javadoc)
	 * @see gnd.biopart.Protein#setDimer(org.jdom.Document)
	 */
	@Override
	protected void setDimer(Document document) {
		Element dimer = document.getRootElement().getChild("dimer");
		if(dimer != null) {
			p1 = new TFSub(dimer.getChild("p1").getValue());
			p2 = new TFSub(dimer.getChild("p2").getValue());
			k_bind_protein = Double.valueOf(dimer.getChild("k_bind_protein").getValue());
			k_unbind_protein = Double.valueOf(dimer.getChild("k_unbind_protein").getValue());
		}
	}
	
	/**
	 * @param document
	 */
	private void addSmallMolecule(Document document) {
		Element element = document.getRootElement().getChild("small_molecule");
		if(element != null) {
			environmentalSignal = new EnvironmentalSignal(element.getAttributeValue("name"));
			k_bind_environmentalSignal = Double.valueOf(element.getChildTextTrim("k_bind_smallMolecule"));
			k_unbind_environmentalSignal = Double.valueOf(element.getChildTextTrim("k_unbind_smallMolecule"));
		}
	}
	
	/**
	 * @return
	 */
	public boolean signalReceiver() {
		return environmentalSignal != null;
	}
	
	/* (non-Javadoc)
	 * @see gnd.biopart.Protein#getDocument()
	 */
	protected Document getDocument() {
		Document document = super.getDocument();
		document.getRootElement().setAttribute("type","tf");
		if(environmentalSignal != null) {
			Element s = new Element("small_molecule");
			s.setAttribute("name", environmentalSignal.getName());
			Element kbs = new Element("k_bind_smallMolecule");
			kbs.setText(String.valueOf(k_bind_environmentalSignal));
			Element kus = new Element("k_unbind_smallMolecule");
			kus.setText(String.valueOf(k_unbind_environmentalSignal));
			s.addContent(kbs);
			s.addContent(kus);
			document.getRootElement().addContent(s);
		}
		return document;
	}
	
	/**
	 * @return
	 */
	public EnvironmentalSignal getSmallMolecule() {
		return environmentalSignal;
	}
	
	/**
	 * @return
	 */
	public double getKBindSignal() {
		return k_bind_environmentalSignal;
	}
	
	/**
	 * @return
	 */
	public double getKUnbindSignal() {
		return k_unbind_environmentalSignal;
	}
	
	/**
	 * @return
	 */
	public boolean bindsSmallMolecule() {
		return environmentalSignal != null;
	}

	/**
	 * @return
	 */
	public boolean inhibited() {
		return bindsSmallMolecule() && environmentalSignal.getInhibitor();
	}
	
	/**
	 * @return
	 */
	public boolean activated() {
		return bindsSmallMolecule() && !environmentalSignal.getInhibitor();
	}
	
	/* (non-Javadoc)
	 * @see gnd.biopart.Protein#toString()
	 */
	public String toString() {
		String result =  super.toString();
		//if(smallMolecule != null) {
		//	result += "\n            " + smallMolecule.toString();
		//	result += " (" + k_bind_smallMolecule + "," + k_unbind_smallMolecule  + ")";	
		//}
		return result;
	}
}
