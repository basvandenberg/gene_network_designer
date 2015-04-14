package bd.biopart;

import org.jdom.Document;
import org.jdom.Element;


/**
 * @author Bastiaan van den Berg
 *
 */
public class TFSub extends Protein {

	/**
	 * @param name
	 */
	public TFSub(String name) {
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
	public TFSub(String name, double k_deg_protein, Protein p1, Protein p2,
			double k_bind_protein, double k_unbind_protein) {
		super(name);
		if(getAllNames().contains(name)) {
			System.out.println("This transcription factor subprotein already exists: " + name);
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
	public static TFSub emptyInstance() {
		return new TFSub("",0,null,null,0,0);
	}
	
	/* (non-Javadoc)
	 * @see gnd.biopart.Protein#getDocument()
	 */
	protected Document getDocument() {
		Document document = super.getDocument();
		document.getRootElement().setAttribute("type", "tf_sub");
		return document;
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
}
