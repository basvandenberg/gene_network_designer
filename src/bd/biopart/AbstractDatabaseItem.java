package bd.biopart;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jdom.Document; 
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import bd.global.Constants;

/**
 * @author Bastiaan van den Berg
 * Abstract database item. 
 */
public abstract class AbstractDatabaseItem implements DatabaseItem {
	
	/**
	 * The name of the database item which is used as identifier.
	 */
	protected final String name;
	
	/**
	 * Constructs a new database item object with the given name. The
	 * data for this item is fetched from the xml file.
	 * @param name The name (unique identifier) of the database item.
	 */
	public AbstractDatabaseItem(String name){
		// store the name
		this.name = name;
		// get the file
		File file = new File(getPath() + name + ".xml");
		// read the item from the file if the file exists
		if(file.exists()) {
			Document doc = getDocument(file);
			init(doc);
		}
		else {
			// System.out.println("File does not exist: " + getPath() + name + ".xml");
		}
	}
	
	/**
	 * @param document
	 */
	protected void init(Document document) {}
	
	/**
	 * @return The path to the different item types
	 */
	public String getPath() {
		String path = "";
		if(this instanceof Device) {
			path = Constants.DEVICE_DIR;
		}
		else if(this instanceof Promoter) {
			path = Constants.PROMOTER_DIR;
		}
		else if(this instanceof RBS) {
			path = Constants.RBS_DIR;
		}
		else if(this instanceof ProteinCoding) {
			path = Constants.PC_DIR;
		}
		else if(this instanceof Terminator) {
			path = Constants.TENMINATOR_DIR;
		}
		else if(this instanceof Reporter) {
			path = Constants.REPORTER_DIR;
		}
		else if(this instanceof Inhibitor) {
			path = Constants.INHIBITOR_DIR;
		}
		else if(this instanceof Activator) {
			path = Constants.ACTIVATOR_DIR;
		}
		else if(this instanceof TFSub) {
			path = Constants.TF_SUB_DIR;
		}
		else if(this instanceof EnvironmentalSignal) {
			path = Constants.SM_DIR;
		}
		return path;
	}
	
	/**
	 * @param file The file that stores the data of a database item.
	 * @return A Document containing the parsed data from file
	 */
	private Document getDocument(File file) {
		Document doc = null;
		try {
			SAXBuilder builder = new SAXBuilder();
			doc = builder.build(file);
		}
		catch(JDOMException jde) {
			jde.printStackTrace();
		}
		catch(IOException ie) {
			ie.printStackTrace();
		}
		return doc;
	}
	
	/**
	 * @return A list with the names of all items in the database.
	 */
	public List<String> getAllNames() {
		File file = new File(getPath());
		List<String> fileNames = Arrays.asList(file.list());
		ArrayList<String> result = new ArrayList<String>();
		for(String s : fileNames) {
			File f = new File(file.getPath()+ File.separator + s);
			if(!f.isDirectory()) {
				int index = s.lastIndexOf('.');
				result.add(s.substring(0,index));
			}
		}
		return result;
	}
	
	/**
	 * Delete the whole database. Use with care.
	 */
	public void deleteAllItems() {
		String path = getPath();
		List<String> names = getAllNames();
		for(String name : names) {
			File file = new File(path + name + ".xml");
			file.delete();
		}
	}
	
	/**
	 * @return An empty fernml xml Document object.
	 */
	protected Document createDocument() {
		// create the root
		Element root = new Element("fernml");
		root.setAttribute("version", "1.0");
		// create document with the root
		Document document = new Document(root);
		return document;
	}
	
	/**
	 * @return The Document with parsed data from the xml file that stores the 
	 * data of a database item.
	 */
	protected abstract Document getDocument();
	
	/**
	 * This function writes the part to file. If a part with the same name already
	 * exists, an error will be printed and the program stops (not very neat...)
	 * The part is written to an xml-file and the location of the file depends on 
	 * the type of the part. For example, a promoter part named pro1 is written to 
	 * data/bioparts/part/promoter/pro1.xml.
	 */
	public void writeDocument() {
		if(getAllNames().contains(name)) {
			System.out.println("Error: trying to write an existing part file: " + name);
			System.exit(-1);
		}
		else {
			File file = new File(getPath()+getName()+".xml");
			try {
				Writer writer = new FileWriter(file);
				XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
				out.output(getDocument(), writer);
				writer.flush();
				writer.close();
			}
			catch(IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see gnd.biopart.DatabaseItem#getName()
	 */
	public String getName() {
		return name;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return name.hashCode();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object other) {
		if(other != null && other instanceof AbstractDatabaseItem) {
			return name.equals(((AbstractDatabaseItem)other).name);
		}
		return false;
	}
}
