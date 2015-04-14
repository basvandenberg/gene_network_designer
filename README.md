# Gene Network Designer

This software is written as part of my masters thesis at the Delft University
of Technology. The software is not extensively tested and the documentation is
very limited. For more information about the software and about the project,
consult my masters thesis document:
http://repository.tudelft.nl/view/ir/uuid:fff3c287-ff65-454a-8ec1-42425d94280c/

The java source files can be found in the src directory, jar-files of the
libraries that are used by the software can be found in the
geneNetworkDesigner_lib directory. The javadoc documentation is stored in the
doc directory (use index.html to browse the documentation).

The data directory contains:
- The bioparts database (a directory-structure with xml files).
- gene_network directory - not used currently, but do not remove it!
- gene_network_logic directory - Stores files with user input for a simulation. 
  Three examples are provided.
- gene_network_template directory - Stores gene network templates.
- tmp_data - Stores simulation data. Each time a simulation is run, the data is
  stored in this directory. The user has to clean this directory manually!
- tmp_model - Stores the models that are used for simulation (fernml-files)

To build network templates and run simulations with it consult the provided 
examples and the documentation on the download site.

## Requirements

The following software must be installed:
- Java JRE (http://java.sun.com/javase/downloads/index.jsp)

For visualization of a simulation, gnuplot is required (but the software
runs without it as well)
- http://www.gnuplot.info/download.html

## Install

Unzip the geneNetworkDesigner.tar.gz file to a location of your choice.

### Test

Use the command line to navigate to the unarchived (unzipped) 
geneNetworkDesigner directory. Type:

java -jar geneNetworkDesigner.jar

and press enter. The software should start loading the bioparts database.

## Tutorial

This tutorial runs a simulation on a randomly selected Decoder device. 

1. Run a command line tool.
2. Go to the GeneNetworkDesigner directory.
3. Run the command 'java -jar LogicGeneNetworkDesigner.jar'. As soon as the 
   bioparts database is loaded in memory, the following options should be 
   presented:
   
   1. Simulate a random logic gene network
   2. Simulate a logic gene network
   3. Simulate all possible logic gene networks
   4. (Re)Build bioparts database
   
4. Choose option 1 and press enter.
5. Fill in the name of the device 'Decoder0' and press enter (do not enter the
   quotation marks, the last character is a zero).
6. A Decoder device is selected randomly and the software runs a simulation. 
   When gnuplot is installed and added to your path, then the output plot is 
   visualized real-time. Otherwise, only the resulting scores of the simulation 
   are provided as output.

Instead of 'Decoder0', you can also run a simulation for a randomly selected
'Demultiplexer0' or 'DLatch0'.

When running the simulation on a randomly selected decoder device, the software
outputs the string representation of this device, such as:

    |Decoder0[(pm_i0m1_i0d3_10,rbs18,pc_reporter0,t);(pm_12,rbs0,pc_iim1,t);(pm_17,rbs15,pc_iim0,t);(pm_iim1_i0m1_18,rbs2,pc_reporter2,t);(pm_iim0_i0d3_10,rbs7,pc_reporter1,t);(pm_iim0_iim1_9,rbs8,pc_reporter3,t);(pm_iim0_16,rbs9,pc_i0m1,t);(pm_iim1_3,rbs16,pc_i0d3_subm,t)],{es_iim1,es_iim0}|

It starts with the name of the device. Between the square brackets a list of 
protein generators is given. The protein generators are separated by semi-colons
. Each protein generator is given between brackets and consists of four
biological parts (promoter, RBS, protein coding part, terminator) separated by 
commas.

We can run a simulation on the same device we just randomly selected.

7. Copy the string representation that the software printed to screen in step 6.
8. Choose option 2 (Simulate a logic gene network).
9. Paste the string representation of the device and press enter.

The software runs a simulation on the same device now.

