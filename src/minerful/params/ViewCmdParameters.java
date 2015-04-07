package minerful.params;

import java.io.File;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

public class ViewCmdParameters extends ParamsManager {
	public static enum ConstraintsSorting {
		type, support, interest;
	}

	public static final String MACHINE_READABLE_RESULTS_PARAM_NAME = "mR";
	public static final String CONSTRAINTS_SORTING_TYPE_NAME = "cS";
	public static final String CONSTRAINTS_NO_FOLDING_NAME = "noCF";
	public static final char THRESHOLD_PARAM_NAME = 't';
	public static final char INTEREST_PARAM_NAME = 'i';
	public static final char CONFIDENCE_PARAM_NAME = 'c';
	public static final String PRINT_PROCESS_DOT_AUTOMATON_PARAM_NAME = "pA";
	public static final String PRINT_PROCESS_TSML_AUTOMATON_PARAM_NAME = "pTSML";
	public static final String FOLDER_FOR_DOT_SUBAUTOMATA_PARAM_NAME = "pSAF";
	public static final String PRINT_CSV_PARAM_NAME = "CSV";
	public static final String PRINT_CONDEC_PARAM_NAME = "condec";
	public static final String PRINT_XML_WEIGHTED_AUTOMATON_PARAM_NAME = "pXWA";
	public static final String FOLDER_FOR_XML_WEIGHTED_SUBAUTOMATA_PARAM_NAME = "pXWSAF";

	public static final Double DEFAULT_THRESHOLD = 1.0;
	public static final Double DEFAULT_INTEREST = 0.0;
	public static final Double DEFAULT_CONFIDENCE = 0.0;

    public Boolean machineReadableResults;
    public ConstraintsSorting constraintsSorting;
    public Boolean noFoldingRequired;
    public Double supportThreshold;
    public Double confidenceThreshold;
	public Double interestThreshold;
	public File fileToSaveConstraintsCsv;
	public File folderToSaveDotFilesForPartialAutomata;
	public File fileToSaveTsmlFileForAutomaton;
	public File fileToSaveDotFileForAutomaton;
	public File fileToSaveConDecDefinition;
	public File fileToSaveXmlFileForAutomaton;
	public File folderToSaveXmlFilesForPartialAutomata;


	/**
	 * 
	 */
	public ViewCmdParameters() {
	    machineReadableResults = false;
	    constraintsSorting = ConstraintsSorting.type;
	    noFoldingRequired = false;
	    supportThreshold = DEFAULT_THRESHOLD;
	    confidenceThreshold = DEFAULT_CONFIDENCE;
		interestThreshold = DEFAULT_INTEREST;
		fileToSaveConstraintsCsv = null;
		folderToSaveDotFilesForPartialAutomata = null;
		fileToSaveTsmlFileForAutomaton = null;
		fileToSaveDotFileForAutomaton = null;
		fileToSaveConDecDefinition = null;
		fileToSaveXmlFileForAutomaton = null;
		folderToSaveXmlFilesForPartialAutomata = null;
	}


    public ViewCmdParameters(Options options, String[] args) {
    	this();
        // parse the command line arguments
    	this.parseAndSetup(options, args);
	}

    public ViewCmdParameters(String[] args) {
    	this();
        // parse the command line arguments
    	this.parseAndSetup(new Options(), args);
	}

	@Override
	protected void setup(CommandLine line) {
        this.constraintsSorting =
        		Enum.valueOf(ConstraintsSorting.class,
        				line.getOptionValue(
        						CONSTRAINTS_SORTING_TYPE_NAME,
        						this.constraintsSorting.toString()
        						)
        				);
        this.machineReadableResults = line.hasOption(MACHINE_READABLE_RESULTS_PARAM_NAME);
        this.noFoldingRequired = line.hasOption(CONSTRAINTS_NO_FOLDING_NAME);
		this.supportThreshold = Double.valueOf(
				line.getOptionValue(
						THRESHOLD_PARAM_NAME,
						this.supportThreshold.toString()
				)
		);
		this.interestThreshold = Double.valueOf(
				line.getOptionValue(
						INTEREST_PARAM_NAME,
						this.interestThreshold.toString()
						)
				);
		this.confidenceThreshold = Double.valueOf(
				line.getOptionValue(
						CONFIDENCE_PARAM_NAME,
						this.confidenceThreshold.toString()
						)
				);
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
	
	@SuppressWarnings("static-access")
	public static Options parseableOptions() {
		Options options = new Options();
        options.addOption(
        		OptionBuilder
        		.hasArg().withArgName("type")
        		.withLongOpt("sort-constraints")
        		.withDescription("Sorting policy for constraints of the discovered process: " + printValues(ConstraintsSorting.values()))
        		.withType(new String())
        		.create(CONSTRAINTS_SORTING_TYPE_NAME)
        		);
        options.addOption(
        		OptionBuilder
        		.withLongOpt("nofolding")
        		.withDescription("avoid the discovered constraints to be folded within implying activities")
        		.create(CONSTRAINTS_NO_FOLDING_NAME)
        		);
        options.addOption(
        		OptionBuilder
        		.withLongOpt("machine-readable")
        		.withDescription("print a machine readable list of supports, for each constraint")
        		.create(MACHINE_READABLE_RESULTS_PARAM_NAME)
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
                .hasArg().withArgName("level")
                .withLongOpt("threshold")
                .withDescription("threshold for support (reliability); it must be a real value ranging from 0.0 to 1.0")
                .withType(new Double(0))
                .create(THRESHOLD_PARAM_NAME)
        );
        options.addOption(
        		OptionBuilder
        		.hasArg().withArgName("level")
        		.withLongOpt("confidence")
        		.withDescription("threshold for confidence level (relevance); it must be a real value ranging from 0.0 to 1.0")
        		.withType(new Double(0))
        		.create(CONFIDENCE_PARAM_NAME)
        		);
        options.addOption(
        		OptionBuilder
        		.hasArg().withArgName("factor")
        		.withLongOpt("interest")
        		.withDescription("threshold for interest factor (relevance); it must be a real value ranging from 0.0 to 1.0")
        		.withType(new Double(0))
        		.create(INTEREST_PARAM_NAME)
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
        		.withDescription("print the discovered process in ConDec format into the specified file")
        		.withType(new String())
        		.create(PRINT_CONDEC_PARAM_NAME)
        		);
        options.addOption(OptionBuilder
        		.hasArg().withArgName("path")
        		.withLongOpt("print-weighted-autom")
        		.withDescription("print the discovered process in weighted automaton XML format, into the specified file")
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
