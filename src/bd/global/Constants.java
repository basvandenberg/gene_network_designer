package bd.global;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Constants {

	// Root directory
	public static final String ROOT_DIR = "data" + File.separator;
	
	// DNA parts directories
	public static final String DNA_DATABASE_DIR = ROOT_DIR + "bioparts" + File.separator;
	
	public static final String PART_DIR = DNA_DATABASE_DIR + "part" + File.separator;
	public static final String POOL_DIR = DNA_DATABASE_DIR + "pool" + File.separator;
	
	public static final String OPERATOR_DIR = PART_DIR + "operator" + File.separator;
	public static final String PROMOTER_DIR = PART_DIR + "promoter" + File.separator;
	public static final String PC_DIR = PART_DIR + "proteinCoding" + File.separator;
	public static final String RBS_DIR = PART_DIR + "rbs" + File.separator;
	public static final String TENMINATOR_DIR = PART_DIR + "terminator" + File.separator;
	
	public static final String PROTEIN_DIR = POOL_DIR + "protein" + File.separator;
	public static final String SM_DIR = POOL_DIR + "environmentalSignal" + File.separator;
	
	public static final String TF_DIR = PROTEIN_DIR + "tf" + File.separator;
	public static final String TF_SUB_DIR = PROTEIN_DIR + "tf_sub" + File.separator;
	public static final String REPORTER_DIR = PROTEIN_DIR + "reporter" + File.separator;
	
	public static final String INHIBITOR_DIR = TF_DIR + "inhibitor" + File.separator;
	public static final String ACTIVATOR_DIR = TF_DIR + "activator" + File.separator;
	
	// Devices dir
	public static final String DEVICE_DIR = ROOT_DIR + "gene_network" + File.separator;
	
	// Gene network directory
	public static final String GENE_NETWORK_DIR = ROOT_DIR + "gene_network_template" + File.separator;
	
	// Logic devices directory
	public static final String LOGIC_DEVICE_DIR = ROOT_DIR + "gene_network_logic" + File.separator;
	
	// tmp dirs
	public static final String TMP_MODEL_DIR = ROOT_DIR + "tmp_models" + File.separator;
	public static final String TMP_DATA_DIR = ROOT_DIR + "tmp_data" + File.separator;
	
	/**
	 * Returns a writer to a newly created data file. 
	 * @param fileName
	 * @return
	 */
	public static BufferedWriter simulationDataFile(String fileName) {
		BufferedWriter writer = null;
		int counter = 0;
		String path = Constants.TMP_DATA_DIR + File.separator + fileName;
		File dataFile = new File(path + counter + ".dat");
		try {
			while(!dataFile.createNewFile()) {
				counter++;
				dataFile = new File(path + counter + ".dat");
			}
			writer = new BufferedWriter(new FileWriter(dataFile));
		}
		catch(IOException ioe) {
			ioe.printStackTrace();
			System.out.println("ERROR: An IO error occured, when creating the data output file.");
		}
		dataFile.setReadOnly();
		return writer;
	}
}
