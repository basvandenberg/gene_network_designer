/**
 * 
 */
package bd.gene_network.logic.experiments;

import bd.biopart.BioPartDatabaseBuilder;
import bd.gene_network.logic.SimAllLogicGeneNetworks;

/**
 * @author bastiaan
 *
 */
public class Experiment10 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// 
		// 
		BioPartDatabaseBuilder.deleteDatabase();
		BioPartDatabaseBuilder.experiment1();
		
		// run the all devices program...
		String[] arguments = {"DLatch0"};
		SimAllLogicGeneNetworks.main(arguments);
		
		/* used SETTINGS
<logic_device_settings name="DLatch0">

  <gene_network>DLatch</gene_network>

  <input id="in0">iim0</input>
  <input id="in1">iim1</input>

  <output id="out0">reporter0</output>

  <binary_timing_diagram>
    <plot id="in0" >010011100</plot>
    <plot id="in1" >111001001</plot>
    <plot id="out0">111101000</plot>
  </binary_timing_diagram>

  <state_time>14400</state_time>
  <visual>false</visual>

</logic_device_settings>
		 */

	}

}
