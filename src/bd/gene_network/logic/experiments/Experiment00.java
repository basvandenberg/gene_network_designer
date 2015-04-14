/**
 * 
 */
package bd.gene_network.logic.experiments;

import bd.biopart.BioPartDatabaseBuilder;
import bd.gene_network.logic.SimAllLogicGeneNetworks;

/**
 * @author Bastiaan van den Berg
 *
 */
public class Experiment00 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// build parts registry that provides 15,360 possible devices
		BioPartDatabaseBuilder.deleteDatabase();
		BioPartDatabaseBuilder.experiment0();
		
		// run the all devices program...
		String[] arguments = {"Demultiplexer0"};
		SimAllLogicGeneNetworks.main(arguments);
		
		// used SETTINGS!!!
		/*<logic_device_settings name="Demultiplexer0">

		  <gene_network>Demultiplexer</gene_network>

		  <input id="in0">iim0</input>
		  <input id="in1">iim1</input>

		  <output id="out0">reporter0</output>
		  <output id="out1">reporter1</output>

		  <binary_timing_diagram>
		    <plot id="in0" >00110</plot>
		    <plot id="in1" >01100</plot>
		    <plot id="out0">01000</plot>
		    <plot id="out1">00100</plot>
		  </binary_timing_diagram>

		  <state_time>7200</state_time>
		  <visual>true</visual>

		</logic_device_settings>*/
	}

}
