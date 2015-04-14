package bd.biopart;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

/**
 * @author Bastiaan van den Berg
 * This class stores all data for an activator.
 */
public class Activator extends TF {

	/**
	 * Transcription rate when activator is bound to its promoter
	 */
	protected double k_transcription;
	
	/**
	 * This constructor reads all data from the xml file placed in 
	 * data/bioparts/pool/protein/TF/activator/name.xml
	 * @param name The name of the activator.
	 */
	public Activator(String name) {
		super(name);
	}
	
	/**
	 * A contructor that can be used to manually construct an activator.
	 * So the data is not read from the database in this case.
	 * @param name
	 * @param k_deg_protein
	 * @param p1
	 * @param p2
	 * @param k_bind_protein
	 * @param k_unbind_protein
	 * @param k_transcription
	 * @param environmentalSignal
	 * @param k_bind_smallMolecule
	 * @param k_unbind_smallMolecule
	 */
	public Activator(String name, double k_deg_protein, TFSub p1, TFSub p2,
			double k_bind_protein, double k_unbind_protein, double k_transcription, 
			EnvironmentalSignal environmentalSignal, double k_bind_smallMolecule, 
			double k_unbind_smallMolecule) {
		super(name, k_deg_protein, p1, p2,k_bind_protein, k_unbind_protein, 
				environmentalSignal, k_bind_smallMolecule, 
				k_unbind_smallMolecule);
		if(getAllNames().contains(name)) {
			System.out.println("This transcription factor already exists: " + name);
		}
		else {
			this.k_transcription = k_transcription;
		}
	}
	
	/**
	 * @return An empty activator instance.
	 */
	public static Activator emptyInstance() {
		return new Activator("",0.0,null,null,0.0,0.0,0.0,null,0.0,0.0);
	}
	
	/**
	 * This method returns all transcription factors (TFs) that bind the given
	 * environmental signal
	 * @param environmentalSignal The environmental signal.
	 * @return A list with TFs that bind environmentalSignal
	 */
	public static List<Activator> getTFs(EnvironmentalSignal environmentalSignal) {
		ArrayList<Activator> result = new ArrayList<Activator>();
		List<String> names = emptyInstance().getAllNames();
		for(String name : names) {
			Activator activator = new Activator(name);
			EnvironmentalSignal other = activator.getSmallMolecule();
			if(other != null && other.equals(environmentalSignal)) {
				result.add(activator);
			}
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see gnd.biopart.TF#init(org.jdom.Document)
	 */
	protected void init(Document document) {
		super.init(document);
		setTranscriptionRate(document);
	}
	
	/* (non-Javadoc)
	 * @see gnd.biopart.TF#getDocument()
	 */
	protected Document getDocument() {
		Document document = super.getDocument();
		document.getRootElement().setAttribute("type","activator");
		Element ktc = new Element("k_transcription");
		ktc.setText(String.valueOf(k_transcription));
		document.getRootElement().addContent(ktc);
		return document;
	}
	
	/**
	 * @param document
	 */
	private void setTranscriptionRate(Document document) {
		k_transcription = Double.valueOf(document.getRootElement().getChildTextTrim("k_transcription"));
	}
	
	/**
	 * @return
	 */
	public double getKTranscription() {
		return k_transcription;
	}
}
