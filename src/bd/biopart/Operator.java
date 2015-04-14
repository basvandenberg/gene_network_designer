package bd.biopart;

import org.jdom.Element;

/**
 * @author Bastiaan van den Berg
 *
 */
public class Operator {

	/**
	 * The binding rate constant of the transcription factor tf.
	 */
	private double k_bind_tf;
	/**
	 * The unbinding rate constant of the transcription factor tf.
	 */
	private double k_unbind_tf;
	/**
	 * The transcription factor that binds to this operator.
	 */
	private TF tf;
	
	/**
	 * Builds an operator based on the data in the xml element object.
	 * @param element An xml element containing the data for this operator.
	 */
	public Operator(Element element) {
		setTF(element);
		setBindingRates(element);
	}
	
	/**
	 * @param k_bind_tf
	 * @param k_unbind_tf
	 * @param tf
	 */
	public Operator(double k_bind_tf, double k_unbind_tf, TF tf) {
		this.k_bind_tf = k_bind_tf;
		this.k_unbind_tf = k_unbind_tf;
		this.tf = tf;
	}
	
	/**
	 * @param element
	 */
	private void setTF(Element element) {
		String tfName = element.getChildTextTrim("tf");
		String type = element.getChild("tf").getAttributeValue("type");
		if(type.equals("inhibitor")) {
			tf = new Inhibitor(tfName);
		}
		else if(type.equals("activator")) {
			tf = new Activator(tfName);
		}
	}
	
	/**
	 * @param element
	 */
	private void setBindingRates(Element element) {
		k_bind_tf = Double.valueOf(element.getChildTextTrim("k_bind_tf"));
		k_unbind_tf = Double.valueOf(element.getChildTextTrim("k_unbind_tf"));
	}
	
	/**
	 * @return
	 */
	protected Element getElement() {
		Element o = new Element("operator");
		Element t = new Element("tf");
		Element kbt = new Element("k_bind_tf");
		Element kut = new Element("k_unbind_tf");
		t.setText(tf.getName());
		if(tf instanceof Inhibitor) {
			t.setAttribute("type", "inhibitor");
		}
		else if(tf instanceof Activator) {
			t.setAttribute("type", "activator");
		}
		kbt.setText(String.valueOf(k_bind_tf));
		kut.setText(String.valueOf(k_unbind_tf));
		o.addContent(t);
		o.addContent(kbt);
		o.addContent(kut);
		return o;
	}
	
	/**
	 * @return
	 */
	public double getKBindTF() {
		return k_bind_tf;
	}
	
	/**
	 * @return
	 */
	public double getKUnbindTF() {
		return k_unbind_tf;
	}
	
	/**
	 * @return
	 */
	public TF getTF() {
		return tf;
	}
	
	// does not look at binding rates...!
	public boolean equals(Object other) {
		if(other != null && other instanceof Operator) {
			return tf.equals(((Operator)other).tf);
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String result = "Operator (kon=" + k_bind_tf + ", koff=" + k_unbind_tf + "):\n";
		result += "        " + tf.toString();
		return result;
	}
}
