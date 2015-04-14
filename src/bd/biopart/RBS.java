package bd.biopart;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

/**
 * @author Bastiaan van den Berg
 *
 */
public class RBS extends Part {

	/**
	 * The translation rate.
	 */
	private double k_translation;

	/**
	 * @param name
	 */
	public RBS(String name) {
		super(name);
	}
	
	/**
	 * @param name
	 * @param k_translation
	 */
	public RBS(String name, double k_translation) {
		super(name);
		if(getAllNames().contains(name)) {
			System.out.println("This rbs already exists: " + name);
		}
		else {
			this.k_translation = k_translation;
		}
	}
	
	/* (non-Javadoc)
	 * @see gnd.biopart.AbstractDatabaseItem#init(org.jdom.Document)
	 */
	protected void init(Document document) {
		super.init(document);
		setTranslationRate(document);
	}
	
	/**
	 * @param document
	 */
	private void setTranslationRate(Document document) {
		Element root = document.getRootElement();
		k_translation = Double.valueOf(root.getChildTextTrim("k_translation"));
	}
	
	/* (non-Javadoc)
	 * @see gnd.biopart.AbstractDatabaseItem#getDocument()
	 */
	protected Document getDocument() {
		Document document = createDocument();
		Element r = new Element("rbs");
		r.setAttribute("name",name);
		Element ktl = new Element("k_translation");
		ktl.setText(String.valueOf(k_translation));
		r.addContent(ktl);
		document.setRootElement(r);
		return document;
	}
	
	/**
	 * @return
	 */
	public static RBS emptyInstance() {
		return new RBS("",0.0);
	}
	
	/**
	 * @return
	 */
	public static List<Part> allRBSs() {
		List<String> allNames = RBS.emptyInstance().getAllNames();
		ArrayList<Part> result = new ArrayList<Part>();
		for(String name : allNames) {
			result.add(new RBS(name));
		}
		return result;
	}
	
	/**
	 * @return
	 */
	public double getKTranslation() {
		return k_translation;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String result = name;
		//result += "(k_translation=" + k_translation + ")\n";
		return result;
	}
}