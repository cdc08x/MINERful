/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.io.params;

import java.io.File;

import minerful.params.ParamsManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

public class OutputModelParameters extends ParamsManager {
	public static final String SAVE_AS_CONDEC_PARAM_NAME = "condec";
	public static final String SAVE_AS_CSV_PARAM_NAME = "CSV";
	public static final String SAVE_AS_XML_PARAM_NAME = "oMF";
	public static final String SAVE_AS_JSON_PARAM_NAME = "JSON";
	public static final String SAVE_PROCESS_DOT_AUTOMATON_PARAM_NAME = "pA";
	public static final String SAVE_PROCESS_TSML_AUTOMATON_PARAM_NAME = "pTSML";
	public static final String FOLDER_FOR_SAVING_DOT_SUBAUTOMATA_PARAM_NAME = "pSAF";
	public static final String SAVE_XML_WEIGHTED_AUTOMATON_PARAM_NAME = "pXWA";
	public static final String FOLDER_FOR_SAVING_XML_WEIGHTED_SUBAUTOMATA_PARAM_NAME = "pXWSAF";

	/** File in which discovered constraints are printed in CSV format. Keep it equal to <code>null</code> for avoiding such print-out. */
	public File fileToSaveConstraintsAsCSV;
	/** Directory in which the discovered constraints are printed as automata, in separate GraphViz DOT files. Keep it equal to <code>null</code> for avoiding such print-outs. */
	public File folderToSaveDotFilesForPartialAutomata;
	/** File in which the discovered process model is printed as a TSML representation of an automaton. Keep it equal to <code>null</code> for avoiding such print-out. */
	public File fileToSaveTsmlFileForAutomaton;
	/** File in which the discovered process model is printed as a GraphViz DOT of an automaton. Keep it equal to <code>null</code> for avoiding such print-out. */
	public File fileToSaveDotFileForAutomaton;
	/** File in which the discovered process model is saved as a Declare XML file. Keep it equal to <code>null</code> for avoiding such print-out. */
	public File fileToSaveAsConDec;
	/** File in which the discovered process model is printed as an XML representation of an automaton. Keep it equal to <code>null</code> for avoiding such print-out. */
	public File fileToSaveXmlFileForAutomaton;
	/** Directory in which the discovered constraints are printed as automata, in separate XML files. Keep it equal to <code>null</code> for avoiding such print-outs. */
	public File folderToSaveXmlFilesForPartialAutomata;
	/** File in which the discovered process model is saved as an XML file. Keep it equal to <code>null</code> for avoiding such print-out. */
    public File fileToSaveAsXML;
	/** File in which the discovered process model is saved as a JSON file. Keep it equal to <code>null</code> for avoiding such print-out. */
	public File fileToSaveAsJSON;

    public OutputModelParameters() {
    	this.fileToSaveConstraintsAsCSV = null;
    	this.folderToSaveDotFilesForPartialAutomata = null;
    	this.fileToSaveTsmlFileForAutomaton = null;
    	this.fileToSaveDotFileForAutomaton = null;
    	this.fileToSaveAsConDec = null;
    	this.fileToSaveXmlFileForAutomaton = null;
    	this.folderToSaveXmlFilesForPartialAutomata = null;
    	this.fileToSaveAsXML = null;
    	this.fileToSaveAsJSON = null;
    }

    public OutputModelParameters(Options options, String[] args) {
    	this();
        // parse the command line arguments
    	this.parseAndSetup(options, args);
	}

	public OutputModelParameters(String[] args) {
    	this();
        // parse the command line arguments
    	this.parseAndSetup(new Options(), args);
	}

