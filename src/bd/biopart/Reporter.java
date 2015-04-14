package bd.biopart;

import org.jdom.Document;

/**
 * @author Bastiaan van den Berg
 *
 */
public class Reporter extends Protein {

	/**
	 * @param name
	 */
	public Reporter(String name) {
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
	public Reporter(String name, double k_deg_protein, Protein p1, Protein p2,
			double k_bind_protein, double k_unbind_protein) {
		super(name);
		if(getAllNames().contains(name)) {
			System.out.println("This reporter already exists: " + name);
		}
		else {
			this.k_deg_protein = k_deg_protein;
			this.p1 = p1;
			this.p2 = p2;
			this.k_bind_protein = k_bind_protein;
			this.k_unbind_protein = k_unbind_protein;
		}
	}
	
	/**
	 * @return
	 */
	public static Reporter emptyInstance() {
		return new Reporter("",0,null,null,0,0);
	}

	/* (non-Javadoc)
	 * @see gnd.biopart.Protein#getDocument()
	 */
	protected Document getDocument() {
		Document document = super.getDocument();
		document.getRootElement().setAttribute("type", "reporter");
		return document;
	}
	
	/* (non-Javadoc)
	 * @see gnd.biopart.Protein#setDimer(org.jdom.Document)
	 */
	@Override
	protected void setDimer(Document document) {
		// no dimer reporters
	}
}
