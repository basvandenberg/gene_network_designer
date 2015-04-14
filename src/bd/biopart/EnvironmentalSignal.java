package bd.biopart;

import org.jdom.Document;
import org.jdom.Element;

/**
 * An environmental signal, for example a small molecule, that can bind to a 
 * transcription factor and thereby either inhibit or activate it.
 * @author Bastiaan van den Berg
 *
 */
public class EnvironmentalSignal extends Pool {
	
	/**
	 * If it is an inhibitor or an activator.
	 */
	public boolean inhibitor;
	
	/**
	 * Construct an environmental signal with data read from the database.
	 * @param name Name of the environmental signal.
	 */
	public EnvironmentalSignal(String name) {
		super(name);
	}
	
	/**
	 * Manually construct an environmental signal. No data read from 
	 * the database.
	 * @param name
	 * @param inhibitor
	 */
	public EnvironmentalSignal(String name, boolean inhibitor) {
		super(name);
		this.inhibitor = inhibitor;
	}
	
	/* (non-Javadoc)
	 * @see gnd.biopart.AbstractDatabaseItem#init(org.jdom.Document)
	 */
	protected void init(Document document) {
		super.init(document);
		setInhibitor(document);
	}

	private void setInhibitor(Document doc) {
		String type = doc.getRootElement().getAttributeValue("type");
		inhibitor = type.equals("inhibitor");
	}

	public static EnvironmentalSignal emptyInstance() {
		return new EnvironmentalSignal("",false);
	}
	
	/* (non-Javadoc)
	 * @see gnd.biopart.AbstractDatabaseItem#getDocument()
	 */
	protected Document getDocument() {
		Document document = createDocument();
		Element sm = new Element("environmental_signal");
		sm.setAttribute("name", getName());
		sm.setAttribute("type", (inhibitor ? "inhibitor" : "activator"));
		document.setRootElement(sm);
		return document;
	}
	
	/**
	 * @return True when it is an inhibitor, false when it is an activator.
	 */
	public boolean getInhibitor() {
		return inhibitor;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Signal: " + name;
	}
}
