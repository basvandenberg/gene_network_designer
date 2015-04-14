package bd.gene_network;

import java.io.FileNotFoundException;

import bd.biopart.BioPartDatabase;
import bd.biopart.BioPartDatabaseBuilder;

public class TestGeneNetworkBuilder {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
			BioPartDatabaseBuilder.deleteDatabase();
			BioPartDatabaseBuilder.testDemultiplexer0();
			GeneNetworkBuilder ldb = new GeneNetworkBuilder("Demultiplexer0");
			ldb.getAllGeneNetworks();
			BioPartDatabase.instance().destroy();
			
			BioPartDatabaseBuilder.deleteDatabase();
			BioPartDatabaseBuilder.testDemultiplexer1();
			ldb = new GeneNetworkBuilder("Demultiplexer0");
			ldb.getAllGeneNetworks();
			BioPartDatabase.instance().destroy();
			
			BioPartDatabaseBuilder.deleteDatabase();
			BioPartDatabaseBuilder.testCElement0();
			ldb = new GeneNetworkBuilder("CElement0");
			ldb.getAllGeneNetworks();
			BioPartDatabase.instance().destroy();
			
			BioPartDatabaseBuilder.deleteDatabase();
			BioPartDatabaseBuilder.testCElement1();
			ldb = new GeneNetworkBuilder("CElement0");
			ldb.getAllGeneNetworks();
			BioPartDatabase.instance().destroy();
			
			BioPartDatabaseBuilder.deleteDatabase();
			BioPartDatabaseBuilder.testDLatch0();
			ldb = new GeneNetworkBuilder("DLatch0");
			ldb.getAllGeneNetworks();
		}
		catch(FileNotFoundException fnfe) {
			System.out.println(fnfe.getMessage());
		}
	}

}
