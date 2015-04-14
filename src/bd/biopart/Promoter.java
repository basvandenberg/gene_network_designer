package bd.biopart;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

/**
 * @author Bastiaan van den Berg
 *
 */
public class Promoter extends Part {

	/**
	 * 
	 */
	private List<Operator> operators;
	/**
	 * 
	 */
	private double k_transcription;
	
	/**
	 * @param name
	 */
	public Promoter(String name) {
		super(name);
	}
	
	/**
	 * @param name
	 * @param operators
	 * @param k_transcription
	 */
	public Promoter(String name, List<Operator> operators, double k_transcription) {
		super(name);
		if(getAllNames().contains(name)) {
			System.out.println("This promoter already exists: " + name);
		}
		else {
			this.operators = operators;
			this.k_transcription = k_transcription;
		}
	}
	
	/**
	 * @return
	 */
	public static Promoter emptyInstance() {
		return new Promoter("",null,0.0);
	}
	
	/**
	 * @param other
	 * @return
	 */
	public boolean sameLibrary(Promoter other) {
		return this.operators.containsAll(other.operators) &&
		other.operators.containsAll(this.operators);
	}
	
	// true if tfs bind that need to be activated by a small molecule
	/**
	 * @return
	 */
	public boolean bindsActivatedTFs() {
		for(Operator operator : operators) {
			if(operator.getTF().activated()) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @return
	 */
	public String regulationPattern() {
		String result = "";
		List<TF> tfs = new ArrayList<TF>();
		for(Operator operator : operators) {
			// to make sure that operators that bind the same tfs get the 
			// correct pattern. A promoter with two TetR operators should get
			// the pattern "-" and not "--".
			if(!tfs.contains(operator.getTF())) {
				if(operator.getTF() instanceof Activator) {
					result = "+" + result;
				}
				else {
					assert operator.getTF() instanceof Inhibitor;
					result += "-";
				}
				tfs.add(operator.getTF());
			}
		}
		return result;
	}
	
	// only the ones that bind normally on tfs
	/**
	 * @param promoters
	 * @param pattern
	 * @param tfs
	 * @return
	 */
	public static List<Promoter> allPromoters(List<Promoter> promoters, String pattern, List<TF> tfs) {
		ArrayList<Promoter> result = new ArrayList<Promoter>();
		for(Promoter promoter : promoters) {
			if(promoter.regulationPattern().equals(pattern) &&
					!promoter.bindsActivatedTFs() &&
					promoter.getTFs().containsAll(tfs)) {
				result.add(promoter);
			}
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see gnd.biopart.AbstractDatabaseItem#init(org.jdom.Document)
	 */
	protected void init(Document document) {
		super.init(document);
		addOperators(document);
		//createRateConstants(document);
		setTranscriptionRate(document);
	}
	
	/**
	 * @param document
	 */
	private void addOperators(Document document) {
		operators = new ArrayList<Operator>();
		List list = document.getRootElement().getChild("operators").getChildren("operator");
		for(Object o : list) {
			Element operator = (Element)o;
			addOperator(operator);
		}
	}
	
	/**
	 * @param element
	 */
	private void addOperator(Element element) {
		Operator operator = new Operator(element);
		operators.add(operator);
	}
	
	/**
	 * @param document
	 */
	private void setTranscriptionRate(Document document) {
		Element ktc = document.getRootElement().getChild("k_transcription");
		k_transcription = Double.valueOf(ktc.getTextTrim());
	}
	
	// pre: occupation.length() == operators.size()
	// pre: occupation contains only '0' '1'
	/**
	 * @param occupation
	 * @return
	 */
	public double getKTranscription(String occupation) {
		
		double result = this.k_transcription;
		
		for(int i = 0; i < occupation.length(); i++) {
			boolean inhibitor = operators.get(i).getTF() instanceof Inhibitor;
			boolean bound = occupation.charAt(i) == '1';
			// an inhibitor is bound, no transcription
			if(inhibitor && bound) {
				return 0.0;
			}
			else if(inhibitor && !bound) {
				// no influence
			}
			else if(!inhibitor && bound) {
				Activator activator = (Activator)operators.get(i).getTF();
				// TODO: 
				result = Math.max(this.k_transcription, activator.getKTranscription());
			}
			else { // !inhibitor && !bound
				// no influence
			}
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see gnd.biopart.AbstractDatabaseItem#getDocument()
	 */
	protected Document getDocument() {
		Document document = createDocument();
		Element p = new Element("promoter");
		p.setAttribute("name", name);
		Element os = new Element("operators");
		for(Operator operator : getOperators()) {
			Element o = operator.getElement();
			os.addContent(o);
		}
		p.addContent(os);
		Element ktc = new Element("k_transcription");
		ktc.setText(String.valueOf(k_transcription));
		p.addContent(ktc);
		document.setRootElement(p);
		return document;
	}
	
	/**
	 * @return
	 */
	public double getKTranscription() {
		return k_transcription;
	}
	
	/**
	 * @return
	 */
	public List<Operator> getOperators() {
		return operators;
	}
	
	/**
	 * @return
	 */
	public ArrayList<TF> getTFs() {
		ArrayList<TF> result = new ArrayList<TF>();
		for(Operator operator : operators) {
			result.add(operator.getTF());
		}
		return result;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String result = name; // + "\n";
		//for(Operator operator : operators) {
		//	result += "      " + operator.toString() + "\n";	
		//}
		return result;
	}
}
