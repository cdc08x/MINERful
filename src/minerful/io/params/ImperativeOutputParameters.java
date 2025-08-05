package minerful.io.params;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.File;

import minerful.params.ParamsManager;

public class ImperativeOutputParameters extends ParamsManager{
    public static final String SAVE_PROCESS_DOT_AUTOMATON_PARAM_NAME = "autoDOT";
    public static final String SAVE_PROCESS_DOT_DFG_PARAM_NAME = "dfgDOT";
    public static final String SAVE_PROCESS_TXT_FOOTPRINT_PARAM_NAME = "footprintTXT";
    // public static final String SAVE_PROCESS_CONDENSED_DOT_AUTOMATON_PARAM_NAME = "dotCond"; // TODO
    public static final String SAVE_PROCESS_TSML_AUTOMATON_PARAM_NAME = "autoTSML";
    public static final String FOLDER_FOR_SAVING_DOT_SUBAUTOMATA_PARAM_NAME = "subautosDOT";
    public static final String MAX_MATRIX_STEPS_PARAM_NAME = "maxKSteps";

    // Public fields for file outputs
    public File fileToSaveDotFileForAutomaton;
    public File fileToSaveDotFileForDFG;
    public File fileToSaveTxtFileForFootprintMatrices;
    public File fileToSaveTsmlFileForAutomaton;
    public File folderToSaveDotFilesForPartialAutomata;

    // Numerical parameter
    public int kMaxMatrixSteps = 1;

    public ImperativeOutputParameters() {
   
    	this.fileToSaveDotFileForAutomaton = null;
    	this.fileToSaveDotFileForDFG = null;
    	this.fileToSaveTxtFileForFootprintMatrices = null;
    	this.fileToSaveTsmlFileForAutomaton = null;
        this.folderToSaveDotFilesForPartialAutomata = null;

    }

    public ImperativeOutputParameters(String[] args) {
        this.parseAndSetup(parseableOptions(), args);
    }

    public ImperativeOutputParameters(Options options, String[] args) {
        this.parseAndSetup(options, args);
    }

    @Override
    public void setup(CommandLine line) {
        this.fileToSaveDotFileForAutomaton = openOutputFile(line, SAVE_PROCESS_DOT_AUTOMATON_PARAM_NAME);
        this.fileToSaveDotFileForDFG = openOutputFile(line, SAVE_PROCESS_DOT_DFG_PARAM_NAME);
        this.fileToSaveTxtFileForFootprintMatrices = openOutputFile(line, SAVE_PROCESS_TXT_FOOTPRINT_PARAM_NAME);
        this.fileToSaveTsmlFileForAutomaton = openOutputFile(line, SAVE_PROCESS_TSML_AUTOMATON_PARAM_NAME);
        this.folderToSaveDotFilesForPartialAutomata = openOutputDir(line, FOLDER_FOR_SAVING_DOT_SUBAUTOMATA_PARAM_NAME);

        if (line.hasOption(MAX_MATRIX_STEPS_PARAM_NAME)) {
            try {
                this.kMaxMatrixSteps = Integer.parseInt(line.getOptionValue(MAX_MATRIX_STEPS_PARAM_NAME));
            } catch (NumberFormatException e) {
                System.err.println("Invalid number for maxKSteps. Using default: 0");
            }
        }
    }

    @Override
    public Options addParseableOptions(Options options) {
        Options myOptions = parseableOptions();
        for (Object myOpt : myOptions.getOptions()) {
            options.addOption((Option) myOpt);
        }
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
                Option.builder(SAVE_PROCESS_DOT_AUTOMATON_PARAM_NAME)
                        .hasArg().argName("path")
                        .longOpt("save-automaton-dot")
                        .desc("write a Graphviz DOT format of a finite state automaton")
                        .type(String.class)
                        .build()
        );
        options.addOption(
                Option.builder(SAVE_PROCESS_DOT_DFG_PARAM_NAME)
                        .hasArg().argName("path")
                        .longOpt("save-dfg-dot")
                        .desc("write a Graphviz DOT format of a directly follows graph")
                        .type(String.class)
                        .build()
        );
        options.addOption(
                Option.builder(SAVE_PROCESS_TXT_FOOTPRINT_PARAM_NAME)
                        .hasArg().argName("path")
                        .longOpt("save-footprint-txt")
                        .desc("write footprint matrices as TXT")
                        .type(String.class)
                        .build()
        );
        options.addOption(
                Option.builder(SAVE_PROCESS_TSML_AUTOMATON_PARAM_NAME)
                        .hasArg().argName("path")
                        .longOpt("save-automaton-tsml")
                        .desc("write a TSML format of a finite state automaton")
                        .type(String.class)
                        .build()
        );
        options.addOption(
                Option.builder(FOLDER_FOR_SAVING_DOT_SUBAUTOMATA_PARAM_NAME)
                        .hasArg().argName("path")
                        .longOpt("subautom-folder")
                        .desc("write sub-automata DOT files into this folder")
                        .type(String.class)
                        .build()
        );
        options.addOption(
                Option.builder(MAX_MATRIX_STEPS_PARAM_NAME)
                        .hasArg().argName("int")
                        .longOpt("max-k-steps")
                        .desc("maximum number of matrix steps for footprint analysis. Default 1.")
                        .type(Number.class)
                        .build()
        );

        return options;
    }
}