	@Override
	protected void setup(CommandLine line) {
    	String procSchemeFilePath = line.getOptionValue(SAVE_AS_XML_PARAM_NAME);
        if (procSchemeFilePath != null) {
        	this.fileToSaveAsXML = new File(procSchemeFilePath);
        }
        String jsonFilePath = line.getOptionValue(SAVE_AS_JSON_PARAM_NAME);
        if (jsonFilePath != null) {
        	this.fileToSaveAsJSON = new File(jsonFilePath);
        }
		String
			folderToSaveDotFilesForPartialAutomataPath = line.getOptionValue(FOLDER_FOR_SAVING_DOT_SUBAUTOMATA_PARAM_NAME);
	    if (folderToSaveDotFilesForPartialAutomataPath != null) {
	        this.folderToSaveDotFilesForPartialAutomata = new File(folderToSaveDotFilesForPartialAutomataPath);
	        if (        !this.folderToSaveDotFilesForPartialAutomata.exists()
	        		||	!this.folderToSaveDotFilesForPartialAutomata.isDirectory()
	                ||  !this.folderToSaveDotFilesForPartialAutomata.canWrite()
	                ) {
	            throw new IllegalArgumentException("Unwritable directory: " + folderToSaveDotFilesForPartialAutomata);
	        }
	    }
		String fileToSaveDotFileForAutomatonPath = line.getOptionValue(SAVE_PROCESS_DOT_AUTOMATON_PARAM_NAME);
        if (fileToSaveDotFileForAutomatonPath != null) {
            this.fileToSaveDotFileForAutomaton = new File(fileToSaveDotFileForAutomatonPath);
        }
        
		String fileToSaveTsmlFileForAutomatonPath = line.getOptionValue(SAVE_PROCESS_TSML_AUTOMATON_PARAM_NAME);
        if (fileToSaveTsmlFileForAutomatonPath != null) {
            this.fileToSaveTsmlFileForAutomaton = new File(fileToSaveTsmlFileForAutomatonPath);
        }
        
        String fileToSaveConstraintsCsvString = line.getOptionValue(SAVE_AS_CSV_PARAM_NAME);
        if (fileToSaveConstraintsCsvString != null) {
            this.fileToSaveConstraintsAsCSV = new File(fileToSaveConstraintsCsvString);
        }
        
        String fileToSaveConDecDefinitionString = line.getOptionValue(SAVE_AS_CONDEC_PARAM_NAME);
        if (fileToSaveConDecDefinitionString != null) {
            this.fileToSaveAsConDec = new File(fileToSaveConDecDefinitionString);
        }
        
        String fileToSaveXmlFileForAutomatonString = line.getOptionValue(SAVE_XML_WEIGHTED_AUTOMATON_PARAM_NAME);
        if (fileToSaveXmlFileForAutomatonString != null) {
        	this.fileToSaveXmlFileForAutomaton = new File(fileToSaveXmlFileForAutomatonString);
        }
		String
			folderToSaveXmlFilesForPartialAutomataPath = line.getOptionValue(FOLDER_FOR_SAVING_XML_WEIGHTED_SUBAUTOMATA_PARAM_NAME);
	    if (folderToSaveXmlFilesForPartialAutomataPath != null) {
	        this.folderToSaveXmlFilesForPartialAutomata = new File(folderToSaveXmlFilesForPartialAutomataPath);
	        if (        !this.folderToSaveXmlFilesForPartialAutomata.exists()
	        		||	!this.folderToSaveXmlFilesForPartialAutomata.isDirectory()
	                ||  !this.folderToSaveXmlFilesForPartialAutomata.canWrite()
	                ) {
	            throw new IllegalArgumentException("Unwritable directory: " + folderToSaveXmlFilesForPartialAutomataPath);
	        }
	    }
    }
    
	@Override
    public Options addParseableOptions(Options options) {
		Options myOptions = listParseableOptions();
		for (Object myOpt: myOptions.getOptions())
			options.addOption((Option)myOpt);
        return options;
	}
	
	@Override
    public Options listParseableOptions() {
    	return parseableOptions();
    }
	@SuppressWarnings("static-access")
	public static Options parseableOptions() {
		Options options = new Options();
        options.addOption(
        		OptionBuilder
        		.hasArg().withArgName("path")
        		.withLongOpt("save-as-xml")
        		.withDescription("path of the file in which to save the discovered process model as XML")
        		.withType(new String())
        		.create(SAVE_AS_XML_PARAM_NAME)
        		);
        options.addOption(
        		OptionBuilder
        		.hasArg().withArgName("path")
        		.withLongOpt("save-as-json")
        		.withDescription("path of the file in which to save the discovered process model as JSON")
        		.withType(new String())
        		.create(SAVE_AS_JSON_PARAM_NAME)
        		);
        options.addOption(
        		OptionBuilder
        		.hasArg().withArgName("path")
        		.withLongOpt("save-automaton")
        		.withDescription(
        				"write a Graphviz DOT format of a finite state automaton representing the mined process on the given file"
        		)
        		.withType(new String())
        		.create(SAVE_PROCESS_DOT_AUTOMATON_PARAM_NAME)
        		);
        options.addOption(
        		OptionBuilder
        		.hasArg().withArgName("path")
        		.withLongOpt("save-automaton-tsml")
        		.withDescription(
        				"write a TSML format of a finite state automaton representing the mined process on the given file"
        		)
        		.withType(new String())
        		.create(SAVE_PROCESS_TSML_AUTOMATON_PARAM_NAME)
        		);
        options.addOption(
        		OptionBuilder
        		.hasArg().withArgName("path")
        		.withLongOpt("subautom-folder")
        		.withDescription("write the Graphviz DOT format of activities' finite state sub-automata on separate files, within the given folder")
        		.withType(new String())
        		.create(FOLDER_FOR_SAVING_DOT_SUBAUTOMATA_PARAM_NAME)
        		);
        options.addOption(OptionBuilder
        		.hasArg().withArgName("path")
        		.withLongOpt("save-as-csv")
        		.withDescription("print results in CSV format into the specified file")
        		.withType(new String())
        		.create(SAVE_AS_CSV_PARAM_NAME)
        		);
        options.addOption(OptionBuilder
        		.hasArg().withArgName("path")
        		.withLongOpt("save-as-condec")
        		.withDescription("print the discovered process as a Declare map (ConDec) into the specified file")
        		.withType(new String())
        		.create(SAVE_AS_CONDEC_PARAM_NAME)
        		);
        options.addOption(OptionBuilder
        		.hasArg().withArgName("path")
        		.withLongOpt("print-weighted-autom")
        		.withDescription(
        				attachInstabilityWarningToDescription("print the discovered process in weighted automaton XML format, into the specified file")
        		)
        		.withType(new String())
        		.create(SAVE_XML_WEIGHTED_AUTOMATON_PARAM_NAME)
        		);
        options.addOption(
        		OptionBuilder
        		.hasArg().withArgName("path")
        		.withLongOpt("xml-subautom-folder")
        		.withDescription("write the weighted automaton XML format of activities' finite state sub-automata on separate files, within the given folder")
        		.withType(new String())
        		.create(FOLDER_FOR_SAVING_XML_WEIGHTED_SUBAUTOMATA_PARAM_NAME)
        		);
       return options;
	}
}