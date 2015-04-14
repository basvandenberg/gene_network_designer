package bd.biopart;

/**
 * @author Bastiaan van den Berg
 *
 */
public class ProteinGenerator {

	/**
	 * 
	 */
	private Promoter promoter;
	/**
	 * 
	 */
	private RBS rbs;
	/**
	 * 
	 */
	private ProteinCoding proteinCoding;
	/**
	 * 
	 */
	private Terminator terminator;
	
	/**
	 * @param p
	 * @param r
	 * @param pc
	 * @param t
	 */
	public ProteinGenerator(Promoter p, RBS r, ProteinCoding pc, Terminator t) {
		promoter = p;
		rbs = r;
		proteinCoding = pc;
		terminator = t;
	}
	
	public static ProteinGenerator fromStringRepresentation(String s) {
		String pgString = s.substring(1, s.length()-1);
		String[] partStrings = pgString.split(",");
		Promoter pm = new Promoter(partStrings[0]);
		RBS rbs = new RBS(partStrings[1]);
		ProteinCoding pc = new ProteinCoding(partStrings[2]);
		Terminator t = new Terminator(partStrings[3]);
		return new ProteinGenerator(pm, rbs, pc, t);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String result = "(";
		result += promoter == null ? "" : promoter.toString();
		result += ",";
		result += rbs == null ? "" : rbs.toString();
		result += ",";
		result += proteinCoding == null ? "" : proteinCoding.toString();
		result += ",";
		result += terminator == null ? "" : terminator.toString();
		result += ")";
		return result;
	}
	
	/**
	 * @param promoter
	 */
	public void setPromoter(Promoter promoter) {
		this.promoter = promoter;
	}
	
	/**
	 * @return
	 */
	public Promoter getPromoter() {
		return promoter;
	}
	
	/**
	 * @param rbs
	 */
	public void setRBS(RBS rbs) {
		this.rbs = rbs;
	}
	
	/**
	 * @return
	 */
	public RBS getRBS() {
		return rbs;
	}
	
	/**
	 * @param proteinCoding
	 */
	public void setProteinCoding(ProteinCoding proteinCoding) {
		this.proteinCoding = proteinCoding;
	}
	
	/**
	 * @return
	 */
	public ProteinCoding getProteinCoding() {
		return proteinCoding;
	}
	
	/**
	 * @param terminator
	 */
	public void setTerminator(Terminator terminator) {
		this.terminator = terminator;
	}
	
	/**
	 * @return
	 */
	public Terminator getTerminator() {
		return terminator;
	}
}