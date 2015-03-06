package minerful;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.NavigableMap;

import javax.xml.bind.JAXBException;

import minerful.concept.constraint.TaskCharRelatedConstraintsBag;
import minerful.index.LinearConstraintsIndexFactory;
import minerful.io.ConstraintsPrinter;
import minerful.logparser.LogParser;
import minerful.params.SystemCmdParameters;
import minerful.params.ViewCmdParameters;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class MinerFulProcessViewerStarter extends AbstractMinerFulStarter {

	@Override
	public Options setupOptions() {
		Options cmdLineOptions = new Options();
		
		Options systemOptions = SystemCmdParameters.parseableOptions(),
				viewOptions = ViewCmdParameters.parseableOptions();
    	for (Object opt: viewOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: systemOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
		
		return cmdLineOptions;
	}

	public static void main(String[] args) {
		MinerFulProcessViewerStarter procViewStarter = new MinerFulProcessViewerStarter();
    	Options cmdLineOptions = procViewStarter.setupOptions();

        ViewCmdParameters viewParams =
        		new ViewCmdParameters(
        				cmdLineOptions,
        				args);
        SystemCmdParameters systemParams =
        		new SystemCmdParameters(
        				cmdLineOptions,
    					args);

        if (systemParams.help) {
        	systemParams.printHelp(cmdLineOptions);
        	System.exit(0);
        }

	}
	
	public void print(TaskCharRelatedConstraintsBag bag, ViewCmdParameters viewParams, SystemCmdParameters systemParams, LogParser logParser) {
		ConstraintsPrinter printer = new ConstraintsPrinter(bag, viewParams.supportThreshold, viewParams.interestThreshold);
		PrintWriter outWriter = null;

		if (viewParams.machineReadableResults) {
        	logger.info(printer.printBagAsMachineReadable(
        			(systemParams.debugLevel.compareTo(
        					SystemCmdParameters.DebugLevel.debug) >= 0)
					)
			);
        }
        if (viewParams.fileToSaveConstraintsCsv != null) {
           	try {
    				outWriter = new PrintWriter(viewParams.fileToSaveConstraintsCsv);
    	        	outWriter.print(printer.printBagCsv());
    	        	outWriter.flush();
    	        	outWriter.close();
    	        	System.out.println("Discovered constraints written in CSV format on " + viewParams.fileToSaveConstraintsCsv);
    			} catch (FileNotFoundException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
        }
        if (viewParams.fileToSaveConDecDefinition != null) {
        	try {
				printer.printConDecModel(viewParams.fileToSaveConDecDefinition);
	        	System.out.println("Discovered process written in ConDec/Declare XML format on " + viewParams.fileToSaveConDecDefinition);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

        switch (viewParams.constraintsSorting) {
		case support:
			bag = LinearConstraintsIndexFactory.indexByTaskCharAndSupport(bag);
			break;
		case interest:
			bag = LinearConstraintsIndexFactory.indexByTaskCharAndInterest(bag);
		case type:
		default:
			break;
		}

        if (viewParams.noFoldingRequired) {
        	switch (viewParams.constraintsSorting) {
        	case interest:
        		logger.debug(printer.printUnfoldedBagOrderedByInterest());
        	case support:
        		logger.debug(printer.printUnfoldedBagOrderedBySupport());
        		break;
        	case type:
    		default:
    			break;
        	}
        } else {
        	logger.info(printer.printBag());
        }
    	
        
		if (viewParams.fileToSaveDotFileForAutomaton != null) {
        	try {
				outWriter = new PrintWriter(viewParams.fileToSaveDotFileForAutomaton);
	        	outWriter.print(printer.printDotAutomaton());
	        	outWriter.flush();
	        	outWriter.close();
	        	System.out.println("Discovered process automaton written in DOT format on " + viewParams.fileToSaveDotFileForAutomaton);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
		
		if (viewParams.fileToSaveTsmlFileForAutomaton != null) {
        	try {
        		outWriter = new PrintWriter(new File(viewParams.fileToSaveTsmlFileForAutomaton.getAbsolutePath()));
	        	outWriter.print(printer.printTSMLAutomaton());
	        	outWriter.flush();
	        	outWriter.close();
	        	System.out.println("Discovered process automaton written in TSML format on " + viewParams.fileToSaveTsmlFileForAutomaton);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (viewParams.fileToSaveXmlFileForAutomaton != null) {
        	try {
        		outWriter = new PrintWriter(new File(viewParams.fileToSaveXmlFileForAutomaton.getAbsolutePath()));
	        	outWriter.print(printer.printWeightedXmlAutomaton(logParser));
	        	outWriter.flush();
	        	outWriter.close();
	        	System.out.println("Discovered weighted process automaton written in XML format on " + viewParams.fileToSaveXmlFileForAutomaton);
			} catch (FileNotFoundException | JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if (viewParams.folderToSaveXmlFilesForPartialAutomata != null) {
        	try {
        		NavigableMap<String, String> partialAutoMap = printer.printWeightedXmlSubAutomata(logParser);
				StringBuilder subAutomataPathsBuilder = new StringBuilder();
				String subAutomatonPath = null;
				
				for (Map.Entry<String, String> partialAutomaton : partialAutoMap.entrySet()) {
					try {
						subAutomatonPath = viewParams.folderToSaveXmlFilesForPartialAutomata
								+ "/"
								+ partialAutomaton.getKey().replaceAll("\\W", "_")
								+ ".automaton.xml";
						outWriter = new PrintWriter(
								subAutomatonPath
								);
						outWriter.print(partialAutomaton.getValue());
						outWriter.flush();
			        	outWriter.close();
			        	
			        	subAutomataPathsBuilder.append("Sub-automaton for activity \"");
			        	subAutomataPathsBuilder.append(partialAutomaton.getKey());
			        	subAutomataPathsBuilder.append("\" written in XML format on ");
			        	subAutomataPathsBuilder.append(subAutomatonPath);
			        	subAutomataPathsBuilder.append('\n');
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				System.out.println(subAutomataPathsBuilder.toString());
			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
       
		if (viewParams.folderToSaveDotFilesForPartialAutomata != null) {
			NavigableMap<String, String> partialAutoMap = printer.printDotPartialAutomata();
			StringBuilder subAutomataPathsBuilder = new StringBuilder();
			String subAutomatonPath = null;
			
			for (Map.Entry<String, String> partialAutomaton : partialAutoMap.entrySet()) {
				try {
					subAutomatonPath = viewParams.folderToSaveDotFilesForPartialAutomata
							+ "/"
							+ partialAutomaton.getKey().replaceAll("\\W", "_")
							+ ".automaton.dot";
					outWriter = new PrintWriter(
							subAutomatonPath
							);
					outWriter.print(partialAutomaton.getValue());
					outWriter.flush();
		        	outWriter.close();
		        	
		        	subAutomataPathsBuilder.append("Sub-automaton for activity \"");
		        	subAutomataPathsBuilder.append(partialAutomaton.getKey());
		        	subAutomataPathsBuilder.append("\" written in DOT format on ");
		        	subAutomataPathsBuilder.append(subAutomatonPath);
		        	subAutomataPathsBuilder.append('\n');
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.out.println(subAutomataPathsBuilder.toString());
		}
	}
}