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
	public static final String PRINT_PROCESS_DOT_AUTOMATON_PARAM_NAME = "pA";
	public static final String PRINT_PROCESS_TSML_AUTOMATON_PARAM_NAME = "pTSML";
	public static final String FOLDER_FOR_DOT_SUBAUTOMATA_PARAM_NAME = "pSAF";
	public static final String PRINT_CONDEC_PARAM_NAME = "condec";
	public static final String PRINT_XML_WEIGHTED_AUTOMATON_PARAM_NAME = "pXWA";
	public static final String FOLDER_FOR_XML_WEIGHTED_SUBAUTOMATA_PARAM_NAME = "pXWSAF";
	public static final String PRINT_CSV_PARAM_NAME = "CSV";
	public static final String PROCESS_SCHEME_OUT_PATH_PARAM_NAME = "oMF";

	public File fileToSaveConstraintsCsv;
	public File folderToSaveDotFilesForPartialAutomata;
	public File fileToSaveTsmlFileForAutomaton;
	public File fileToSaveDotFileForAutomaton;
	public File fileToSaveConDecDefinition;
	public File fileToSaveXmlFileForAutomaton;
	public File folderToSaveXmlFilesForPartialAutomata;
    public File processModelOutputFile;
    
    public OutputModelParameters() {
    	this.fileToSaveConstraintsCsv = null;
    	this.folderToSaveDotFilesForPartialAutomata = null;
    	this.fileToSaveTsmlFileForAutomaton = null;
    	this.fileToSaveDotFileForAutomaton = null;
    	this.fileToSaveConDecDefinition = null;
    	this.fileToSaveXmlFileForAutomaton = null;
    	this.folderToSaveXmlFilesForPartialAutomata = null;
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
    	String procSchemeFilePath = line.getOptionValue(PROCESS_SCHEME_OUT_PATH_PARAM_NAME);
        if (procSchemeFilePath != null) {
        	this.processModelOutputFile = new File(procSchemeFilePath);
        }
		String
			folderToSaveDotFilesForPartialAutomataPath = line.getOptionValue(FOLDER_FOR_DOT_SUBAUTOMATA_PARAM_NAME);
	    if (folderToSaveDotFilesForPartialAutomataPath != null) {
	        this.folderToSaveDotFilesForPartialAutomata = new File(folderToSaveDotFilesForPartialAutomataPath);
	        if (        !this.folderToSaveDotFilesForPartialAutomata.exists()
	        		||	!this.folderToSaveDotFilesForPartialAutomata.isDirectory()
	                ||  !this.folderToSaveDotFilesForPartialAutomata.canWrite()
	                ) {
	            throw new IllegalArgumentException("Unwritable directory: " + folderToSaveDotFilesForPartialAutomata);
	        }
	    }
		String fileToSaveDotFileForAutomatonPath = line.getOptionValue(PRINT_PROCESS_DOT_AUTOMATON_PARAM_NAME);
        if (fileToSaveDotFileForAutomatonPath != null) {
            this.fileToSaveDotFileForAutomaton = new File(fileToSaveDotFileForAutomatonPath);
        }
        
		String fileToSaveTsmlFileForAutomatonPath = line.getOptionValue(PRINT_PROCESS_TSML_AUTOMATON_PARAM_NAME);
        if (fileToSaveTsmlFileForAutomatonPath != null) {
            this.fileToSaveTsmlFileForAutomaton = new File(fileToSaveTsmlFileForAutomatonPath);
        }
        
        String fileToSaveConstraintsCsvString = line.getOptionValue(PRINT_CSV_PARAM_NAME);
        if (fileToSaveConstraintsCsvString != null) {
            this.fileToSaveConstraintsCsv = new File(fileToSaveConstraintsCsvString);
        }
        
        String fileToSaveConDecDefinitionString = line.getOptionValue(PRINT_CONDEC_PARAM_NAME);
        if (fileToSaveConDecDefinitionString != null) {
            this.fileToSaveConDecDefinition = new File(fileToSaveConDecDefinitionString);
        }
        
        String fileToSaveXmlFileForAutomatonString = line.getOptionValue(PRINT_XML_WEIGHTED_AUTOMATON_PARAM_NAME);
        if (fileToSaveXmlFileForAutomatonString != null) {
        	this.fileToSaveXmlFileForAutomaton = new File(fileToSaveXmlFileForAutomatonString);
        }
		String
			folderToSaveXmlFilesForPartialAutomataPath = line.getOptionValue(FOLDER_FOR_XML_WEIGHTED_SUBAUTOMATA_PARAM_NAME);
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
        		.withLongOpt("proc-out")
        		.withDescription("path to write the discovered process scheme in")
        		.withType(new String())
        		.create(PROCESS_SCHEME_OUT_PATH_PARAM_NAME)
        		);
        options.addOption(
        		OptionBuilder
        		.hasArg().withArgName("path")
        		.withLongOpt("print-automaton")
        		.withDescription(
        				attachInstabilityWarningToDescription("write a Graphviz DOT format of a finite state automaton representing the mined process on the given file")
        		)
        		.withType(new String())
        		.create(PRINT_PROCESS_DOT_AUTOMATON_PARAM_NAME)
        		);
        options.addOption(
        		OptionBuilder
        		.hasArg().withArgName("path")
        		.withLongOpt("print-tsml")
        		.withDescription(
        				attachInstabilityWarningToDescription("write a TSML format of a finite state automaton representing the mined process on the given file")
        		)
        		.withType(new String())
        		.create(PRINT_PROCESS_TSML_AUTOMATON_PARAM_NAME)
        		);
        options.addOption(
        		OptionBuilder
        		.hasArg().withArgName("path")
        		.withLongOpt("subautom-folder")
        		.withDescription("write the Graphviz DOT format of activities' finite state sub-automata on separate files, within the given folder")
        		.withType(new String())
        		.create(FOLDER_FOR_DOT_SUBAUTOMATA_PARAM_NAME)
        		);
        options.addOption(OptionBuilder
        		.hasArg().withArgName("path")
        		.withLongOpt("print-csv")
        		.withDescription("print results in CSV format into the specified file")
        		.withType(new String())
        		.create(PRINT_CSV_PARAM_NAME)
        		);
        options.addOption(OptionBuilder
        		.hasArg().withArgName("path")
        		.withLongOpt("print-condec")
        		.withDescription("print the discovered process as a Declare map (ConDec) into the specified file")
        		.withType(new String())
        		.create(PRINT_CONDEC_PARAM_NAME)
        		);
        options.addOption(OptionBuilder
        		.hasArg().withArgName("path")
        		.withLongOpt("print-weighted-autom")
        		.withDescription(
        				attachInstabilityWarningToDescription("print the discovered process in weighted automaton XML format, into the specified file")
        		)
        		.withType(new String())
        		.create(PRINT_XML_WEIGHTED_AUTOMATON_PARAM_NAME)
        		);
        options.addOption(
        		OptionBuilder
        		.hasArg().withArgName("path")
        		.withLongOpt("xml-subautom-folder")
        		.withDescription("write the weighted automaton XML format of activities' finite state sub-automata on separate files, within the given folder")
        		.withType(new String())
        		.create(FOLDER_FOR_XML_WEIGHTED_SUBAUTOMATA_PARAM_NAME)
        		);
       return options;
	}
}