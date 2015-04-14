package bd.biopart;

import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;

/**
 * @author Bastiaan van den Berg
 *
 */
public abstract class Protein extends Pool {
	
	double k_deg_protein;
	Protein p1;
	Protein p2;
	double k_bind_protein;
	double k_unbind_protein;
	
	/**
	 * @param name
	 */
	public Protein(String name) {
		super(name);
	}
	
	/**
	 * @param name
	 * @param k_deg_protein
	 * @param p1
	 * @param p2
	 * @param k_bind_protein
	 * @param k_unbind_protein
	 */
	public Protein(String name, double k_deg_protein, Protein p1, Protein p2,
			double k_bind_protein, double k_unbind_protein) {
		super(name);
	}
	
	/* (non-Javadoc)
	 * @see gnd.biopart.AbstractDatabaseItem#init(org.jdom.Document)
	 */
	protected void init(Document document) {
		super.init(document);
		setDegradationRate(document);
		setDimer(document);		
	}
	
	/**
	 * @param doc
	 */
	private void setDegradationRate(Document doc) {
		k_deg_protein = Double.valueOf(doc.getRootElement().getChildTextTrim("k_deg_protein"));
	}
	
	/**
	 * @param document
	 */
	protected abstract void setDimer(Document document);
	
	/**
	 * @return
	 */
	public boolean isMonomer() {
		return p1 == null && p2 == null;
	}
	
	// pre homo-oligomer
	/**
	 * @return
	 */
	public Protein getMonomer() {
		if(isMonomer()) {
			return this;
		}
		else {
			return p1.getMonomer();
		}
	}
	
	// recursive
	/**
	 * @param monomers
	 */
	public void getMonomers(Set<Protein> monomers) {
		if(!isMonomer()) {
			p1.getMonomers(monomers);
			p2.getMonomers(monomers);
		}
		else {
			monomers.add(this);
		}
	}
	
	// recursive
	/**
	 * @param oligomers
	 */
	public void getOligomers(Set<Protein> oligomers) {
		if(!isMonomer()) {
			oligomers.add(this);
			p1.getOligomers(oligomers);
			p2.getOligomers(oligomers);
		}
	}
	
	/* (non-Javadoc)
	 * @see gnd.biopart.AbstractDatabaseItem#getDocument()
	 */
	protected Document getDocument() {
		Document document = createDocument();
		Element p = new Element("protein");
		p.setAttribute("name", name);
		Element kdp = new Element("k_deg_protein");
		kdp.setText(String.valueOf(k_deg_protein));
		p.addContent(kdp);
		if(p1 != null && p2 != null) {
			Element dimer = new Element("dimer");
			Element pr1 = new Element("p1");
			Element pr2 = new Element("p2");
			Element kbp = new Element("k_bind_protein");
			Element kup = new Element("k_unbind_protein");
			pr1.setText(p1.getName());
			pr2.setText(p2.getName());
			kbp.setText(String.valueOf(k_bind_protein));
			kup.setText(String.valueOf(k_unbind_protein));
			dimer.addContent(pr1);
			dimer.addContent(pr2);
			dimer.addContent(kbp);
			dimer.addContent(kup);
			p.addContent(dimer);
		}
		document.setRootElement(p);
		return document;
	}
	
	/**
	 * @return
	 */
	public double getKDegProtein() {
		return k_deg_protein;
	}
	
	/**
	 * @return
	 */
	public Protein getP1() {
		return p1;
	}
	
	/**
	 * @return
	 */
	public Protein getP2() {
		return p2;
	}
	
	/**
	 * @return
	 */
	public double getKBindProtein() {
		return k_bind_protein;
	}
	
	/**
	 * @return
	 */
	public double getKUnbindProtein() {
		return k_unbind_protein;
	}
	
	/**
	 * @return
	 */
	public int getOligomerNumber() {
		if(getP1() == null) {
			return 0;
		}
		else {
			return 1 + getP1().getOligomerNumber();
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString () {
		String result =  name;// + "(degradationRate=" + k_deg_protein + ")";
		//if(p1 != null) {
		//	result += "\n            " + p1.toString();
		//	result += "\n            " + p2.toString();
		//	result += "\n            (" + k_bind_protein + "," + k_unbind_protein + ")";
		//}
		return result;
	}
}
