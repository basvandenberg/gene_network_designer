package bd.biopart;

import org.jdom.Document;
import org.jdom.Element;

/**
 * @author Bastiaan van den Berg
 *
 */
public class Terminator extends Part {
	
	/**
	 * @param name
	 */
	public Terminator(String name) {
		super(name);
	}
	
	/**
	 * @return
	 */
	public static Terminator emptyInstance() {
		return new Terminator("");
	}
	
	/* (non-Javadoc)
	 * @see gnd.biopart.AbstractDatabaseItem#getDocument()
	 */
	protected Document getDocument() {
		Document document = createDocument();
		Element t = new Element("terminator");
		t.setAttribute("name", getName());
		document.setRootElement(t);
		return document;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String result = name;
		return result;
	}
}