/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.io.params;

import java.io.File;

import minerful.io.encdec.csv.CsvEncoder;
import minerful.params.ParamsManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class OutputSpecificationParameters extends ParamsManager {
	public static final String SAVE_AS_CONDEC_PARAM_NAME = "oConDec";
	public static final String SAVE_AS_NUSMV_PARAM_NAME = "oNuSMV";
	public static final String SAVE_AS_CSV_PARAM_NAME = "oCSV";
	public static final String SAVE_AS_JSON_PARAM_NAME = "oJSON";
	public static final String SAVE_PROCESS_DOT_AUTOMATON_PARAM_NAME = "autoDOT";
//	public static final String SAVE_PROCESS_CONDENSED_DOT_AUTOMATON_PARAM_NAME = "dotCond"; // TODO To be done, one day
	public static final String SAVE_PROCESS_TSML_AUTOMATON_PARAM_NAME = "autoTSML";
	public static final String FOLDER_FOR_SAVING_DOT_SUBAUTOMATA_PARAM_NAME = "subautosDOT";

	/** File in which discovered constraints are printed in CSV format. Keep it equal to <code>null</code> to disable this functionality. */
	public File fileToSaveConstraintsAsCSV;
	/** Directory in which the discovered constraints are printed as automata, in separate GraphViz DOT files. Keep it equal to <code>null</code> to disable this functionality. */
	public File folderToSaveDotFilesForPartialAutomata;
	/** File in which the discovered process specification is printed as a TSML representation of an automaton. Keep it equal to <code>null</code> for avto disable this functionality. */
	public File fileToSaveTsmlFileForAutomaton;
	/** File in which the discovered process specification is printed as a GraphViz DOT of an automaton. Keep it equal to <code>null</code> to disable this functionality. */
	public File fileToSaveDotFileForAutomaton;
//	/** File in which the discovered process specification is printed as a GraphViz DOT of an automaton in which multiple transitions are collapsed into one with many labels, for readability reasons. Keep it equal to <code>null</code> to disable this functionality. */
//	public File fileToSaveDotFileForCondensedAutomaton; // TODO One day
	/** File in which the discovered process specification is saved as a Declare XML file. Keep it equal to <code>null</code> to disable this functionality. */
	public File fileToSaveAsConDec;
	/** File in which the discovered process specification is saved as a NuSMV file. Keep it equal to <code>null</code> to disable this functionality. */
	public File fileToSaveAsNuSMV;
	/** File in which the discovered process specification is saved as a JSON file. Keep it equal to <code>null</code> to disable this functionality. */
	public File fileToSaveAsJSON;
	/** Columns to be printed if constraints are printed in CSV format. Notice that this attribute is not associated to a command-line parameter. */
	public CsvEncoder.PRINT_OUT_ELEMENT[] csvColumnsToPrint = CsvEncoder.PRINT_OUT_ELEMENT.values();

    public OutputSpecificationParameters() {
    	this.fileToSaveConstraintsAsCSV = null;
    	this.folderToSaveDotFilesForPartialAutomata = null;
    	this.fileToSaveTsmlFileForAutomaton = null;
    	this.fileToSaveDotFileForAutomaton = null;
//    	this.fileToSaveDotFileForCondensedAutomaton = null; // TODO One day
    	this.fileToSaveAsConDec = null;
    	this.fileToSaveAsNuSMV = null;
    	this.fileToSaveAsJSON = null;
    }

    public OutputSpecificationParameters(Options options, String[] args) {
    	this();
        // parse the command line arguments
    	this.parseAndSetup(options, args);
	}

	public OutputSpecificationParameters(String[] args) {
    	this();
        // parse the command line arguments
    	this.parseAndSetup(new Options(), args);
	}

	@Override
	protected void setup(CommandLine line) {        
    	this.fileToSaveAsJSON = openOutputFile(line, SAVE_AS_JSON_PARAM_NAME);

        this.folderToSaveDotFilesForPartialAutomata = openOutputDir(line, FOLDER_FOR_SAVING_DOT_SUBAUTOMATA_PARAM_NAME);

        this.fileToSaveDotFileForAutomaton = openOutputFile(line, SAVE_PROCESS_DOT_AUTOMATON_PARAM_NAME);
        
//        this.fileToSaveDotFileForCondensedAutomaton = openOutputFile(line, SAVE_PROCESS_CONDENSED_DOT_AUTOMATON_PARAM_NAME); // TODO One day
        
		this.fileToSaveTsmlFileForAutomaton = openOutputFile(line, SAVE_PROCESS_TSML_AUTOMATON_PARAM_NAME);
        
        this.fileToSaveConstraintsAsCSV = openOutputFile(line, SAVE_AS_CSV_PARAM_NAME);

        this.fileToSaveAsConDec = openOutputFile(line, SAVE_AS_CONDEC_PARAM_NAME);
        
        this.fileToSaveAsNuSMV = openOutputFile(line, SAVE_AS_NUSMV_PARAM_NAME);
                
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
        		Option.builder(SAVE_AS_JSON_PARAM_NAME)
        		.hasArg().argName("path")
        		.longOpt("save-as-json")
        		.desc("path of the file in which to save the discovered process specification as JSON")
        		.type(String.class)
        		.build()
        		);
        options.addOption(
        		Option.builder(SAVE_PROCESS_DOT_AUTOMATON_PARAM_NAME)
        		.hasArg().argName("path")
        		.longOpt("save-automaton-dot")
        		.desc(
        				"write a Graphviz DOT format of a finite state automaton representing the declarative process"
        		)
        		.type(String.class)
        		.build()
        		);
//        options.addOption( // TODO One day
//        		Option.builder(SAVE_PROCESS_CONDENSED_DOT_AUTOMATON_PARAM_NAME)
//        		.hasArg().argName("path")
//        		.longOpt("save-cond-automaton-dot")
//        		.desc(
//        				"write a Graphviz DOT format of a condensed, more readable finite state automaton representing the declarative process"
//        		)
//        		.type(String.class)
//        		.create(SAVE_PROCESS_CONDENSED_DOT_AUTOMATON_PARAM_NAME)
//        		);
        options.addOption(
        		Option.builder(SAVE_PROCESS_TSML_AUTOMATON_PARAM_NAME)
        		.hasArg().argName("path")
        		.longOpt("save-automaton-tsml")
        		.desc(
        				"write a TSML format of a finite state automaton representing the mined process on the given file"
        		)
        		.type(String.class)
        		.build()
        		);
        options.addOption(
        		Option.builder(FOLDER_FOR_SAVING_DOT_SUBAUTOMATA_PARAM_NAME)
        		.hasArg().argName("path")
        		.longOpt("subautom-folder")
        		.desc("write the Graphviz DOT format of activities' finite state sub-automata on separate files, within the given folder")
        		.type(String.class)
        		.build()
        		);
        options.addOption(
				Option.builder(SAVE_AS_CSV_PARAM_NAME)
				.hasArg().argName("path")
				.longOpt("save-as-csv")
				.desc("print results in CSV format into the specified file")
				.type(String.class)
				.build()
        		);
		options.addOption(
				Option.builder(SAVE_AS_CONDEC_PARAM_NAME)
        		.hasArg().argName("path")
        		.longOpt("save-as-condec")
        		.desc("print the discovered process as a Declare map (ConDec) into the specified file")
        		.type(String.class)
        		.build()
        		);
  options.addOption(
        Option.builder(SAVE_AS_NUSMV_PARAM_NAME)
        .hasArg().argName("path")
            .longOpt("print-replay-autom")
            .desc("print the discovered process as an input script for NuSMV into the specified file")
        		.type(String.class)
        		.build()
        		);

		return options;
	}
}