package bd.model;



import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import bd.global.Constants;

public class FernMLWriter implements ModelWriter {
	
	public void writeModel(Model model) {
		Document document = buildDocument(model);
		File file = new File(Constants.TMP_MODEL_DIR + model.device.getName() + ".fernml");
		writeDocument(document, file);
	}
	
	private Document buildDocument(Model model) {
		
		// Create the root
		Element root = new Element("fernml");
		root.setAttribute("version", "1.0");
		
		// Create species and reactions Element
		Element species = new Element("listOfSpecies");
		Element reactions = new Element("listOfReactions");
		
		// Add all species from speciesSet as Element
		for(Species s : model.speciesList) {
			species.addContent(getSpeciesElement(s));
		}
		
		//Add all reactions from reactionSet as Element
		for(Reaction r : model.reactionList) {
			reactions.addContent(getReactionElement(r));
		}
		
		// Add species and reaction Element to the root
		root.addContent(species);
		root.addContent(reactions);
		
		// create the document with the filled root
		Document document = new Document(root);
		return document;
	}
	
	private Element getSpeciesElement(Species species) {
		Element s = new Element("species");
		s.setAttribute("name", species.getName());
		s.setAttribute("initialAmount", String.valueOf(species.getInitialAmount()));
		return s;
	}
	
	private Element getReactionElement(Reaction reaction) {
		
		Element r = new Element("reaction");
		r.setAttribute("kineticConstant", String.valueOf(reaction.getRateConstant()));
		
		Element reactants = new Element("listOfReactants");
		for(String name : reaction.getReactants()) {
			Element reactant = new Element("speciesReference");
			reactant.setAttribute("name", name);
			reactants.addContent(reactant);
		}
		
		Element products = new Element("listOfProducts");
		for(String name : reaction.getProducts()) {
			Element product = new Element("speciesReference");
			product.setAttribute("name", name);
			products.addContent(product);
		}
		
		r.addContent(reactants);
		r.addContent(products);
		
		return r;
	}
	
	private void writeDocument(Document document, File file) {
		try {
			Writer writer = new FileWriter(file);
			XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
			out.output(document, writer);
			writer.flush();
			writer.close();
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
