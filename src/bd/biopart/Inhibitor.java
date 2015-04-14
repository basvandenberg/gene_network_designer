package bd.biopart;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;

/**
 * An Inhibitor.
 * @author Bastiaan van den Berg
 *
 */
public class Inhibitor extends TF {

	/**
	 * Construct an inhibitor, data read from database.
	 * @param name The name of the inhibitor.
	 */
	public Inhibitor(String name) {
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
	public Inhibitor(String name, double k_deg_protein, TFSub p1, TFSub p2,
			double k_bind_protein, double k_unbind_protein, 
			EnvironmentalSignal environmentalSignal, double k_bind_smallMolecule, 
			double k_unbind_smallMolecule) {
		super(name, k_deg_protein, p1, p2,k_bind_protein, k_unbind_protein, 
				environmentalSignal, k_bind_smallMolecule, 
				k_unbind_smallMolecule);
	}

	public static Inhibitor emptyInstance() {
		return new Inhibitor("",0.0,null,null,0.0,0.0,null,0.0,0.0);
	}
	
	/**
	 * @param environmentalSignal
	 * @return
	 */
	public static List<Inhibitor> getTFs(EnvironmentalSignal environmentalSignal) {
		ArrayList<Inhibitor> result = new ArrayList<Inhibitor>();
		List<String> names = emptyInstance().getAllNames();
		for(String name : names) {
			Inhibitor inhibitor = new Inhibitor(name);
			EnvironmentalSignal other = inhibitor.getSmallMolecule();
			if(other != null && other.equals(environmentalSignal)) {
				result.add(inhibitor);
			}
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see gnd.biopart.TF#getDocument()
	 */
	protected Document getDocument() {
		Document document = super.getDocument();
		document.getRootElement().setAttribute("type","inhibitor");
		return document;
	}
}
